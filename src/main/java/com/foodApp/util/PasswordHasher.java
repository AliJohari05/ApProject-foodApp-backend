package com.foodApp.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    // Hashes a plain-text password using BCrypt
    public static String hashPassword(String plainTextPassword) {
        // Generate a salt and hash the password
        // 10 is the log2 of the number of rounds, a good default is 10-12
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(10));
    }

    // Verifies a plain-text password against a hashed password
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        // Check whether the plain-text password matches the hashed password
        // This internally hashes the plainTextPassword with the salt from hashedPassword
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}