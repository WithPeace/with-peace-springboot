package com.example.withpeace.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimeFormatter {

    public static String timeFormat(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(time, now);

        if(duration.getSeconds() < 60) {
            return duration.getSeconds() + "초 전";
        } else if(duration.toMinutes() < 60) {
            return duration.toMinutes() + "분 전";
        } else if(duration.toHours() < 24) {
            return duration.toHours() + "시간 전";
        } else if(duration.toDays() < 7) {
            return duration.toDays() + "일 전";
        } else {
            if (time.getYear() != now.getYear()) {
                return time.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));
            } else {
                return time.format(DateTimeFormatter.ofPattern("M월 d일"));
            }
        }
    }
}