package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.CouponDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Coupon;
import com.foodApp.model.Role;
import com.foodApp.service.CouponService;
import com.foodApp.service.CouponServiceImpl;
import com.foodApp.security.TokenService;
import com.foodApp.util.Message;
import com.foodApp.util.QueryParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CouponHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CouponService couponService = new CouponServiceImpl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }

        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
            return;
        }

        DecodedJWT jwt;
        try {
            jwt = TokenService.verifyToken(token);
            // فقط نقش‌های buyer و admin مجاز به بررسی کوپن هستند
            Set<String> allowedRoles = Set.of(Role.CUSTOMER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, Message.FORBIDDEN.get());
                return;
            }
        } catch (Exception e) {
            sendResponse(exchange, 403, Message.FORBIDDEN.get());
            return;
        }

        Map<String, String> queryParams = QueryParser.parse(exchange.getRequestURI().getRawQuery());
        String couponCode = queryParams.get("coupon_code");

        if (couponCode == null || couponCode.isBlank()) {
            sendResponse(exchange, 400, Message.INVALID_INPUT.get());
            return;
        }

        try {
            // برای این endpoint، فقط کد کوپن و تاریخ اعتبار آن بررسی می‌شود.
            // بررسی‌های پیچیده‌تر مانند min_price و تعداد استفاده واقعی، در زمان ثبت سفارش انجام خواهد شد.
            Optional<Coupon> couponOptional = couponService.validateAndGetCoupon(couponCode, null, null);

            if (couponOptional.isEmpty()) {
                sendResponse(exchange, 404, Message.ERROR_404.get()); // یافت نشد یا معتبر نیست بر اساس تاریخ
                return;
            }

            CouponDto couponDto = new CouponDto(couponOptional.get());
            String jsonResponse = objectMapper.writeValueAsString(couponDto);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}