package com.foodApp.model;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false , length = 100)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false,length = 20,unique = true)
    private String phone;


    @Column(name = "logo_url", nullable = false, columnDefinition = "TEXT")
    private String logobase64;

    @Column(name = "tax_fee")
    private Integer taxFee;

    @Column(name = "additional_fee")
    private Integer additionalFee;

    private boolean approved;

    @ManyToOne // Each user (seller) can have multiple restaurants.
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // === Getters and Setters ===


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

    public String getLogobase64() {
        return logobase64;
    }
    public void setLogobase64(String logobase64) {
        this.logobase64 = logobase64;
    }

    public Integer getTaxFee() {
        return taxFee;
    }
    public void setTaxFee(Integer taxFee) {
        this.taxFee = taxFee;
    }

    public Integer getAdditionalFee() {
        return additionalFee;
    }
    public void setAdditionalFee(Integer additionalFee) {
        this.additionalFee = additionalFee;
    }

    public boolean isApproved() {
        return approved;
    }
    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
