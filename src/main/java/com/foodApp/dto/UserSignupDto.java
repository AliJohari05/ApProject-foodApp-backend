package com.foodApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodApp.model.Role;
import com.foodApp.model.Status;
import com.foodApp.model.User;

import java.util.regex.Pattern;

public class UserSignupDto {

    @JsonProperty("full_name")
    private String fullName;

    private String phone;
    private String email;
    private String password;
    private String role;
    private String address;

    @JsonProperty("profileImageBase64")
    private String profileImageBase64;

    @JsonProperty("bank_info")
    private BankInfoDto bankInfo;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }

    public BankInfoDto getBankInfo() { return bankInfo; }
    public void setBankInfo(BankInfoDto bankInfo) { this.bankInfo = bankInfo; }

    public static User toUser(UserSignupDto dto) {
        User user = new User();
        user.setName(dto.fullName);
        user.setPhone(dto.phone);
        user.setEmail(dto.email);
        user.setPassword(dto.password);
        user.setAddress(dto.address);
        user.setProfileImageUrl(dto.profileImageBase64);
        Role userRole = Role.valueOf(dto.role.toUpperCase());
        user.setRole(userRole);

        if (userRole == Role.SELLER || userRole == Role.DELIVERY) {
            user.setStatus(Status.PENDING_APPROVAL);
        } else {
            user.setStatus(Status.APPROVED);
        }
        if (dto.bankInfo != null) {
            user.setBankName(dto.bankInfo.getBankName());
            user.setAccountNumber(dto.bankInfo.getAccountNumber());
        }
        return user;
    }
    public boolean isValid() {
        return fullName != null && !fullName.isBlank()
                && phone != null && !phone.isBlank()
                && password != null && !password.isBlank()
                && role != null && !role.isBlank()
                && address != null && !address.isBlank();
    }

    public String validateFields() {
        if (phone != null && !Pattern.matches("\\d{11}", phone)) {
            if(!phone.equalsIgnoreCase("admin"))// Assuming "admin" is a special case phone number
                return "invalid phone";
        }
        if (email != null && !email.isBlank()) {
            String emailRegex = "^[\\w-\\.+]+@[\\w-]+\\.[a-z]{2,4}$";
            if (!Pattern.matches(emailRegex, email.toLowerCase())) {
                return "invalid email";
            }
        }
        return null;
    }
}
