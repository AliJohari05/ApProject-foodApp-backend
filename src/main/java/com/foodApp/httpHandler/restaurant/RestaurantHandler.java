package com.foodApp.httpHandler.restaurant;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.CreateMenuDto;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.*;
import com.foodApp.exception.*;
import com.foodApp.service.RestaurantService;
import com.foodApp.service.RestaurantServiceImpl;
import com.foodApp.service.UserService;
import com.foodApp.service.UserServiceImpl;
import com.foodApp.util.Message;
import com.foodApp.security.TokenService;
import com.foodApp.dto.RestaurantDto;
import com.foodApp.dto.MenuItemCreateUpdateDto;
import com.foodApp.dto.MenuItemDto;
import com.foodApp.service.ItemService;
import com.foodApp.service.ItemServiceImpl;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.JsonMappingException;


import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;


public class RestaurantHandler extends BaseHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    private final UserService userService = new UserServiceImpl();
    private final ItemService itemService = new ItemServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        String token = extractToken(exchange);
        int userId = -1;
        try {
            DecodedJWT jwt = TokenService.verifyToken(token);
            String userRole = jwt.getClaim("role").asString();
            if (!Role.SELLER.name().equalsIgnoreCase(userRole)) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }
            userId = Integer.parseInt(jwt.getSubject());
        } catch (Exception e) {
            sendResponse(exchange, 401, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get())));
            return;
        }

        if (path.equals("/restaurants")) {
            if ("POST".equalsIgnoreCase(method)) {
                handlePostRestaurant(exchange, userId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        } else if (path.equals("/restaurants/mine")) {
            if ("GET".equalsIgnoreCase(method)) {
                handleGetMineRestaurants(exchange, userId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        } else if (path.matches("/restaurants/\\d+")) {
            int restaurantId = -1;
            try {
                restaurantId = Integer.parseInt(path.split("/")[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }
            if ("PUT".equalsIgnoreCase(method)) {
                handlePutRestaurant(exchange, userId, restaurantId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        } else if (path.matches("/restaurants/\\d+/item")) {
            int restaurantId = -1;
            try {
                restaurantId = Integer.parseInt(path.split("/")[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }
            if ("POST".equalsIgnoreCase(method)) {
                handleAddMenuItem(exchange, userId, restaurantId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        } else if (path.matches("/restaurants/\\d+/item/\\d+")) {
            int restaurantId = -1;
            int menuItemId = -1;
            try {
                restaurantId = Integer.parseInt(path.split("/")[2]);
                menuItemId = Integer.parseInt(path.split("/")[4]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }
            if ("PUT".equalsIgnoreCase(method)) {
                handleEditRestaurantItem(exchange, userId, restaurantId, menuItemId);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDeleteRestaurantItem(exchange, userId, restaurantId, menuItemId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        }
        else if(path.matches("/restaurants/\\d+/menu")) {
            int restaurantId = -1;
            try {
                restaurantId = Integer.parseInt(path.split("/")[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }
            if ("POST".equalsIgnoreCase(method)) {
                handlePostRestaurantCreateMenu(exchange,userId,restaurantId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        }
        else {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        }
    }

    private void handlePostRestaurantCreateMenu(HttpExchange exchange,int userId ,int restaurantId) throws JsonProcessingException {
        InputStream body = exchange.getRequestBody();
        CreateMenuDto createMenuDto;
        try {
            createMenuDto = objectMapper.readValue(body,CreateMenuDto.class);
        }catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        } catch (Exception e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        }
        if (createMenuDto.getTitle().isEmpty()) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.MISSING_FIELDS.get())));
            return;
        }
        Restaurant restaurant = restaurantService.findById(restaurantId);
        if (restaurant == null) {
            sendResponse(exchange,404,objectMapper.writeValueAsString(Map.of("error",Message.ERROR_404.get())));
            return;
        }
        if(restaurant.getOwner().getUserId() != userId) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
            return;
        }


        sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("title", Message.CREATED_MENU.get())));

    }

    private void handlePostRestaurant(HttpExchange exchange, int userId) throws IOException {
        InputStream body = exchange.getRequestBody();
        RestaurantDto restaurantDto;
        try {
            restaurantDto = objectMapper.readValue(body, RestaurantDto.class);
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        } catch (Exception e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        }

        if (!restaurantDto.hasRequiredFields()) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.MISSING_FIELDS.get())));
            return;
        }

        String validationError = restaurantDto.validateFields();
        if (validationError != null) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", validationError))); // ارسال خطای اعتبارسنجی
            return;
        }

        try {
            Restaurant restaurantModel = new Restaurant();
            restaurantModel.setName(restaurantDto.getName());
            restaurantModel.setAddress(restaurantDto.getAddress());
            restaurantModel.setPhone(restaurantDto.getPhone());
            restaurantModel.setLogobase64(restaurantDto.getLogoBase64());
            restaurantModel.setTaxFee(restaurantDto.getTax_fee() != null ? restaurantDto.getTax_fee() : 0);
            restaurantModel.setAdditionalFee(restaurantDto.getAdditional_fee() != null ? restaurantDto.getAdditional_fee() : 0);

            User owner = userService.findById(userId);
            if (owner == null) {
                sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.USER_NOT_FOUND.get())));
                return;
            }
            restaurantModel.setOwner(owner);
            restaurantModel.setApproved(true);
            restaurantModel.setCreatedAt(LocalDateTime.now());
            restaurantModel.setUpdatedAt(LocalDateTime.now());

            Restaurant savedRestaurant = restaurantService.registerRestaurantAndReturn(restaurantModel);

            RestaurantDto responseDto = new RestaurantDto(
                    savedRestaurant.getId(),
                    savedRestaurant.getName(),
                    savedRestaurant.getAddress(),
                    savedRestaurant.getPhone(),
                    savedRestaurant.getLogobase64(),
                    savedRestaurant.getTaxFee(),
                    savedRestaurant.getAdditionalFee()
            );

            sendResponse(exchange, 201, objectMapper.writeValueAsString(responseDto));

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleGetMineRestaurants(HttpExchange exchange, int userId) throws IOException {
        try {
            List<Restaurant> myRestaurants = restaurantService.getRestaurantByOwnerId(userId);
            List<RestaurantDto> responseDtos = myRestaurants.stream()
                    .map(r -> new RestaurantDto(
                            r.getId(),
                            r.getName(),
                            r.getAddress(),
                            r.getPhone(),
                            r.getLogobase64(),
                            r.getTaxFee(),
                            r.getAdditionalFee()))
                    .collect(Collectors.toList());
            String jsonResponse = objectMapper.writeValueAsString(responseDtos);
            sendResponse(exchange, 200, jsonResponse);
        } catch (UserNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.USER_NOT_FOUND.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }


    private void handlePutRestaurant(HttpExchange exchange, int userId, int restaurantId) throws IOException {
        InputStream body = exchange.getRequestBody();
        RestaurantDto restaurantDto;
        try {
            restaurantDto = objectMapper.readValue(body, RestaurantDto.class);
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
            return;
        }
        String validationError = restaurantDto.validateFields();
        if (validationError != null) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", validationError))); // ارسال خطای اعتبارسنجی
            return;
        }
        try {
            Restaurant updatedRestaurant = restaurantService.updateRestaurantForPut(restaurantId, restaurantDto, userId);
            RestaurantDto responseDto = new RestaurantDto(
                    updatedRestaurant.getId(),
                    updatedRestaurant.getName(),
                    updatedRestaurant.getAddress(),
                    updatedRestaurant.getPhone(),
                    updatedRestaurant.getLogobase64(),
                    updatedRestaurant.getTaxFee(),
                    updatedRestaurant.getAdditionalFee()
            );
            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseDto));
        } catch (RestaurantNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.UNAUTHORIZED.get()))); // UNAUTHORIZED یا FORBIDDEN
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleAddMenuItem(HttpExchange exchange, int userId, int restaurantId) throws IOException {
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }
        try (InputStream is = exchange.getRequestBody()) {
            MenuItemCreateUpdateDto createDto = objectMapper.readValue(is, MenuItemCreateUpdateDto.class);

            if (!createDto.hasRequiredFieldsForCreate()) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.MISSING_FIELDS.get())));
                return;
            }
            String validationError = createDto.validateFields();
            if (validationError != null) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", validationError)));
                return;
            }

            if (!createDto.getVendorId().equals(restaurantId)) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", "Vendor ID in body does not match restaurant ID in path.")));
                return;
            }

            Restaurant restaurant = restaurantService.findById(restaurantId);
            if (restaurant == null) {
                sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
                return;
            }
            if (restaurant.getOwner().getUserId() != userId) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }

            MenuItem createdMenuItem = itemService.createRestaurantMenuItem(restaurantId, createDto);
            MenuItemDto responseDto = new MenuItemDto();
            responseDto.setId(createdMenuItem.getId());
            responseDto.setName(createdMenuItem.getName());
            responseDto.setImageBase64(createdMenuItem.getImage());
            responseDto.setDescription(createdMenuItem.getDescription());
            responseDto.setPrice(createdMenuItem.getPrice().intValue());
            responseDto.setSupply(createdMenuItem.getStock());
            responseDto.setKeywords(createdMenuItem.getKeywords() != null ? List.of(createdMenuItem.getKeywords().split(",")) : null);


            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseDto));

        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (RestaurantNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleEditRestaurantItem(HttpExchange exchange, int userId, int restaurantId, int menuItemId) throws IOException {
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }
        try (InputStream is = exchange.getRequestBody()) {
            MenuItemCreateUpdateDto updateDto = objectMapper.readValue(is, MenuItemCreateUpdateDto.class);

            String validationError = updateDto.validateFields();
            if (validationError != null) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", validationError)));
                return;
            }

            Restaurant restaurant = restaurantService.findById(restaurantId);
            if (restaurant == null) {
                sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
                return;
            }
            if (restaurant.getOwner().getUserId() != userId) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }

            MenuItem updatedMenuItem = itemService.updateRestaurantMenuItem(restaurantId, menuItemId, updateDto);
            MenuItemDto responseDto = new MenuItemDto();
            responseDto.setId(updatedMenuItem.getId());
            responseDto.setName(updatedMenuItem.getName());
            responseDto.setImageBase64(updatedMenuItem.getImage());
            responseDto.setDescription(updatedMenuItem.getDescription());
            responseDto.setPrice(updatedMenuItem.getPrice().intValue());
            responseDto.setSupply(updatedMenuItem.getStock());
            responseDto.setKeywords(updatedMenuItem.getKeywords() != null ? List.of(updatedMenuItem.getKeywords().split(",")) : null);

            sendResponse(exchange, 200, objectMapper.writeValueAsString(responseDto));

        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (RestaurantNotFoundException | MenuItemNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleDeleteRestaurantItem(HttpExchange exchange, int userId, int restaurantId, int menuItemId) throws IOException {
        try {
            Restaurant restaurant = restaurantService.findById(restaurantId);
            if (restaurant == null) {
                sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
                return;
            }
            if (restaurant.getOwner().getUserId() != userId) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }

            itemService.deleteRestaurantMenuItem(restaurantId, menuItemId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));

        } catch (RestaurantNotFoundException | MenuItemNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }
}