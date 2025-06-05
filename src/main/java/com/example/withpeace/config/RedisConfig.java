package com.example.withpeace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 설정 클래스
 * - Redis 연결 및 직렬화 방식 설정
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.timeout}")
    private Duration timeout;

    /**
     * Redis 연결을 위한 LettuceConnectionFactory 빈 생성
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 1. Redis 서버 정보 설정
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(host, port);

        // 2. Lettuce 클라이언트 설정 (명령 타임아웃만 적용)
        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .commandTimeout(timeout)       // Redis 명령 타임아웃 적용
                .build();

        // 3. LettuceConnectionFactory 생성
        return new LettuceConnectionFactory(standaloneConfiguration, clientConfiguration);
    }

    /**
     * RedisTemplate<String, String> 빈 등록
     * - 캐시 값은 JSON 문자열로 처리
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // 문자열 기반 키/값 직렬화 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

}
