package com.pingpong.repository;

import com.pingpong.model.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    List<League> findByIsPublic(Boolean isPublic);

    Optional<League> findByInviteCode(String inviteCode);
}
