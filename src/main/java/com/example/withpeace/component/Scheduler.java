package com.example.withpeace.component;

import com.example.withpeace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class Scheduler {
    private final UserRepository userRepository;

    // 00시 00분 00초에 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void withdrawalUsers(){
        userRepository.deleteUsersByDeleteDate(LocalDate.now().minusDays(14));
    }
}

