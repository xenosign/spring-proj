package com.tetz.spring_proj.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // JPA Auditing 기능 활성화
    // @CreatedDate, @LastModifiedDate 어노테이션이 동작하도록 함
}
