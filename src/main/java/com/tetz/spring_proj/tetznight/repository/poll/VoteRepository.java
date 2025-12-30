package com.tetz.spring_proj.tetznight.repository.poll;

import com.tetz.spring_proj.tetznight.domain.poll.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByPollIdAndUserId(Long pollId, Long userId);

    boolean existsByPollIdAndUserId(Long pollId, Long userId);

    long countByPollId(Long pollId);

    long countByPollOptionId(Long pollOptionId);

    List<Vote> findByPollId(Long pollId);

    @Query("SELECT v FROM Vote v WHERE v.pollOption.id = :optionId ORDER BY v.votedAt DESC")
    List<Vote> findByPollOptionIdOrderByVotedAtDesc(@Param("optionId") Long optionId);
}