package com.example.withpeace.domain;

import com.example.withpeace.type.EReportType;
import com.example.withpeace.type.EReason;
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
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EReportType type;

    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    private EReason reason;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Builder
    public Report(User writer, Post post, Comment comment, EReportType type, EReason reason) {
        this.writer = writer;
        this.post = post;
        this.comment = comment;
        this.type = type;
        this.reason = reason;
        this.createDate = LocalDateTime.now();
    }
}
