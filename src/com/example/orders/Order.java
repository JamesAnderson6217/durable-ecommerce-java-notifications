package com.example.orders;

public record Order(String id, String accountId, String status, String receiptUrl) {}
