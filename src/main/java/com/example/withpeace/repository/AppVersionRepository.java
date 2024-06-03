package com.example.withpeace.repository;

import com.example.withpeace.domain.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

    AppVersion findFirstByOrderByIdAsc();

}
