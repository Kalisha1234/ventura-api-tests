package com.ventura.api.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SetupStatusResponse {
    private boolean hasBusiness;
    private boolean hasCustomers;
    private boolean hasResources;
    private boolean hasOrders;
    private boolean hasInvoices;
    private boolean hasAppointments;
    private boolean complete;
}
