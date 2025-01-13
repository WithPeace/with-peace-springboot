package com.example.withpeace.domain;

import com.example.withpeace.type.ECommentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BalanceGame game;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ECommentType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User writer;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Builder
    public Comment(Post post, BalanceGame game, ECommentType type, User writer, String content) {
        this.post = post;
        this.game = game;
        this.type = type;
        this.writer = writer;
        this.content = content;
        this.createDate = LocalDateTime.now();
    }

}
