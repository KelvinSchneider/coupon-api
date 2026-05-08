package com.kelvin.coupon_api.controller;

import com.kelvin.coupon_api.dto.request.CreateCouponRequest;
import com.kelvin.coupon_api.dto.response.CouponResponse;
import com.kelvin.coupon_api.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
@Tag(name = "Coupon", description = "Coupon management endpoints")
public class CouponController {

    private final CouponService service;

    @PostMapping
    @Operation(summary = "Create a coupon",
            description = "Creates a new coupon. Special characters in code are stripped automatically.")
    @ApiResponse(responseCode = "201", description = "Coupon created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "422", description = "Business rule violation")
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CreateCouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a coupon by ID")
    @ApiResponse(responseCode = "200", description = "Coupon found")
    @ApiResponse(responseCode = "404", description = "Coupon not found")
    public ResponseEntity<CouponResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft-delete a coupon")
    @ApiResponse(responseCode = "204", description = "Coupon deleted")
    @ApiResponse(responseCode = "404", description = "Coupon not found")
    @ApiResponse(responseCode = "409", description = "Coupon already deleted")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}