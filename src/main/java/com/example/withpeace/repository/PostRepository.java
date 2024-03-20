package com.example.withpeace.repository;

import com.example.withpeace.domain.Post;

import com.example.withpeace.type.ETopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT p FROM Post p WHERE p.type=:type ORDER BY p.createDate DESC")
    Page<Post> findByType(ETopic type, Pageable pageable);
}
