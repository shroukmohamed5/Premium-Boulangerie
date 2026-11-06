package com.premium;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record Item(
            @NotBlank @Size(max = 300) String key,
            @Min(1) @Max(99) int qty,
            @DecimalMin("0") BigDecimal price,
            @Size(max = 300) String label,
            @Size(max = 120) String name) {
    }

    public record Request(
            @NotBlank @Pattern(regexp = "delivery|pickup") String mode,
            @Size(max = 300) String address,
            @Size(max = 40) String promo,
            @Size(max = 5) String lang,
            BigDecimal clientTotal,
            @NotEmpty @Size(max = 50) List<@Valid Item> items) {
    }

    public record Response(long id, BigDecimal subtotal, BigDecimal discount,
                           BigDecimal deliveryFee, BigDecimal total, boolean mismatch) {
    }

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @RequestBody Request request) {
        return service.place(request);
    }
}
