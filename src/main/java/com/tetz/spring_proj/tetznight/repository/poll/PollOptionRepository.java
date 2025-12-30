package com.tetz.spring_proj.tetznight.repository.poll;

import com.tetz.spring_proj.tetznight.domain.poll.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    List<PollOption> findByPollIdOrderByDisplayOrder(Long pollId);

    boolean existsByIdAndPollId(Long optionId, Long pollId);
}