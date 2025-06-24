package com.foodApp.httpHandler.admin;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.CouponDto;
import com.foodApp.exception.CouponNotFoundException;
import com.foodApp.exception.RestaurantNotFoundException;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Coupon;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.CouponService;
import com.foodApp.service.CouponServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminCouponsHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CouponService couponService = new CouponServiceImpl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String token = extractToken(exchange);
            if(token == null) {
                sendResponse(exchange, 401, Message.UNSUPPORTED_MEDIA_TYPE.get());
                return;
            }
            DecodedJWT jwt;
            try {
                jwt = TokenService.verifyToken(token);
            } catch (Exception e) {
                sendResponse(exchange, 403, Message.FORBIDDEN.get());
                return;
            }
            if (!jwt.getClaim("role").asString().equals(Role.ADMIN.name())) {
                sendResponse(exchange, 403, Message.UNAUTHORIZED.get());
                return;
            }
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if(path.equals("/admin/coupons")) {
                if(method.equals("GET")) {
                    GetHandlerListAllCoupons(exchange);
                }
                else if(method.equals("POST")) {
                    PostHandlerCreatCoupon(exchange);
                }else {
                    sendResponse(exchange,405,Message.METHOD_NOT_ALLOWED.get());
                    return;
                }
            }
            else if(path.matches("/admin/coupons/\\d+")) {
                int couponId;
                try {
                    couponId = Integer.parseInt(path.split("/")[3]); // /admin/coupons/{id}
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    sendResponse(exchange, 400, Message.INVALID_INPUT.get());
                    return;
                }

                if ("GET".equalsIgnoreCase(method)) {
                    handleGetCouponDetails(exchange, couponId);
                } else if ("PUT".equalsIgnoreCase(method)) {
                    handleUpdateCoupon(exchange, couponId);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    handleDeleteCoupon(exchange, couponId);
                } else {
                    sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
                }
            }else{
                sendResponse(exchange,404,Message.ERROR_404.get());
            }

        }catch(Exception e) {
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
            return;
        }


    }
    private void GetHandlerListAllCoupons(HttpExchange exchange) throws IOException {
        try {
            List<Coupon> coupons = couponService.findAllCoupons();
            List<CouponDto> dtolist = new ArrayList<>();
            for(Coupon coupon : coupons) {
                dtolist.add(new CouponDto(coupon));
            }
            String json = objectMapper.writeValueAsString(dtolist);
            sendResponse(exchange, 200, json);
        }catch(Exception e) {
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
    private void PostHandlerCreatCoupon(HttpExchange exchange) throws IOException {
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, Message.UNSUPPORTED_MEDIA_TYPE.get());
            return;
        }
        try (InputStream is = exchange.getRequestBody()) {
            CouponDto createDto = objectMapper.readValue(is, CouponDto.class);
            Coupon createdCoupon = couponService.createCoupon(createDto);
            CouponDto responseDto = new CouponDto(createdCoupon);
            sendResponse(exchange, 201, objectMapper.writeValueAsString(responseDto)); // 201 Created
        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, Message.INVALID_INPUT.get());
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
    private void handleGetCouponDetails(HttpExchange exchange, int couponId) throws IOException {
        try {
            Optional<Coupon> couponOptional = couponService.getCouponById(couponId);
            if (couponOptional.isEmpty()) {
                sendResponse(exchange, 404, Message.ERROR_404.get());
                return;
            }
            CouponDto responseDto = new CouponDto(couponOptional.get());
            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseDto));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
    private void handleUpdateCoupon(HttpExchange exchange, int couponId) throws IOException {
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, Message.UNSUPPORTED_MEDIA_TYPE.get());
            return;
        }
        try (InputStream is = exchange.getRequestBody()) {
            CouponDto updateDto = objectMapper.readValue(is, CouponDto.class);
            Coupon updatedCoupon = couponService.updateCoupon(couponId, updateDto);
            CouponDto responseDto = new CouponDto(updatedCoupon);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseDto));
        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, Message.INVALID_INPUT.get());
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage()))); // ارسال پیام خطای مشخص
        } catch (RestaurantNotFoundException e) {
            sendResponse(exchange, 404, Message.ERROR_404.get());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
    private void handleDeleteCoupon(HttpExchange exchange, int couponId) throws IOException {
        try {
            couponService.deleteCoupon(couponId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));
        }catch (CouponNotFoundException e){
            sendResponse(exchange, 404, Message.ERROR_404.get());
        }
        catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Message.SERVER_ERROR.get());
        }
    }
}
