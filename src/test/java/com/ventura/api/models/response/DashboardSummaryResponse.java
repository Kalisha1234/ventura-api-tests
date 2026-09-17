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
public class DashboardSummaryResponse {
    private DashboardRevenueResponse revenue;
    private DashboardInventoryResponse inventory;
    private List<DashboardRecentInvoiceResponse> recentInvoices;
    private List<DashboardDailyRevenueResponse> dailyRevenue;
}
