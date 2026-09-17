package com.ventura.api.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Deliberately does NOT use {@code @JsonInclude(NON_NULL)}: the API treats an explicit
 * {@code "lastName": null} as "clear the last name", which the suite needs to be able to send.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLastNameDto {
    private String lastName;
}
