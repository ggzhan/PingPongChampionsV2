package com.pingpong.controller;

import com.pingpong.dto.CreateLeagueRequest;
import com.pingpong.dto.JoinPrivateLeagueRequest;
import com.pingpong.dto.LeagueDetailResponse;
import com.pingpong.dto.LeagueResponse;
import com.pingpong.dto.MatchRequest;
import com.pingpong.dto.MatchResponse;
import com.pingpong.service.LeagueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeagueController {

    private final LeagueService leagueService;

    @PostMapping
    public ResponseEntity<LeagueResponse> createLeague(
            @Valid @RequestBody CreateLeagueRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            LeagueResponse response = leagueService.createLeague(request, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<LeagueResponse>> getPublicLeagues() {
        List<LeagueResponse> leagues = leagueService.getPublicLeagues();
        return ResponseEntity.ok(leagues);
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeagueResponse>> getMyLeagues(Authentication authentication) {
        String username = authentication.getName();
        List<LeagueResponse> leagues = leagueService.getUserLeagues(username);
        return ResponseEntity.ok(leagues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueDetailResponse> getLeagueDetails(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            LeagueDetailResponse response = leagueService.getLeagueDetails(id, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<LeagueResponse> joinPublicLeague(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            LeagueResponse response = leagueService.joinPublicLeague(id, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/join-private")
    public ResponseEntity<LeagueResponse> joinPrivateLeague(
            @Valid @RequestBody JoinPrivateLeagueRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            LeagueResponse response = leagueService.joinPrivateLeague(request, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveLeague(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            leagueService.leaveLeague(id, username);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/matches")
    public ResponseEntity<?> recordMatch(
            @PathVariable Long id,
            @Valid @RequestBody MatchRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            leagueService.recordMatch(id, request, username);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<MatchResponse>> getMatches(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.getMatches(id));
    }
}
