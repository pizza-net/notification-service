package com.pizzanet.notificationservice.dto;

import java.time.LocalDateTime;

public class OrderStatusChangedEvent {
    private Long orderId;
    private Long userId;
    private String userEmail;
    private String orderStatus;
    private Double totalPrice;
    private LocalDateTime timestamp;

    // Constructors
    public OrderStatusChangedEvent() {
    }

    public OrderStatusChangedEvent(Long orderId, Long userId, String userEmail, String orderStatus, Double totalPrice, LocalDateTime timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.orderStatus = orderStatus;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OrderStatusChangedEvent{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", userEmail='" + userEmail + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", totalPrice=" + totalPrice +
                ", timestamp=" + timestamp +
                '}';
    }
}
