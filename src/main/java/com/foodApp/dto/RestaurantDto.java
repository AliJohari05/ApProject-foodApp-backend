package com.foodApp.dto;

import java.util.regex.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RestaurantDto {

    private Integer id; // این فیلد در YAML شما در بخش schema.restaurant تعریف شده بود
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    private Integer tax_fee;
    private Integer additional_fee;



    public RestaurantDto() {
    }

    public RestaurantDto(Integer id, String name, String address, String phone, String logoBase64, Integer tax_fee, Integer additional_fee) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logoBase64 = logoBase64;
        this.tax_fee = tax_fee;
        this.additional_fee = additional_fee;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogoBase64() {
        return logoBase64;
    }
    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public Integer getTax_fee() {
        return tax_fee;
    }
    public void setTax_fee(Integer tax_fee) {
        this.tax_fee = tax_fee;
    }

    public Integer getAdditional_fee() {
        return additional_fee;
    }
    public void setAdditional_fee(Integer additional_fee) {
        this.additional_fee = additional_fee;
    }

    public boolean hasRequiredFields() {
        return  name != null && !name.isBlank() &&
                address != null && !address.isBlank() &&
                phone != null && !phone.isBlank() &&
                logoBase64 != null && !logoBase64.isBlank() &&
                tax_fee != null && additional_fee != null;
    }

    public String validateFields() {
        if (name != null && (name.length() < 2 || name.length() > 100)) {
            return "invalid name: length must be between 2 and 100 characters.";
        }
        if (phone != null && !Pattern.matches("^\\d{11}$", phone)) {
            return "invalid phone: must be 11 digits.";
        }
        if (tax_fee != null && tax_fee < 0) {
            return "invalid tax_fee: cannot be negative.";
        }
        if (additional_fee != null && additional_fee < 0) {
            return "invalid additional_fee: cannot be negative.";
        }
        return null;
    }

}