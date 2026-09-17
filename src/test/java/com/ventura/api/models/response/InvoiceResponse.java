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
public class InvoiceResponse {
    private String id;
    private String invoiceNumber;
    private String businessId;
    private List<String> orderIds;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String invoiceType;
    private double subtotal;
    private double vatRate;
    private double vatAmount;
    private double nhilRate;
    private double nhilAmount;
    private double getfundRate;
    private double getfundAmount;
    private double totalTax;
    private double totalAmount;
    private double amountPaid;
    private String status;
    private String paymentMethod;
    private String paymentDate;
    private String issueDate;
    private String dueDate;
    private String sentAt;
    private String notes;
    private String createdAt;
    private String updatedAt;
}
