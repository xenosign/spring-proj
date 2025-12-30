package com.tetz.spring_proj.tetznight.repository.poll;

import com.tetz.spring_proj.tetznight.domain.poll.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {

    List<Poll> findByIsActiveTrue();

    List<Poll> findByCreatedBy(Long userId);

    List<Poll> findByIsActiveTrueOrderByCreatedAtDesc();

    List<Poll> findByIsActiveTrueOrderByCreatedAtAsc();

    List<Poll> findByCreatedByOrderByCreatedAtDesc(Long userId);

    List<Poll> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(
            LocalDateTime startDate, LocalDateTime endDate);
}