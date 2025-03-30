package com.ll.dopdang.global.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class ResponseAspect {
	private final HttpServletResponse response;

	@Around("""
		(
		    within
		    (
		        @org.springframework.web.bind.annotation.RestController *
		    )
		    &&
		    (
		        @annotation(org.springframework.web.bind.annotation.GetMapping)
		        ||
		        @annotation(org.springframework.web.bind.annotation.PostMapping)
		        ||
		        @annotation(org.springframework.web.bind.annotation.PutMapping)
		        ||
		        @annotation(org.springframework.web.bind.annotation.DeleteMapping)
		        ||
		        @annotation(org.springframework.web.bind.annotation.RequestMapping)
		    )
		)
		||
		@annotation(org.springframework.web.bind.annotation.ResponseBody)
		""")
	public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
		Object proceed = joinPoint.proceed();

		if (proceed instanceof ResponseEntity<?> rsData) {
			response.setStatus(rsData.getStatusCode().value());
		}

		return proceed;
	}
}
