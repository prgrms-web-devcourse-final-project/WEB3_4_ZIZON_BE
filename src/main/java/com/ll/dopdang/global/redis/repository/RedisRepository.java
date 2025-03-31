package com.ll.dopdang.global.redis.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * RedisRepository
 */
@Component
@RequiredArgsConstructor
public class RedisRepository {
	/**
	 * redis 템플릿
	 */
	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * redis에 저장
	 * @param key key
	 * @param value value
	 * @param duration 저장 기간
	 * @param timeUnit 시간 단위
	 */
	public void save(String key, Object value, Long duration, TimeUnit timeUnit) {
		redisTemplate.opsForValue().set(key, value, duration, timeUnit);
	}

	/**
	 * redis에서 값 가져오기
	 * @param key 키
	 * @return {@link Object}
	 */
	public Object get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	/**
	 * redis에서 값 제거
	 * @param key 키
	 */
	public void remove(String key) {
		redisTemplate.delete(key);
	}
}
