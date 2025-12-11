package com.pingpong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean isPublic;
    private String inviteCode;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private Integer memberCount;
}
