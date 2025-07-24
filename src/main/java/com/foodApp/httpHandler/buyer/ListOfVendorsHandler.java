// FoodApp/ListOfVendorsHandler.java
package com.foodApp.httpHandler.buyer;

import com.auth0.jwt.exceptions.JWTVerificationException;
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

        if (path.equals("/vendors") && "POST".equalsIgnoreCase(method)) {
            handlePostVendors(exchange);
        } else if (path.matches("/vendors/\\d+") && "GET".equalsIgnoreCase(method)) {
            System.out.println("GET vendor list");
            handleViewVendorMenu(exchange);
        } else {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        }
    }

    private void handlePostVendors(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            return;
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";")[0].trim().equalsIgnoreCase("application/json")) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }
        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
            return;
        }
        DecodedJWT jwt;
        try{
            jwt = TokenService.verifyToken(token);
            Set<String> allowedRoles = Set.of(Role.BUYER.name(), Role.ADMIN.name());
            String userRole = jwt.getClaim("role").asString();
            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get() + ": Insufficient role")));
                return;
            }
        }catch (JWTVerificationException e){ // Catch specific JWT validation errors first
            e.printStackTrace(); // Print stack trace to see the exact JWT error
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get() + ": Invalid or expired token")));
            return;
        }
        catch (Exception e){ // Catch other unexpected exceptions
            e.printStackTrace(); // Print stack trace for other errors
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get() + ": Role check failed or other authorization issue")));
            return;
        }
        try(InputStream is = exchange.getRequestBody()) {
            VendorFilterDto dto = objectMapper.readValue(is, VendorFilterDto.class);
            List<Restaurant> vendors = restaurantService.findApprovedByFilters(dto.getSearch(),dto.getKeywords());
            String json = objectMapper.writeValueAsString(vendors);
            sendResponse(exchange, 200, json);
        }catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            e.printStackTrace();
            sendResponse(exchange, 400,objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleViewVendorMenu(HttpExchange exchange) throws IOException {
        System.out.println("GET vendor List start method");
        String token = extractToken(exchange);
        if (token == null) {
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
            return;
        }
        System.out.println("hiiiiiiiiiii");

        DecodedJWT jwt;
        try{
            System.out.println("start");
            jwt = TokenService.verifyToken(token); // Likely throws exception here
            System.out.println("fffffffff"); // This line won't be reached if exception is thrown

            Set<String> allowedRoles = Set.of(Role.BUYER.name(), Role.ADMIN.name(),Role.SELLER.name());
            System.out.println("fsfsfsfsf");

            String userRole = jwt.getClaim("role").asString();

            System.out.println("DEBUG: User Role from JWT = " + userRole);
            System.out.println("DEBUG: Allowed Roles for endpoint = " + allowedRoles);
            System.out.println("DEBUG: Is user role allowed? " + allowedRoles.contains(userRole));

            if (!allowedRoles.contains(userRole)) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get() + ": Insufficient role")));
                return;
            }
        }catch (JWTVerificationException e){ // Catch specific JWT validation errors first
            e.printStackTrace(); // Print stack trace to see the exact JWT error
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get() + ": Invalid or expired token")));
            return;
        }
        catch (Exception e){ // Catch other unexpected exceptions
            e.printStackTrace(); // Print stack trace for other errors
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get() + ": Role check failed or other authorization issue")));
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int restaurantId;
        try {
            restaurantId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        }

        try {
            Restaurant restaurant = restaurantService.findById(restaurantId);
            if (restaurant == null) {
                sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
                return;
            }

            List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(restaurantId);

            Map<String, Object> response = new HashMap<>();
            response.put("vendor", restaurant);

            Map<String, List<MenuItemDto>> categorizedMenuItems = new HashMap<>();
            List<String> menuTitles = new ArrayList<>();

            for (MenuItem item : menuItems) {
                String categoryTitle = "Uncategorized";
                if (item.getCategory() != null && !item.getCategory().isEmpty()) {
                    for (Category category : item.getCategory()) {
                        String currentCategoryTitle = category.getTitle();
                        categorizedMenuItems.computeIfAbsent(currentCategoryTitle, k -> {
                            menuTitles.add(currentCategoryTitle);
                            return new ArrayList<>();
                        }).add(convertToMenuItemDto(item));
                    }
                } else {
                    categorizedMenuItems.computeIfAbsent(categoryTitle, k -> {
                        menuTitles.add(categoryTitle);
                        return new ArrayList<>();
                    }).add(convertToMenuItemDto(item));
                }
            }

            response.put("menu_titles", menuTitles);
            response.putAll(categorizedMenuItems);

            String jsonResponse = objectMapper.writeValueAsString(response);
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
        dto.setKeywords(List.of(menuItem.getKeywords().split(" ")));
        if (menuItem.getCategory() != null) {
            dto.setCategories(menuItem.getCategory().stream()
                    .map(Category::getTitle)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}