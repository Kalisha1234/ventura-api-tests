package com.ventura.api.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    private String id;
    private String shortId;
    private String firstName;
    private String lastName;
    private String email;
    private String googleId;
    private String avatarUrl;
    private String avatarKey;
    private String businessId;
    private boolean isSystem;
    private boolean isActive;
    private boolean isEmailVerified;
    private boolean deleted;
    private String deletedAt;
    private String createdAt;
    private String updatedAt;
}
