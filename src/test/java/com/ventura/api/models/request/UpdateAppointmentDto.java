package com.ventura.api.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateAppointmentDto {
    private String title;
    private String start;
    private String end;
    private String notes;
    private String location;
    private List<InviteeDto> invitees;
    private RecurrenceDto recurrence;
    private Boolean clearRecurrence;
}
