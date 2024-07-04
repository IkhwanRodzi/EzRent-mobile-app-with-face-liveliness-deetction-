package com.example.ezrent.model;

import java.io.Serializable;

public class RentalHouse implements Serializable {
    private String id;
    private String name;
    private String address;
    private String price;
    private String imageUrl;
    private String description;
    private String ownerPhoneNumber;

    // Default constructor
    public RentalHouse() {}

    // Constructor with parameters
    public RentalHouse(String id, String name, String address, String price, String imageUrl, String description, String ownerPhoneNumber) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.ownerPhoneNumber = ownerPhoneNumber;
    }

    // Getter and setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerPhoneNumber() {
        return ownerPhoneNumber;
    }

    public void setOwnerPhoneNumber(String ownerPhoneNumber) {
        this.ownerPhoneNumber = ownerPhoneNumber;
    }
}
