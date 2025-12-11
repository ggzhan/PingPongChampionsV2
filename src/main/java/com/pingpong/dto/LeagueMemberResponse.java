package com.pingpong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueMemberResponse {

    private Long userId;
    private String username;
    private String role;
    private Integer elo;
    private Integer wins;
    private Integer losses;
    private Integer lastEloChange;
    private String trend;
    private LocalDateTime joinedAt;
}
