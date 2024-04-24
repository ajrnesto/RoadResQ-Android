package com.vulcanizingapp.Objects;

public class Service {
    String id;
    String serviceName;
    String serviceNameAllCaps;
    String description;
    double price;
    boolean status;

    public Service() {
    }

    public Service(String id, String serviceName, String serviceNameAllCaps, String description, double price, boolean status) {
        this.id = id;
        this.serviceName = serviceName;
        this.serviceNameAllCaps = serviceNameAllCaps;
        this.description = description;
        this.price = price;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceNameAllCaps() {
        return serviceNameAllCaps;
    }

    public void setServiceNameAllCaps(String serviceNameAllCaps) {
        this.serviceNameAllCaps = serviceNameAllCaps;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
