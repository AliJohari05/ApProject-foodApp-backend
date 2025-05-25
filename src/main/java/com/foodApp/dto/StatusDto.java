package com.foodApp.dto;

import com.foodApp.model.Status;

public class StatusDto {
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isValid() {
        return Status.isValid(status);
    }
}
