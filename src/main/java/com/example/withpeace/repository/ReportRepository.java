package com.example.withpeace.repository;

import com.example.withpeace.domain.Post;
import com.example.withpeace.domain.Report;
import com.example.withpeace.domain.User;
import com.example.withpeace.type.EReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByWriterAndPostAndType(User user, Post post, EReportType type);
}