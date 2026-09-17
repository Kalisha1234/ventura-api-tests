package com.ventura.api.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {
    private String id;
    private String orderNumber;
    private String businessId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private List<OrderItemResponse> items;
    private double totalAmount;
    private String status;
    private String invoiceId;
    private String createdAt;
    private String updatedAt;
}
