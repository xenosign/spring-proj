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
    @Query("SELECT t FROM TodoEntity t WHERE t.user = :user ORDER BY t.createdAt DESC")
    List<TodoEntity> findAllTodosByUser(@Param("user") UserEntity user);

    @Query("SELECT t FROM TodoEntity t WHERE t.user = :user AND t.completed = false ORDER BY t.createdAt DESC")
    List<TodoEntity> findIncompleteTodosByUser(@Param("user") UserEntity user);

    @Query("SELECT t FROM TodoEntity t WHERE t.id = :id AND t.user = :user")
    Optional<TodoEntity> findTodoByIdAndUser(@Param("id") Long id, @Param("user") UserEntity user);
}