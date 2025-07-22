package com.foodApp.httpHandler.restaurant;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodApp.dto.*;
import com.foodApp.httpHandler.BaseHandler;
import com.foodApp.model.*;
import com.foodApp.exception.*;
import com.foodApp.service.*;
import com.foodApp.util.Message;
import com.foodApp.security.TokenService;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.JsonMappingException;


import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;


public class RestaurantHandler extends BaseHandler implements HttpHandler {
    private final RestaurantService restaurantService = new RestaurantServiceImpl();
    private final UserService userService = new UserServiceImpl();
    private final ItemService itemService = new ItemServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final OrderService orderService = new OrderServiceImpl();

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
        else if (path.matches("/restaurants/\\d+/menu/[^/]+")) { // /restaurants/{id}/menu/{title}
            int restaurantId = -1;
            String menuTitle = null;
            try {
                restaurantId = Integer.parseInt(path.split("/")[2]);
                menuTitle = path.split("/")[4];
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }
            if ("PUT".equalsIgnoreCase(method)) {
                handleAddMenuItemToMenu(exchange, userId, restaurantId, menuTitle);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        }
        else if (path.matches("/restaurants/\\d+/menu/[^/]+/\\d+")) { // /restaurants/{id}/menu/{title}/{item_id}
            int restaurantId = -1;
            String menuTitle = null;
            try {
                restaurantId = Integer.parseInt(path.split("/")[2]);
                menuTitle = path.split("/")[4];
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }
            if ("DELETE".equalsIgnoreCase(method)) {
                handleDeleteRestaurantMenu(exchange, userId, restaurantId, menuTitle);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        }
        else if (path.matches("/restaurants/\\d+/orders")) { // /restaurants/{id}/orders (فقط GET)
            int restaurantId = -1;
            try {
                restaurantId = Integer.parseInt(path.split("/")[2]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }
            if ("GET".equalsIgnoreCase(method)) {
                handleGetRestaurantOrders(exchange, userId, restaurantId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        } else if (path.matches("/restaurants/orders/\\d+")) { // /restaurants/orders/{order_id} (فقط PATCH)
            int orderId = -1;
            try {
                orderId = Integer.parseInt(path.split("/")[3]); // /restaurants/orders/{order_id}
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
                return;
            }
            if ("PATCH".equalsIgnoreCase(method)) {
                handlePatchRestaurantOrderStatus(exchange, userId, orderId);
            } else {
                sendResponse(exchange, 405, objectMapper.writeValueAsString(Map.of("error", Message.METHOD_NOT_ALLOWED.get())));
            }
        }
        else {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        }
    }

    private void handlePatchRestaurantOrderStatus(HttpExchange exchange, int userId, int orderId) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";")[0].trim().equalsIgnoreCase("application/json")) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }

        try (InputStream is = exchange.getRequestBody()) {
            // خواندن داده‌های ورودی
            Map<String, String> requestBody = objectMapper.readValue(is, Map.class);
            String newStatus = requestBody.get("status");

            if (newStatus == null) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.MISSING_FIELDS.get() + ": status is required.")));
                return;
            }

            orderService.updateOrderStatusByRestaurant(orderId, newStatus);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));

        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
        } catch (OrderNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
        } catch (InvalidDeliveryStatusTransitionException e) {
            sendResponse(exchange, 409, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleGetRestaurantOrders(HttpExchange exchange, int userId, int restaurantId) throws IOException {
        String search = exchange.getRequestURI().getQuery();
        String status = null;
        String customerName = null;
        String courierName = null;

        if (search != null) {
            Map<String, String> queryParams = Arrays.stream(search.split("&"))
                    .map(param -> param.split("="))
                    .collect(Collectors.toMap(param -> param[0], param -> param.length > 1 ? param[1] : null));

            status = queryParams.get("status");
            customerName = queryParams.get("user");
            courierName = queryParams.get("courier");
        }

        try {
            List<Order> orders = orderService.getRestaurantOrders(restaurantId, search, customerName, courierName, OrderStatus.valueOf(status));
            List<OrderDto> responseDtos = orders.stream()
                    .map(order -> new OrderDto(order.getId(), order.getCustomer().getUserId(), order.getStatus(), order.getTotalPrice()))
                    .collect(Collectors.toList());

            String jsonResponse = objectMapper.writeValueAsString(responseDtos);
            sendResponse(exchange, 200, jsonResponse);
        } catch (RestaurantNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }



    private void handleAddMenuItemToMenu(HttpExchange exchange, int userId, int restaurantId, String menuTitle) throws JsonProcessingException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";")[0].trim().equalsIgnoreCase("application/json")) {
            sendResponse(exchange, 415, objectMapper.writeValueAsString(Map.of("error", Message.UNSUPPORTED_MEDIA_TYPE.get())));
            return;
        }
        try (InputStream is = exchange.getRequestBody()) {
            Map<String, Integer> requestBody = objectMapper.readValue(is, Map.class);
            Integer menuItemId = requestBody.get("item_id");

            if (menuItemId == null) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.MISSING_FIELDS.get() + ": item_id is required.")));
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

            categoryService.addMenuItemToCategory(restaurantId, menuTitle, menuItemId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));

        } catch (JsonMappingException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
        } catch (IllegalArgumentException e) { // برای تضاد یا خطاهای "آیتم در دسته نیست" از سرویس
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (RestaurantNotFoundException | MenuItemNotFoundException e) { // برای 404 از سرویس (رستوران، دسته، آیتم)
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (UnauthorizedAccessException e) { // برای آیتمی که به رستوران تعلق ندارد
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
        }
    }

    private void handleDeleteRestaurantMenu(HttpExchange exchange, int userId, int restaurantId, String menuTitle) throws JsonProcessingException {
        try {
            String[] parts = exchange.getRequestURI().getPath().split("/");
            int menuItemId = Integer.parseInt(parts[5]);

            Restaurant restaurant = restaurantService.findById(restaurantId);
            if (restaurant == null) {
                sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
                return;
            }
            if (restaurant.getOwner().getUserId() != userId) {
                sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
                return;
            }

            // حذف آیتم از دسته‌بندی
            categoryService.removeMenuItemFromCategory(restaurantId, menuTitle, menuItemId);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("message", Message.SUCCESS.get())));

        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", Message.INVALID_INPUT.get())));
        } catch (RestaurantNotFoundException | MenuItemNotFoundException e) {
            sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
        } catch (UnauthorizedAccessException e) {
            sendResponse(exchange, 403, objectMapper.writeValueAsString(Map.of("error", Message.FORBIDDEN.get())));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of("error", Message.SERVER_ERROR.get() + ": " + e.getMessage())));
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
        Category createCategory = categoryService.createRestaurantCategory(restaurantId,createMenuDto);
        sendResponse(exchange, 200, objectMapper.writeValueAsString(Map.of("title", createCategory.getTitle())));

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
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";")[0].trim().equalsIgnoreCase("application/json")) {
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



            Restaurant restaurant = restaurantService.findById(restaurantId);
            if (restaurant == null) {
                sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of("error", Message.ERROR_404.get())));
                return;
            }
            if (!createDto.getVendorId().equals(restaurant.getId())) {
                sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of("error", "Vendor ID in body does not match restaurant ID in path.")));
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