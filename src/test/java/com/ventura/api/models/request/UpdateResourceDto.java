package com.ventura.api.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateResourceDto {
    private String name;
    private Double price;
    private String primaryImage;
    private List<String> supportingImages;
    private String primaryImageKey;
    private List<String> supportingImageKeys;
    private String description;
    private String notes;
    private Double availableQuantity;
    private Double lowStockThreshold;
    private Map<String, Object> businessHours;
}
