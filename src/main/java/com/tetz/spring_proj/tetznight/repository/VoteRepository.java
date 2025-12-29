package com.tetz.spring_proj.tetznight.repository;

import com.tetz.spring_proj.tetznight.domain.vote.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}
