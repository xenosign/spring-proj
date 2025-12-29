package com.tetz.spring_proj.tetznight.domain.vote;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll_options")
@Getter
@Setter
@NoArgsConstructor
public class PollOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(nullable = false, length = 100)
    private String optionText; // "참석", "불참석", "미정"

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "pollOption", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<>();

    // 투표 수 계산
    public int getVoteCount() {
        return votes.size();
    }
}
