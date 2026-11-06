package com.premium;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Re-prices an order from the database (the browser's prices are never trusted),
 * mirrors the page's rules for promo code and delivery fee, and stores everything.
 *
 * Cart keys sent by the page:
 *   "<menuId>|"                     plain item
 *   "<coffeeId>|Medium, Whole"      coffee with size and milk
 *   "deal|<dealId>"                 deal
 *   "box|4 rolls, Vanilla cream, Pecans[ · “gift note”]"   custom box
 */
@Service
public class OrderService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal TOLERANCE = new BigDecimal("0.02");

    private final JdbcTemplate jdbc;
    private final PremiumProperties props;

    public OrderService(JdbcTemplate jdbc, PremiumProperties props) {
        this.jdbc = jdbc;
        this.props = props;
    }

    private record Line(OrderController.Item item, BigDecimal unit) {
    }

    @Transactional
    public OrderController.Response place(OrderController.Request r) {
        boolean delivery = "delivery".equals(r.mode());
        String address = r.address() == null ? "" : r.address().trim();
        if (delivery && address.isEmpty()) {
            throw bad("Delivery address is required");
        }

        Map<String, BigDecimal> options = loadOptions();
        List<Line> lines = new ArrayList<>();
        BigDecimal subtotal = ZERO;
        for (OrderController.Item it : r.items()) {
            BigDecimal unit = unitPrice(it.key(), options);
            lines.add(new Line(it, unit));
            subtotal = subtotal.add(unit.multiply(BigDecimal.valueOf(it.qty())));
        }

        String promo = null;
        BigDecimal discount = ZERO;
        if (r.promo() != null && !r.promo().isBlank()) {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "select code, percent_off from promo_code where active and upper(code) = upper(?)",
                    r.promo().trim());
            if (!rows.isEmpty()) {
                promo = (String) rows.get(0).get("code");
                int pct = ((Number) rows.get(0).get("percent_off")).intValue();
                discount = subtotal.multiply(BigDecimal.valueOf(pct))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal net = subtotal.subtract(discount);
        BigDecimal fee = delivery && subtotal.signum() > 0 && net.compareTo(props.freeDeliveryOver()) < 0
                ? props.deliveryFee().setScale(2, RoundingMode.HALF_UP) : ZERO;
        BigDecimal total = net.add(fee);
        boolean mismatch = r.clientTotal() == null
                || r.clientTotal().subtract(total).abs().compareTo(TOLERANCE) > 0;

        Long id = jdbc.queryForObject("""
                insert into customer_order(mode, address, promo_code, subtotal, discount, delivery_fee,
                                           total, client_total, mismatch, lang)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id""",
                Long.class, r.mode(), delivery ? address : null, promo, subtotal, discount, fee,
                total, r.clientTotal(), mismatch, r.lang());

        jdbc.batchUpdate("""
                insert into order_item(order_id, item_key, name, label, qty, unit_price, client_unit_price)
                values (?, ?, ?, ?, ?, ?, ?)""", lines, lines.size(), (ps, l) -> {
            ps.setLong(1, id);
            ps.setString(2, l.item().key());
            ps.setString(3, l.item().name());
            ps.setString(4, l.item().label());
            ps.setInt(5, l.item().qty());
            ps.setBigDecimal(6, l.unit());
            ps.setBigDecimal(7, l.item().price());
        });

        return new OrderController.Response(id, subtotal, discount, fee, total, mismatch);
    }

    // ---------- pricing ----------

    private BigDecimal unitPrice(String key, Map<String, BigDecimal> options) {
        int i = key.indexOf('|');
        String kind = i < 0 ? key : key.substring(0, i);
        String rest = i < 0 ? "" : key.substring(i + 1);
        BigDecimal price = switch (kind) {
            case "deal" -> dealPrice(rest, key);
            case "box" -> boxPrice(rest, options, key);
            default -> menuPrice(kind, rest, options, key);
        };
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal dealPrice(String dealId, String key) {
        List<BigDecimal> p = jdbc.queryForList(
                "select now_price from deal where id = ? and active", BigDecimal.class, dealId);
        if (p.isEmpty()) throw bad("Unknown deal in " + key);
        return p.get(0);
    }

    private BigDecimal menuPrice(String id, String rest, Map<String, BigDecimal> options, String key) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select price, cat from menu_item where id = ? and active", id);
        if (rows.isEmpty()) throw bad("Unknown item: " + key);
        BigDecimal price = (BigDecimal) rows.get(0).get("price");
        if ("coffee".equals(rows.get(0).get("cat")) && !rest.isBlank()) {
            String[] parts = rest.split(", ");
            price = price.add(delta(options, "size", parts[0], key));
            if (parts.length > 1) price = price.add(delta(options, "milk", parts[1], key));
        }
        return price;
    }

    private BigDecimal boxPrice(String rest, Map<String, BigDecimal> options, String key) {
        int n = rest.indexOf(" \u00B7 \u201C");          // strip the gift note: " · “...”"
        String body = n >= 0 ? rest.substring(0, n) : rest;
        String[] parts = body.split(", ");                 // size, sauce, topping, topping...
        BigDecimal price = delta(options, "box_size", parts[0], key);
        for (int j = 2; j < parts.length; j++) {
            BigDecimal topping = options.get("topping:" + parts[j]);
            if (topping != null) price = price.add(topping);
        }
        return price;
    }

    private BigDecimal delta(Map<String, BigDecimal> options, String group, String label, String key) {
        BigDecimal d = options.get(group + ":" + label);
        if (d == null) throw bad("Unknown " + group + " option '" + label + "' in " + key);
        return d;
    }

    private Map<String, BigDecimal> loadOptions() {
        Map<String, BigDecimal> m = new HashMap<>();
        jdbc.query("select grp, label, delta from product_option", rs -> {
            m.put(rs.getString(1) + ":" + rs.getString(2), rs.getBigDecimal(3));
        });
        return m;
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
