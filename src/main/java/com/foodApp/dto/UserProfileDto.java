package com.foodApp.dto;

import com.foodApp.model.User;

import java.math.BigDecimal;

public class UserProfileDto {
    private String id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String role;
    private String profileImageUrl;
    private BankInfoDto bankInfo;
    private BigDecimal walletBalance;
    private String status;
    public UserProfileDto() {
    }
    public UserProfileDto(User user) {
        this.id = String.valueOf(user.getUserId());
        this.fullName = user.getName();
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.role = user.getRole().name();
        this.profileImageUrl = user.getProfileImageUrl();
        this.walletBalance = user.getWalletBalance();
        this.status = user.getStatus().name();

        if (user.getBankName() != null && user.getAccountNumber() != null) {
            this.bankInfo = new BankInfoDto(user.getBankName(), user.getAccountNumber());
        }
    }

    // Getters and setters (unchanged)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public BankInfoDto getBankInfo() { return bankInfo; }
    public void setBankInfo(BankInfoDto bankInfo) { this.bankInfo = bankInfo; }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
