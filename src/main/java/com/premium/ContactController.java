package com.premium;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ContactController {

    public record ReservationRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 40) String phone,
            @NotNull LocalDate date,
            @NotNull LocalTime time,
            @Min(1) @Max(8) int guests,
            @NotBlank @Pattern(regexp = "Table|Box pick-up") String type) {
    }

    public record NewsletterRequest(@NotBlank @Email @Size(max = 254) String email) {
    }

    // Opening hours, Sunday first (same order as JS getDay() and the page's `sched` array)
    private static final int[][] SCHED = {{8, 15}, {7, 18}, {7, 18}, {7, 18}, {7, 18}, {7, 18}, {8, 19}};

    private final JdbcTemplate jdbc;
    private final PremiumProperties props;

    public ContactController(JdbcTemplate jdbc, PremiumProperties props) {
        this.jdbc = jdbc;
        this.props = props;
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> reserve(@Valid @RequestBody ReservationRequest r) {
        LocalDate today = LocalDate.now(ZoneId.of(props.timezone()));
        if (r.date().isBefore(today.minusDays(1))) {               // 1 day of slack for time-zone differences
            throw bad("Date is in the past");
        }
        int[] hours = SCHED[r.date().getDayOfWeek().getValue() % 7];   // MONDAY=1 ... SUNDAY=7 -> 0
        int minutes = r.time().getHour() * 60 + r.time().getMinute();
        if (minutes % 30 != 0 || minutes < hours[0] * 60 || minutes > (hours[1] - 1) * 60) {
            throw bad("Time is outside opening hours");               // same slots the page offers
        }
        Long id = jdbc.queryForObject("""
                insert into reservation(name, phone, res_date, res_time, guests, kind)
                values (?, ?, ?, ?, ?, ?) returning id""",
                Long.class, r.name().trim(), r.phone().trim(), r.date(), r.time(), r.guests(), r.type());
        return Map.of("id", id);
    }

    @PostMapping("/newsletter")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@Valid @RequestBody NewsletterRequest r) {
        jdbc.update("insert into subscriber(email) values (?) on conflict do nothing",
                r.email().trim().toLowerCase(Locale.ROOT));
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
