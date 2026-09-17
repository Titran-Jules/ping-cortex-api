package com.titran.pingcortex.service;

import com.titran.pingcortex.ai.AbstractAiClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseAnalyzeService {
    private final AbstractAiClient aiClient;


}
