package com.pingpong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {
    private Long id;
    private String winnerUsername;
    private String loserUsername;
    private Integer winnerEloChange;
    private Integer loserEloChange;
    private LocalDateTime playedAt;
}
