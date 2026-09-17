package com.ventura.api.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** invoiceType is one of "STANDARD" | "PROFORMA" | "RECEIPT". */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateInvoiceDto {
    private List<String> orderIds;
    private String invoiceType;
    private String dueDate;
    private String notes;
}
