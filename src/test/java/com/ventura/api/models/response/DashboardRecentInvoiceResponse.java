package com.ventura.api.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardRecentInvoiceResponse {
    private String invoiceId;
    private String invoiceNumber;
    private String customerName;
    private double totalAmount;
    private String status;
    private String createdAt;
}
