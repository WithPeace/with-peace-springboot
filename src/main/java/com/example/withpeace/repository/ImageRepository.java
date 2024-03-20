package com.example.withpeace.repository;

import com.example.withpeace.domain.Image;
import com.example.withpeace.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i.url FROM Image i WHERE i.post = :post")
    List<String> findUrlsByPostId(Post post);

    @Query(value = "SELECT i.url FROM images i WHERE i.post_id = :postId ORDER BY i.id ASC LIMIT 1",
            nativeQuery = true)
    Optional<String> findUrlsByPostIdOrderByIdAsc(@Param("postId") Long postId);

}
