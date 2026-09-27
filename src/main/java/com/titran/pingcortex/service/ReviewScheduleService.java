package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.response.ReviewScheduleResponse;
import com.titran.pingcortex.repository.ReviewScheduleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReviewScheduleService {
    private final ReviewScheduleRepository reviewScheduleRepository;

    public void applyReviewIfDue(UUID userId, UUID conceptId, int quality) {
        Optional<ReviewScheduleResponse> due = reviewScheduleRepository.findDueSchedule(userId, conceptId);
        if (due.isEmpty()) {
            return;
        }
        ReviewScheduleResponse current = due.get();

        int newRepetitionCount;
        int newIntervalDays;
        if (quality < 3) {
            newRepetitionCount = 0;
            newIntervalDays = 1;
        } else {
            newRepetitionCount = current.repetitionCount() + 1;
            if (current.repetitionCount() == 0) {
                newIntervalDays = 1;
            } else if (current.repetitionCount() == 1) {
                newIntervalDays = 6;
            } else {
                newIntervalDays = (int) Math.round(current.repetitionCount() * current.easinessFactor());
            }
        }

        double newEasinessFactor = Math.max(1.3,
                current.easinessFactor() + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));
        reviewScheduleRepository.updateSchedule(userId, conceptId, newIntervalDays, newEasinessFactor, newRepetitionCount);
    }
}
