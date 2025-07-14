package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.ItemFilterDto;
import com.foodApp.dto.MenuItemDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.MenuItem;
import com.foodApp.model.Role;
import com.foodApp.model.Category;
import com.foodApp.security.TokenService;
import com.foodApp.service.ItemService;
import com.foodApp.service.ItemServiceImpl;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ItemService itemService = new ItemServiceImpl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/items") && "POST".equalsIgnoreCase(method)) {
            handlePostItems(exchange);
        } else if (path.matches("/items/\\d+") && "GET".equalsIgnoreCase(method)) {
            handleGetItemById(exchange);
        } else {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        }
    }

    private void handlePostItems(HttpExchange exchange) throws IOException {
        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
            return;
        }
        try {
            DecodedJWT jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.BUYER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }
        } catch (Exception e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
            return;
        }

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";")[0].trim().equalsIgnoreCase("application/json")) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }

        try (InputStream is = exchange.getRequestBody()) {
            ItemFilterDto filterDto = objectMapper.readValue(is, ItemFilterDto.class);

            BigDecimal maxPrice = (filterDto.getPrice() != null) ? BigDecimal.valueOf(filterDto.getPrice()) : null;

            List<MenuItem> items = itemService.findItemsWithFilters(
                    filterDto.getSearch(),
                    maxPrice,
                    filterDto.getKeywords()
            );

            List<MenuItemDto> itemDtos = items.stream()
                    .map(this::convertToMenuItemDto)
                    .collect(Collectors.toList());

            String jsonResponse = objectMapper.writeValueAsString(itemDtos);
            sendResponse(exchange, 200, jsonResponse);

        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleGetItemById(HttpExchange exchange) throws IOException {
        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
            return;
        }
        try {
            DecodedJWT jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.BUYER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }
        } catch (Exception e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
            return;
        }

        String[] pathSegments = exchange.getRequestURI().getPath().split("/");
        int itemId;
        try {
            itemId = Integer.parseInt(pathSegments[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        }

        try {
            Optional<MenuItem> itemOptional = itemService.findItemById(itemId);
            if (itemOptional.isEmpty()) {
                sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
                return;
            }
            MenuItem item = itemOptional.get();

            MenuItemDto itemDto = convertToMenuItemDto(item);

            String jsonResponse = objectMapper.writeValueAsString(itemDto);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private MenuItemDto convertToMenuItemDto(MenuItem menuItem) {
        MenuItemDto dto = new MenuItemDto();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setImageBase64(menuItem.getImage());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice().intValue());
        dto.setSupply(menuItem.getStock());
        if (menuItem.getCategory() != null) {
            dto.setCategories(menuItem.getCategory().stream()
                    .map(Category::getTitle)
                    .collect(Collectors.toList()));
        }
        dto.setKeywords(menuItem.getKeywords() != null ? List.of(menuItem.getKeywords().split(",")) : null); // نگاشت Keywords
        return dto;
    }
}