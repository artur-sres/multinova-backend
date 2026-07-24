package com.artur_sres.multinova.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductRequestDTO {

    private Long brandId;

    private Long categoryId;

    @Size(max = 50, message = "Brand code must not exceed 50 characters.")
    private String brandCode;

    @NotBlank(message = "Product name is required.")
    @Size(max = 255, message = "Product name must not exceed 255 characters.")
    private String name;

    private String description;

    @Size(max = 500, message = "Image URL must not exceed 500 characters.")
    private String imageUrl;

    @Min(value = 0, message = "Stock cannot be negative.")
    private int stock;

    public Long getBrandId() {
        return brandId;
    }
    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getBrandCode() {
        return brandCode;
    }
    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }
}
