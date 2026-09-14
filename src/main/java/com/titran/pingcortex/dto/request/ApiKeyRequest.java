package com.titran.pingcortex.dto.request;

import com.titran.pingcortex.model.AiProvider;

public record ApiKeyRequest(AiProvider provider, String apiKey) {
}
