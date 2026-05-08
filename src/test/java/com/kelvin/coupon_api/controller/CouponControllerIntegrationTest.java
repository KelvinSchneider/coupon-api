package com.kelvin.coupon_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kelvin.coupon_api.dto.request.CreateCouponRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final OffsetDateTime FUTURE = OffsetDateTime.now().plusDays(30);

    // POST: /coupon

    @Test
    @DisplayName("POST /coupon - should return 201 with sanitized code")
    void shouldCreateCoupon() throws Exception {
        CreateCouponRequest req = new CreateCouponRequest(
                "ABC-123", "desc", new BigDecimal("1.0"), FUTURE, false);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.redeemed").value(false));
    }

    @Test
    @DisplayName("POST /coupon - should return 422 when expiration date is in the past")
    void shouldRejectPastExpiration() throws Exception {
        CreateCouponRequest req = new CreateCouponRequest(
                "ABC123", "desc", new BigDecimal("1.0"), OffsetDateTime.now().minusDays(1), false);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /coupon - should return 400 when required fields missing")
    void shouldReturn400WhenMissingFields() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /coupon - should create as published when published=true")
    void shouldCreateAsPublished() throws Exception {
        CreateCouponRequest req = new CreateCouponRequest(
                "ABC123", "desc", new BigDecimal("1.0"), FUTURE, true);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.published").value(true));
    }

    // GET: /coupon/{id}

    @Test
    @DisplayName("GET /coupon/{id} - should return 200 when coupon exists")
    void shouldGetCoupon() throws Exception {
        String id = createCouponAndGetId();

        mockMvc.perform(get("/coupon/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    @DisplayName("GET /coupon/{id} - should return 404 when coupon not found")
    void shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/coupon/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    // DELETE: /coupon/{id}

    @Test
    @DisplayName("DELETE /coupon/{id} - should return 204 on first delete")
    void shouldDeleteCoupon() throws Exception {
        String id = createCouponAndGetId();

        mockMvc.perform(delete("/coupon/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /coupon/{id} - should return 409 when already deleted")
    void shouldReturn409WhenAlreadyDeleted() throws Exception {
        String id = createCouponAndGetId();

        mockMvc.perform(delete("/coupon/{id}", id));
        mockMvc.perform(delete("/coupon/{id}", id)).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /coupon/{id} - should return 404 when coupon not found")
    void shouldReturn404OnDeleteWhenNotFound() throws Exception {
        mockMvc.perform(delete("/coupon/{id}", "00000000-0000-0000-0000-000000000000")).andExpect(status().isNotFound());
    }

    // Helper

    private String createCouponAndGetId() throws Exception {
        CreateCouponRequest req = new CreateCouponRequest("ABC123", "desc", new BigDecimal("1.0"), FUTURE, false);

        MvcResult result = mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

}
