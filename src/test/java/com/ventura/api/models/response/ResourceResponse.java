package com.ventura.api.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceResponse {
    private String id;
    private String shortId;
    private String businessId;
    private String type;
    private String name;
    private double price;
    private String primaryImage;
    private List<String> supportingImages;
    private String primaryImageKey;
    private List<String> supportingImageKeys;
    private String description;
    private String notes;
    private double availableQuantity;
    private double lowStockThreshold;
    private Map<String, Object> businessHours;
    private String createdAt;
    private String updatedAt;
}
