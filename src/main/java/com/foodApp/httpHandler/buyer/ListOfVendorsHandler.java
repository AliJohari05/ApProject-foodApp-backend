package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.VendorFilterDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Restaurant;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class ListOfVendorsHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange,405, Message.METHOD_NOT_ALLOWED.get());
            return;
        }
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, Message.UNSUPPORTED_MEDIA_TYPE.get());
            return;
        }
        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, Message.UNAUTHORIZED.get());
            return;
        }
        DecodedJWT jwt;
        try{
            jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.CUSTOMER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403,  Message.FORBIDDEN.get());
                return;
            }
        }catch (Exception e){
            sendResponse(exchange, 403,  Message.FORBIDDEN.get());
            return;
        }
        try(InputStream is = exchange.getRequestBody()) {
            VendorFilterDto dto = objectMapper.readValue(is, VendorFilterDto.class);
            List<Restaurant> vendors = restaurantService.findApprovedByFilters(dto.getSearch(),dto.getKeywords());
            String json = objectMapper.writeValueAsString(vendors);
            sendResponse(exchange, 200, json);
        }catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            e.printStackTrace();
            sendResponse(exchange, 400,Message.INVALID_INPUT.get());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + Message.SERVER_ERROR.get() + ": " + e.getMessage() + "\"}");
        }


    }
}
