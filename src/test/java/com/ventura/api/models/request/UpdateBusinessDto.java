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
public class UpdateBusinessDto {
    private String name;
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
}
