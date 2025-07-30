package com.foodApp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class RestaurantLogoUploader {

    private static final String UPLOAD_DIR = "uploads/restaurant_logos";

    static {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println("Created restaurant logo upload directory: " + uploadDir.getAbsolutePath());
        }
    }

    public static String saveLogo(String base64Image, int restaurantId) {
        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }

        String fileExtension = ".png";
        String imageData = base64Image;

        if (imageData.startsWith("data:image/")) {
            int commaIndex = imageData.indexOf(',');
            if (commaIndex > 0) {
                String mimeTypePart = imageData.substring(0, commaIndex);
                if (mimeTypePart.contains("jpeg")) fileExtension = ".jpeg";
                else if (mimeTypePart.contains("png")) fileExtension = ".png";
                else if (mimeTypePart.contains("gif")) fileExtension = ".gif";

                imageData = imageData.substring(commaIndex + 1);
            }
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(imageData);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 string for restaurant logo: " + e.getMessage());
            return null;
        }

        String filename = "restaurant_" + restaurantId + "_" + UUID.randomUUID() + fileExtension;
        File outputFile = new File(UPLOAD_DIR, filename);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(imageBytes);
            System.out.println("Restaurant logo saved to: " + outputFile.getAbsolutePath());
            return "/" + UPLOAD_DIR + "/" + filename;
        } catch (IOException e) {
            System.err.println("Error saving restaurant logo: " + e.getMessage());
            return null;
        }
    }
}
