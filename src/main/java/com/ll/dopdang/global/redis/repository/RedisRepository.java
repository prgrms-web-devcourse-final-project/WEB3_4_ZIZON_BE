package com.ll.dopdang.global.redis.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisRepository {
	private final RedisTemplate<String, Object> redisTemplate;

	public void save(String key, Object value, Long duration, TimeUnit timeUnit) {
		redisTemplate.opsForValue().set(key, value, duration, timeUnit);
	}

	public Object get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public void remove(String key) {
		redisTemplate.delete(key);
	}
}
