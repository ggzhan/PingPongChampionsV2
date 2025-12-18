package com.pingpong.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlayerStatsResponse {
    private LeagueMemberResponse playerInfo;
    private List<MatchResponse> matchHistory;
}
