package com.pingpong.repository;

import com.pingpong.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByLeagueIdOrderByPlayedAtDesc(Long leagueId);

    List<Match> findByWinnerIdOrLoserIdOrderByPlayedAtDesc(Long winnerId, Long loserId);

    @org.springframework.data.jpa.repository.Query("SELECT m FROM Match m WHERE m.league.id = :leagueId AND (m.winner.id = :userId OR m.loser.id = :userId) ORDER BY m.playedAt DESC")
    List<Match> findByLeagueIdAndUserIdOrderByPlayedAtDesc(Long leagueId, Long userId);
}
