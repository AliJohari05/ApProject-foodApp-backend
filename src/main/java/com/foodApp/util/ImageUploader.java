package com.foodApp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID; // For unique filenames

public class ImageUploader {

    // Define the base directory for uploads relative to the project root
    // This assumes your backend project runs from its root directory.
    // E.g., if your backend is 'FoodAppBackend', this directory will be created
    // at 'FoodAppBackend/uploads/profile_images/'
    private static final String UPLOAD_DIR = "uploads/profile_images";

    // Ensure the upload directory exists when the class is loaded
    static {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // Create directories if they don't exist
            System.out.println("Created upload directory: " + uploadDir.getAbsolutePath());
        }
    }

    /**
     * Saves a Base64 encoded image string to a file and returns its relative path/filename.
     * @param base64Image The Base64 encoded image string (can include data URI prefix like "data:image/jpeg;base64,").
     * @param userId The ID of the user (for filename uniqueness/organization).
     * @return The relative path/filename of the saved image within UPLOAD_DIR (e.g., "/uploads/profile_images/user_1_abcd.png"), or null if saving fails.
     */
    public static String saveProfileImage(String base64Image, int userId) {
        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }

        // --- Basic Image Format Detection and Data URI Prefix Removal ---
        String fileExtension = ".png"; // Default to PNG
        String imageData = base64Image;

        if (imageData.startsWith("data:image/")) {
            int commaIndex = imageData.indexOf(',');
            if (commaIndex > 0) {
                String mimeTypePart = imageData.substring(0, commaIndex);
                if (mimeTypePart.contains("jpeg")) {
                    fileExtension = ".jpeg";
                } else if (mimeTypePart.contains("png")) {
                    fileExtension = ".png";
                } else if (mimeTypePart.contains("gif")) {
                    fileExtension = ".gif";
                }
                imageData = imageData.substring(commaIndex + 1); // Remove data URI prefix
            }
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(imageData);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 string for image: " + e.getMessage());
            return null;
        }

        // Create a unique filename for the image using user ID and UUID
        String filename = "user_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;
        File outputFile = new File(UPLOAD_DIR, filename);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(imageBytes);
            System.out.println("Image saved to: " + outputFile.getAbsolutePath());
            // Return a path that the frontend can potentially use (e.g., if backend serves static files)
            // This path must be prepended with the BASE_URL in the frontend for full URL.
            return "/" + UPLOAD_DIR + "/" + filename; // Return with leading slash for URL convention
        } catch (IOException e) {
            System.err.println("Error saving image file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes a profile image given its URL or relative path (as stored in the database).
     * @param imageUrl The URL of the image, e.g., "/uploads/profile_images/user_1_abcd.png"
     */
    public static void deleteProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        // Extract the filename relative to UPLOAD_DIR
        String filenameComponent = null;
        if (imageUrl.startsWith("/" + UPLOAD_DIR + "/")) {
            filenameComponent = imageUrl.substring(("/" + UPLOAD_DIR + "/").length());
        } else {
            // If it's just the filename, assume it's directly in UPLOAD_DIR
            filenameComponent = new File(imageUrl).getName(); // get only filename part
        }

        if (filenameComponent.isEmpty()) return;

        File fileToDelete = new File(UPLOAD_DIR, filenameComponent);

        if (fileToDelete.exists() && fileToDelete.isFile()) {
            if (fileToDelete.delete()) {
                System.out.println("Deleted old profile image: " + fileToDelete.getAbsolutePath());
            } else {
                System.err.println("Failed to delete old profile image: " + fileToDelete.getAbsolutePath());
            }
        } else {
            System.out.println("Old profile image not found to delete: " + fileToDelete.getAbsolutePath());
        }
    }
}