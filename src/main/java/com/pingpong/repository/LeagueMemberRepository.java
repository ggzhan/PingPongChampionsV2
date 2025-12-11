package com.pingpong.repository;

import com.pingpong.model.LeagueMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueMemberRepository extends JpaRepository<LeagueMember, Long> {

    List<LeagueMember> findByUserId(Long userId);

    List<LeagueMember> findByLeagueId(Long leagueId);

    boolean existsByUserIdAndLeagueId(Long userId, Long leagueId);

    Optional<LeagueMember> findByUserIdAndLeagueId(Long userId, Long leagueId);
}
