package com.titran.pingcortex.service;

import com.titran.pingcortex.dto.request.CourseRequest;
import com.titran.pingcortex.dto.request.CourseUpdate;
import com.titran.pingcortex.dto.request.CourseUpdateActive;
import com.titran.pingcortex.dto.response.CourseResponse;
import com.titran.pingcortex.repository.CourseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public List<CourseResponse> findAllCourses(UUID userId, Boolean isActive) {
        return courseRepository.findAll(userId, isActive);
    }

    public Optional<CourseResponse> findCourseById(UUID userId, UUID courseId) {
        return courseRepository.findById(userId, courseId);
    }

    public CourseResponse createCourse(UUID userId, CourseRequest courseRequest) {
        return courseRepository.createCourse(userId, courseRequest);
    }

    public Optional<CourseResponse> updateCourse(UUID userId, UUID courseId, CourseUpdate  courseUpdate) {
        return courseRepository.updateCourse(userId, courseId, courseUpdate);
    }

    public Optional<CourseResponse> updateCourseActive(UUID userId, UUID courseId, CourseUpdateActive courseUpdateActive) {
        return courseRepository.updateCourseActive(userId, courseId, courseUpdateActive);
    }
}
