package com.titran.pingcortex.dto.response;

public record UsageSummaryResponse(long totalTokensIn, long totalTokensOut, long requestCount, Integer alertThreshold) {
}
