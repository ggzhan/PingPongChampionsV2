package com.pingpong.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequest {

    @NotNull(message = "Winner ID is required")
    private Long winnerId;

    @NotNull(message = "Loser ID is required")
    private Long loserId;
}
