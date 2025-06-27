package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.VendorFilterDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.Category;
import com.foodApp.model.MenuItem;
import com.foodApp.model.Restaurant;
import com.foodApp.model.Role;
import com.foodApp.security.TokenService;
import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.foodApp.repository.MenuItemRepository;
import com.foodApp.repository.MenuItemRepositoryImp;
import com.foodApp.util.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.foodApp.dto.MenuItemDto;
import com.foodApp.exception.RestaurantNotFoundException;


public class ListOfVendorsHandler extends BaseHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    private final MenuItemRepository menuItemRepository = new MenuItemRepositoryImp();
    {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Path dispatching
        if (path.equals("/vendors") && "POST".equalsIgnoreCase(method)) {
            handlePostVendors(exchange);
        } else if (path.matches("/vendors/\\d+") && "GET".equalsIgnoreCase(method)) { // Matches /vendors/{id}
            handleGetVendorById(exchange);
        } else {
            sendResponse(exchange, 404, Message.ERROR_404.get());
        }
    }

    private void handlePostVendors(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, Message.METHOD_NOT_ALLOWED.get());
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
            Set<String> allowedRoles = Set.of(Role.BUYER.name(), Role.ADMIN.name());
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

    private void handleGetVendorById(HttpExchange exchange) throws IOException {
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
        try{
            jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.BUYER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403,  Message.FORBIDDEN.get());
                return;
            }
        }catch (Exception e){
            sendResponse(exchange, 403,  Message.FORBIDDEN.get());
            return;
        }

        String[] pathSegments = exchange.getRequestURI().getPath().split("/");
        int vendorId;
        try {
            vendorId = Integer.parseInt(pathSegments[2]); // Assuming path is /vendors/{id}
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            sendResponse(exchange, 400, Message.INVALID_INPUT.get());
            return;
        }

        try {
            Restaurant vendor = restaurantService.findById(vendorId);
            if (vendor == null) {
                sendResponse(exchange, 404, Message.ERROR_404.get());
                return;
            }

            List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(vendorId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("vendor", vendor);

            Map<String, List<MenuItemDto>> categorizedMenuItems = new HashMap<>();
            List<String> menuTitles = new ArrayList<>(); // To store unique menu titles

            for (MenuItem item : menuItems) {
                if (item.getCategory() != null && !item.getCategory().isEmpty()) {
                    for (Category category : item.getCategory()) {
                        String categoryTitle = category.getTitle();
                        if (!menuTitles.contains(categoryTitle)) {
                            menuTitles.add(categoryTitle);
                        }
                        categorizedMenuItems.computeIfAbsent(categoryTitle, k -> new ArrayList<>())
                                .add(convertToMenuItemDto(item));
                    }
                } else {
                    // Handle items without a specific category or add to a "General" category
                    String generalCategory = "General";
                    if (!menuTitles.contains(generalCategory)) {
                        menuTitles.add(generalCategory);
                    }
                    categorizedMenuItems.computeIfAbsent(generalCategory, k -> new ArrayList<>())
                            .add(convertToMenuItemDto(item));
                }
            }

            responseData.put("menu_titles", menuTitles);
            responseData.putAll(categorizedMenuItems); // Add dynamic keys for categorized items

            String jsonResponse = objectMapper.writeValueAsString(responseData);
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + Message.SERVER_ERROR.get() + ": " + e.getMessage() + "\"}");
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
        return dto;
    }
}