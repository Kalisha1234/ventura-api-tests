package com.ventura.api.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentResponse {
    private String id;
    private String shortId;
    private String businessId;
    private String createdBy;
    private String title;
    private String start;
    private String end;
    private String notes;
    private String location;
    private List<InviteeResponse> invitees;
    private RecurrenceResponse recurrence;
    private String status;
    private String createdAt;
    private String updatedAt;
}
