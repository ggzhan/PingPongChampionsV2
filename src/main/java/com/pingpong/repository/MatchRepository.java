package com.pingpong.repository;

import com.pingpong.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByLeagueIdOrderByPlayedAtDesc(Long leagueId);

    List<Match> findByWinnerIdOrLoserIdOrderByPlayedAtDesc(Long winnerId, Long loserId);
}
