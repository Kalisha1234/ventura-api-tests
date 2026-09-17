package com.ventura.api.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** paymentMethod is one of "CASH" | "MOBILE_MONEY" | "BANK_TRANSFER" | "CARD" | "CHEQUE". */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordPaymentDto {
    private Double amount;
    private String paymentMethod;
    private String paymentDate;
}
