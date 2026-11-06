package com.premium;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the menu and deals in exactly the shape index.html already uses
 * (id, cat, name, price, tag, img, z, c, note, ing, al / id, name, desc, was, now, img, z, cat).
 */
@RestController
@RequestMapping("/api")
public class MenuController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public MenuController(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @GetMapping("/menu")
    public List<Map<String, Object>> menu() {
        return jdbc.query("select * from menu_item where active order by sort_order", (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getString("id"));
            m.put("cat", rs.getString("cat"));
            m.put("name", rs.getString("name"));
            m.put("price", rs.getBigDecimal("price"));
            m.put("tag", rs.getString("tag"));
            m.put("img", rs.getString("img"));
            String zoom = rs.getString("zoom");
            if (zoom != null) m.put("z", zoom);
            String from = rs.getString("cup_from");
            if (from != null) m.put("c", List.of(from, rs.getString("cup_to")));
            m.put("note", rs.getString("note"));
            m.put("ing", rs.getString("ingredients"));
            m.put("al", rs.getString("allergens"));
            return m;
        });
    }

    @GetMapping("/deals")
    public List<Map<String, Object>> deals() {
        return jdbc.query("select * from deal where active order by sort_order", (rs, i) -> {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("id", rs.getString("id"));
            d.put("name", rs.getString("name"));
            d.put("desc", rs.getString("description"));
            d.put("was", rs.getBigDecimal("was_price"));
            d.put("now", rs.getBigDecimal("now_price"));
            d.put("img", rs.getString("img"));
            String zoom = rs.getString("zoom");
            if (zoom != null) d.put("z", zoom);
            d.put("cat", rs.getString("cat"));
            return d;
        });
    }

    /** Loaded by index.html before its main script: window.PREMIUM_DATA = { menu, deals }. */
    @GetMapping(value = "/data.js", produces = "text/javascript;charset=UTF-8")
    public ResponseEntity<String> dataJs() throws JsonProcessingException {
        Map<String, Object> data = Map.of("menu", menu(), "deals", deals());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body("window.PREMIUM_DATA=" + mapper.writeValueAsString(data) + ";");
    }
}
