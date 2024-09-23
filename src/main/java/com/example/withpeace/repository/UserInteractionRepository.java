package com.example.withpeace.repository;

import com.example.withpeace.domain.User;
import com.example.withpeace.domain.UserInteraction;
import com.example.withpeace.type.EActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
}
