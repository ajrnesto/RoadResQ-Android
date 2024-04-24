package com.vulcanizingapp.Objects;

public class ShopItem {
    String id;
    String categoryId;
    double price;
    String productName;
    int stock;
    Long thumbnail;

    public ShopItem() {
    }

    public ShopItem(String id, String categoryId, double price, String productName, int stock, Long thumbnail) {
        this.id = id;
        this.categoryId = categoryId;
        this.price = price;
        this.productName = productName;
        this.stock = stock;
        this.thumbnail = thumbnail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Long getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Long thumbnail) {
        this.thumbnail = thumbnail;
    }
}
