package com.titran.pingcortex.dto.response;

public record ReviewScheduleResponse(int intervalDays, double easinessFactor, int repetitionCount) {
}
