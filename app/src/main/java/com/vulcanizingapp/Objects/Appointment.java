package com.vulcanizingapp.Objects;

public class Appointment {
    String uid;
    String userUid;
    String firstName;
    String lastName;
    String mobile;
    String brand;
    String model;
    String serviceType;
    String description;
    long schedule;
    long timestamp;
    String status;
    String cancelReason;

    public Appointment() {
    }

    public Appointment(String uid, String userUid, String firstName, String lastName, String mobile, String brand, String model, String serviceType, String description, long schedule, long timestamp, String status, String cancelReason) {
        this.uid = uid;
        this.userUid = userUid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.brand = brand;
        this.model = model;
        this.serviceType = serviceType;
        this.description = description;
        this.schedule = schedule;
        this.timestamp = timestamp;
        this.status = status;
        this.cancelReason = cancelReason;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSchedule() {
        return schedule;
    }

    public void setSchedule(long schedule) {
        this.schedule = schedule;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}