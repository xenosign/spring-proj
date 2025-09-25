package com.tetz.spring_proj.todo.repository;

import com.tetz.spring_proj.todo.domain.TodoEntity;
import com.tetz.spring_proj.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, Long> {

    // 특정 사용자의 모든 할일 조회
    @Query("SELECT t FROM TodoEntity t WHERE t.user = :user ORDER BY t.updatedAt DESC")
    List<TodoEntity> findAllTodosByUser(@Param("user") UserEntity user);

    // 특정 사용자의 미완료된 할일만 조회
    @Query("SELECT t FROM TodoEntity t WHERE t.user = :user AND t.completed = false ORDER BY t.updatedAt DESC")
    List<TodoEntity> findIncompleteTodosByUser(@Param("user") UserEntity user);

    // 특정 사용자의 특정 할일 조회 (수정/완료 처리 시 사용)
    @Query("SELECT t FROM TodoEntity t WHERE t.id = :id AND t.user = :user")
    Optional<TodoEntity> findTodoByIdAndUser(@Param("id") Long id, @Param("user") UserEntity user);
}