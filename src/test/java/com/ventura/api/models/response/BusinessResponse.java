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
public class BusinessResponse {
    private String id;
    private String shortId;
    private String name;
    private String ownerId;
    private List<String> categories;
    private String description;
    private String tagLine;
    private String logo;
    private String logoKey;
    private String email;
    private String phone;
    private String website;
    private String address;
    private String city;
    private String state;
    private String country;
    private String taxId;
    private String registrationNumber;
    private Map<String, Object> businessHours;
    private Map<String, Object> socials;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;
}
