package com.ventura.api.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Both fields are required by the spec (send null explicitly to clear the avatar). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvatarDto {
    private String avatarUrl;
    private String avatarKey;
}
