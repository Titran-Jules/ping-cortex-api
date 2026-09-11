package com.titran.pingcortex.model;

public record UsageAggregate(long totalTokenIn, long totalTokenOut, long requestCount) {
}
