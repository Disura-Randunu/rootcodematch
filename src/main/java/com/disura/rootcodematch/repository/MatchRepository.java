package com.disura.rootcodematch.repository;

import com.disura.rootcodematch.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
