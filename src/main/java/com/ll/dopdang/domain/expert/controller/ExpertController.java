package com.ll.dopdang.domain.expert.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ll.dopdang.domain.expert.dto.request.ExpertRequestDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertResponseDto;
import com.ll.dopdang.domain.expert.service.ExpertService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/experts")
@RequiredArgsConstructor
public class ExpertController {

    private final ExpertService expertService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createExpert(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody ExpertRequestDto requestDto) {
        Long memberId = customUserDetails.getId();
        expertService.createExpert(requestDto, memberId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "전문가 프로필을 등록하였습니다");
        return new ResponseEntity<>(response, HttpStatus.valueOf(200));
    }

    @GetMapping
    public ResponseEntity<List<ExpertResponseDto>> getExperts(
            @RequestParam(required = false) List<String> categoryNames,
            @RequestParam(required = false) String careerLevel
    ) {
        return new ResponseEntity<>(expertService.getAllExperts(categoryNames,careerLevel), HttpStatus.valueOf(200));
    }
}
