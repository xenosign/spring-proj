package com.tetz.spring_proj.analytics.repository.ui;

import com.tetz.spring_proj.analytics.domain.ui.UiTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UiTestRepository extends JpaRepository<UiTestEntity, Long> {}
