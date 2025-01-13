package com.example.withpeace.repository;

import com.example.withpeace.domain.BalanceGame;
import com.example.withpeace.domain.Comment;
import com.example.withpeace.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.post = :post")
    List<Comment> findCommentsByPost(Post post);

    @EntityGraph(attributePaths = {"writer"})
    @Query("SELECT c FROM Comment c WHERE c.game = :game")
    List<Comment> findCommentsWithWriterByBalanceGame(BalanceGame game);

}
