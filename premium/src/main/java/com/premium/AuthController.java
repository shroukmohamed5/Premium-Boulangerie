package com.premium;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

/**
 * E-mail + password accounts. Passwords are stored as BCrypt hashes. A login creates a random
 * session token; only its SHA-256 is stored in the database, and the token itself travels in an
 * HttpOnly, SameSite=Lax cookie.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record LoginRequest(
            @NotBlank @Size(max = 254) String email,
            @NotBlank @Size(max = 72) String password) {
    }

    public record AvatarRequest(
            @Size(max = 100_000)
            @Pattern(regexp = "data:image/(png|jpeg|webp);base64,[A-Za-z0-9+/=]+") String avatar) {
    }

    public record UserView(String name, String email, String avatar) {
    }

    private record Current(long id, UserView view) {
    }

    private static final String COOKIE = "premium_session";
    private static final Duration SESSION_TTL = Duration.ofDays(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate jdbc;
    private final RateLimiter limiter;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final String dummyHash = encoder.encode("not-a-real-password");   // equalises timing for unknown e-mails

    public AuthController(JdbcTemplate jdbc, RateLimiter limiter) {
        this.jdbc = jdbc;
        this.limiter = limiter;
    }

    @PostMapping("/register")
    public ResponseEntity<UserView> register(@Valid @RequestBody RegisterRequest r, HttpServletRequest req) {
        limiter.check("auth:" + req.getRemoteAddr());
        if (tooLong(r.password())) throw new ApiException(HttpStatus.BAD_REQUEST, "validation");
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        String name = r.name().trim();
        Long id;
        try {
            id = jdbc.queryForObject(
                    "insert into app_user(email, name, password_hash) values (?, ?, ?) returning id",
                    Long.class, email, name, encoder.encode(r.password()));
        } catch (DuplicateKeyException e) {
            throw new ApiException(HttpStatus.CONFLICT, "email_taken");
        }
        return startSession(HttpStatus.CREATED, id, new UserView(name, email, null), req);
    }

    @PostMapping("/login")
    public ResponseEntity<UserView> login(@Valid @RequestBody LoginRequest r, HttpServletRequest req) {
        limiter.check("auth:" + req.getRemoteAddr());
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        List<UserRow> rows = jdbc.query(
                "select id, name, email, avatar, password_hash from app_user where email = ?",
                (rs, i) -> new UserRow(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                        rs.getString("avatar"), rs.getString("password_hash")),
                email);
        String hash = rows.isEmpty() ? dummyHash : rows.get(0).hash();
        boolean ok = !tooLong(r.password()) && encoder.matches(r.password(), hash);
        if (rows.isEmpty() || !ok) throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials");
        UserRow u = rows.get(0);
        jdbc.update("delete from auth_session where expires_at < now()");
        return startSession(HttpStatus.OK, u.id(), new UserView(u.name(), u.email(), u.avatar()), req);
    }

    @GetMapping("/me")
    public ResponseEntity<UserView> me(HttpServletRequest req) {
        Current c = current(req).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "unauthenticated"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(c.view());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req) {
        Cookie c = WebUtils.getCookie(req, COOKIE);
        if (c != null) jdbc.update("delete from auth_session where token_hash = ?", sha256(c.getValue()));
        ResponseCookie cleared = cookie("", req, Duration.ZERO);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cleared.toString()).build();
    }

    @PutMapping("/avatar")
    public ResponseEntity<Void> avatar(@Valid @RequestBody AvatarRequest r, HttpServletRequest req) {
        Current c = current(req).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "unauthenticated"));
        jdbc.update("update app_user set avatar = ? where id = ?", r.avatar(), c.id());
        return ResponseEntity.noContent().build();
    }

    // ---------- helpers ----------

    private record UserRow(long id, String name, String email, String avatar, String hash) {
    }

    private ResponseEntity<UserView> startSession(HttpStatus status, long userId, UserView view, HttpServletRequest req) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        jdbc.update("insert into auth_session(token_hash, user_id, expires_at) values (?, ?, now() + interval '30 days')",
                sha256(token), userId);
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie(token, req, SESSION_TTL).toString())
                .cacheControl(CacheControl.noStore())
                .body(view);
    }

    private Optional<Current> current(HttpServletRequest req) {
        Cookie c = WebUtils.getCookie(req, COOKIE);
        if (c == null || c.getValue().isBlank()) return Optional.empty();
        List<Current> rows = jdbc.query("""
                select u.id, u.name, u.email, u.avatar
                from auth_session s join app_user u on u.id = s.user_id
                where s.token_hash = ? and s.expires_at > now()""",
                (rs, i) -> new Current(rs.getLong("id"),
                        new UserView(rs.getString("name"), rs.getString("email"), rs.getString("avatar"))),
                sha256(c.getValue()));
        return rows.stream().findFirst();
    }

    private static ResponseCookie cookie(String value, HttpServletRequest req, Duration maxAge) {
        return ResponseCookie.from(COOKIE, value)
                .httpOnly(true)
                .secure(req.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private static boolean tooLong(String password) {          // BCrypt only uses the first 72 bytes
        return password.getBytes(StandardCharsets.UTF_8).length > 72;
    }

    private static String sha256(String s) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
