package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.request.CourseMaterialRequest;
import com.titran.pingcortex.dto.response.CourseMaterialResponse;
import com.titran.pingcortex.exception.CourseNotFoundException;
import com.titran.pingcortex.repository.CourseMaterialRepository;
import com.titran.pingcortex.util.TransactionUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CourseMaterialService {
    private final CourseMaterialRepository courseMaterialRepository;
    private final CourseService courseService;
    private final AnalyzeCoverageService analyzeCoverageService;

    public List<CourseMaterialResponse> findAll(UUID userId, UUID courseId) {
        courseService.findCourseById(userId, courseId).orElseThrow(CourseNotFoundException::new);
        return courseMaterialRepository.findAllByCourseId(courseId);
    }

    public CourseMaterialResponse create(UUID userId, UUID courseId, CourseMaterialRequest courseMaterialRequest) {
        courseService.findCourseById(userId, courseId).orElseThrow(CourseNotFoundException::new);
        CourseMaterialResponse newCourseMaterial = courseMaterialRepository.create(courseId, courseMaterialRequest.content(), courseMaterialRequest.type());
        TransactionUtils.afterCommit(() -> analyzeCoverageService.analyzeCoverage(userId, courseId, newCourseMaterial.id()));
        return newCourseMaterial;
    }
}
