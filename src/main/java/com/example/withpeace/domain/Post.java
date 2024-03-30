package com.example.withpeace.domain;

import com.example.withpeace.type.ETopic;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ETopic type;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Builder
    public Post(User writer, String title, String content, ETopic type) {
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createDate = LocalDateTime.now();
    }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setType(ETopic type) { this.type = type; }
    
}
