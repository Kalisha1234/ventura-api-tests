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
public class SearchResultsResponse {
    private String query;
    private List<CustomerResponse> customers;
    private List<ResourceResponse> resources;
    private List<OrderResponse> orders;
    private List<InvoiceResponse> invoices;
    private List<AppointmentResponse> appointments;
}
