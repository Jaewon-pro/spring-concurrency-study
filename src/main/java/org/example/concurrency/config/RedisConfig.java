package org.example.concurrency.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                redisProperties.getHost(), redisProperties.getPort());

        String password = redisProperties.getPassword();

        if (hasText(password)) {
            config.setPassword(password);
        }
        return new LettuceConnectionFactory(config);
    }

}
