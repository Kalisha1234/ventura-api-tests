package com.ventura.api.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerResponse {
    private String id;
    private String shortId;
    private String businessId;
    private String name;
    private String email;
    private String phone;
    private String notes;
    private String createdAt;
    private String updatedAt;
}
