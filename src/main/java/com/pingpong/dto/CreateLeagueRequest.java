package com.pingpong.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeagueRequest {

    @NotBlank(message = "League name is required")
    @Size(min = 3, max = 100, message = "League name must be between 3 and 100 characters")
    private String name;

    private String description;

    private Boolean isPublic = true;
}
