package com.pingpong.service;

import com.pingpong.aspect.RetryOnDbFailure;
import com.pingpong.dto.CreateLeagueRequest;
import com.pingpong.dto.JoinPrivateLeagueRequest;
import com.pingpong.dto.LeagueDetailResponse;
import com.pingpong.dto.LeagueMemberResponse;
import com.pingpong.dto.LeagueResponse;
import com.pingpong.dto.MatchRequest;
import com.pingpong.dto.MatchResponse;
import com.pingpong.model.League;
import com.pingpong.model.LeagueMember;
import com.pingpong.model.Match;
import com.pingpong.model.User;
import com.pingpong.repository.LeagueMemberRepository;
import com.pingpong.repository.LeagueRepository;
import com.pingpong.repository.MatchRepository;
import com.pingpong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@RetryOnDbFailure(maxAttempts = 3, initialDelayMs = 1000, multiplier = 2.0)
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final LeagueMemberRepository leagueMemberRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    private static final List<String> SUPER_ADMIN_EMAILS = List.of(
            "gerald.zhang@gmail.com",
            "markus.koenigreich@gmail.com");

    private boolean isSuperAdmin(User user) {
        return user.getEmail() != null && SUPER_ADMIN_EMAILS.contains(user.getEmail());
    }

    @Transactional
    public LeagueResponse createLeague(CreateLeagueRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        League league = new League();
        league.setName(request.getName());
        league.setDescription(request.getDescription());
        league.setIsPublic(request.getIsPublic());
        league.setCreatedBy(user);

        // Generate invite code for private leagues
        if (!request.getIsPublic()) {
            league.setInviteCode(generateInviteCode());
        }

        league = leagueRepository.save(league);

        // Add creator as owner
        LeagueMember member = new LeagueMember();
        member.setUser(user);
        member.setLeague(league);
        member.setRole("OWNER");
        leagueMemberRepository.save(member);

        // Update in-memory list for response
        league.getMembers().add(member);

        return mapToLeagueResponse(league);
    }

    @Transactional(readOnly = true)
    public List<LeagueResponse> getPublicLeagues() {
        return leagueRepository.findByIsPublic(true).stream()
                .map(this::mapToLeagueResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeagueResponse> getUserLeagues(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LeagueMember> memberships = leagueMemberRepository.findByUserId(user.getId());

        return memberships.stream()
                .map(membership -> mapToLeagueResponse(membership.getLeague()))
                .collect(Collectors.toList());
    }

    @Transactional
    public LeagueResponse joinPublicLeague(Long leagueId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new RuntimeException("League not found"));

        if (!league.getIsPublic()) {
            throw new RuntimeException("This league is private. Use invite code to join.");
        }

        // Check if already a member
        if (leagueMemberRepository.existsByUserIdAndLeagueId(user.getId(), leagueId)) {
            throw new RuntimeException("You are already a member of this league");
        }

        // Add user as member
        LeagueMember member = new LeagueMember();
        member.setUser(user);
        member.setLeague(league);
        member.setRole("MEMBER");
        leagueMemberRepository.save(member);

        // Update in-memory list for response
        league.getMembers().add(member);

        return mapToLeagueResponse(league);
    }

    @Transactional
    public LeagueResponse joinPrivateLeague(JoinPrivateLeagueRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        League league = leagueRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        // Check if already a member
        if (leagueMemberRepository.existsByUserIdAndLeagueId(user.getId(), league.getId())) {
            throw new RuntimeException("You are already a member of this league");
        }

        // Add user as member
        LeagueMember member = new LeagueMember();
        member.setUser(user);
        member.setLeague(league);
        member.setRole("MEMBER");
        leagueMemberRepository.save(member);

        // Update in-memory list for response
        league.getMembers().add(member);

        return mapToLeagueResponse(league);
    }

    @Transactional
    public void leaveLeague(Long leagueId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeagueMember membership = leagueMemberRepository.findByUserIdAndLeagueId(user.getId(), leagueId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this league"));

        if ("OWNER".equals(membership.getRole())) {
            throw new RuntimeException("League owner cannot leave. Transfer ownership or delete the league.");
        }

        leagueMemberRepository.delete(membership);
    }

    @Transactional(readOnly = true)
    public LeagueDetailResponse getLeagueDetails(Long leagueId, String username) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new RuntimeException("League not found"));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all members for this league
        List<LeagueMember> members = leagueMemberRepository.findByLeagueId(leagueId);

        // Get all matches for this league
        List<Match> matches = matchRepository.findByLeagueIdOrderByPlayedAtDesc(leagueId);

        // Calculate stats for all members
        // Initialize stats map
        Map<Long, MemberStats> statsMap = new HashMap<>();
        for (LeagueMember member : members) {
            statsMap.put(member.getUser().getId(), new MemberStats());
        }

        // Process matches to calculate wins, losses, and trends
        for (Match match : matches) {
            Long winnerId = match.getWinner().getId();
            Long loserId = match.getLoser().getId();

            if (statsMap.containsKey(winnerId)) {
                MemberStats stats = statsMap.get(winnerId);
                stats.wins++;
                stats.addTrend('W');
            }

            if (statsMap.containsKey(loserId)) {
                MemberStats stats = statsMap.get(loserId);
                stats.losses++;
                stats.addTrend('L');
            }
        }

        // Build response
        List<LeagueMemberResponse> memberResponses = new ArrayList<>();
        boolean isMember = false;

        for (LeagueMember member : members) {
            if (member.getUser().getId().equals(currentUser.getId())) {
                isMember = true;
            }

            MemberStats stats = statsMap.get(member.getUser().getId());
            memberResponses.add(mapToLeagueMemberResponse(member, stats));
        }

        LeagueDetailResponse response = new LeagueDetailResponse();
        response.setId(league.getId());
        response.setName(league.getName());
        response.setDescription(league.getDescription());
        response.setIsPublic(league.getIsPublic());
        response.setCreatedByUsername(league.getCreatedBy().getUsername());
        response.setCreatedAt(league.getCreatedAt());
        response.setMemberCount(members.size());
        response.setMembers(memberResponses);

        // Only show invite code if user is a member or owner or super admin
        if (isMember || league.getCreatedBy().getId().equals(currentUser.getId()) || isSuperAdmin(currentUser)) {
            response.setInviteCode(league.getInviteCode());
        }

        return response;
    }

    @Transactional
    public void recordMatch(Long leagueId, MatchRequest request, String reporterUsername) {
        // Verify reporter is in the league (optional: could restrict to
        // admins/participants)
        User reporter = userRepository.findByUsername(reporterUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!leagueMemberRepository.existsByUserIdAndLeagueId(reporter.getId(), leagueId) && !isSuperAdmin(reporter)) {
            throw new RuntimeException("You must be a member of the league to record matches");
        }

        // Get winner and loser memberships
        LeagueMember winnerMember = leagueMemberRepository.findByUserIdAndLeagueId(request.getWinnerId(), leagueId)
                .orElseThrow(() -> new RuntimeException("Winner is not in this league"));

        LeagueMember loserMember = leagueMemberRepository.findByUserIdAndLeagueId(request.getLoserId(), leagueId)
                .orElseThrow(() -> new RuntimeException("Loser is not in this league"));

        if (winnerMember.getUser().getId().equals(loserMember.getUser().getId())) {
            throw new RuntimeException("Winner and loser cannot be the same person");
        }

        // Calculate ELO change
        int K = 32;
        double winnerExpected = 1.0 / (1.0 + Math.pow(10.0, (loserMember.getElo() - winnerMember.getElo()) / 400.0));
        double loserExpected = 1.0 / (1.0 + Math.pow(10.0, (winnerMember.getElo() - loserMember.getElo()) / 400.0));

        int winnerNewElo = (int) Math.round(winnerMember.getElo() + K * (1.0 - winnerExpected));
        int loserNewElo = (int) Math.round(loserMember.getElo() + K * (0.0 - loserExpected));

        int winnerChange = winnerNewElo - winnerMember.getElo();
        int loserChange = loserNewElo - loserMember.getElo();

        // Update memberships
        winnerMember.setElo(winnerNewElo);
        loserMember.setElo(loserNewElo);

        leagueMemberRepository.save(winnerMember);
        leagueMemberRepository.save(loserMember);

        // Save match record
        Match match = new Match();
        match.setLeague(winnerMember.getLeague());
        match.setWinner(winnerMember.getUser());
        match.setLoser(loserMember.getUser());
        match.setWinnerEloChange(winnerChange);
        match.setLoserEloChange(loserChange);

        matchRepository.save(match);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getMatches(Long leagueId) {
        return matchRepository.findByLeagueIdOrderByPlayedAtDesc(leagueId).stream()
                .map(match -> new MatchResponse(
                        match.getId(),
                        match.getWinner().getUsername(),
                        match.getLoser().getUsername(),
                        match.getWinnerEloChange(),
                        match.getLoserEloChange(),
                        match.getPlayedAt()))
                .collect(Collectors.toList());
    }

    private LeagueResponse mapToLeagueResponse(League league) {
        LeagueResponse response = new LeagueResponse();
        response.setId(league.getId());
        response.setName(league.getName());
        response.setDescription(league.getDescription());
        response.setIsPublic(league.getIsPublic());
        response.setInviteCode(league.getInviteCode());
        response.setCreatedByUsername(league.getCreatedBy().getUsername());
        response.setCreatedAt(league.getCreatedAt());
        response.setMemberCount((int) leagueMemberRepository.countByLeagueId(league.getId()));
        return response;
    }

    private LeagueMemberResponse mapToLeagueMemberResponse(LeagueMember member, MemberStats stats) {
        LeagueMemberResponse response = new LeagueMemberResponse();
        response.setUserId(member.getUser().getId());
        response.setUsername(member.getUser().getUsername());
        response.setRole(member.getRole());
        response.setElo(member.getElo());
        response.setJoinedAt(member.getJoinedAt());
        response.setWins(stats.wins);
        response.setLosses(stats.losses);
        response.setTrend(stats.trend.toString());
        // Last ELO change logic could be added here if tracked in stats
        return response;
    }

    private String generateInviteCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        // Check if code already exists, regenerate if needed
        if (leagueRepository.findByInviteCode(code.toString()).isPresent()) {
            return generateInviteCode();
        }

        return code.toString();
    }

    // Helper class for calculating member stats
    private static class MemberStats {
        int wins = 0;
        int losses = 0;
        StringBuilder trend = new StringBuilder();

        void addTrend(char result) {
            if (trend.length() < 5) {
                trend.append(result);
            }
        }
    }
}
