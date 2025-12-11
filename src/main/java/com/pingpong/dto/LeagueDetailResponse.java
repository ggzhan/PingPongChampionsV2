package com.pingpong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueDetailResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean isPublic;
    private String inviteCode;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private Integer memberCount;
    private List<LeagueMemberResponse> members;
}
