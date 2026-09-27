package com.titran.pingcortex.controller;

import com.titran.pingcortex.dto.response.ReviewTodayResponse;
import com.titran.pingcortex.security.CurrentUser;
import com.titran.pingcortex.service.ReviewScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ReviewController {
    private final ReviewScheduleService reviewScheduleService;

    @GetMapping("/reviews/today")
    public ResponseEntity<List<ReviewTodayResponse>> findAllToday(HttpServletRequest request) {
        UUID userId = CurrentUser.id(request);
        return ResponseEntity.ok(reviewScheduleService.findAllDueToday(userId));
    }
}
