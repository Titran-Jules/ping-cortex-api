package com.titran.pingcortex.ai;

import com.titran.pingcortex.exception.UnsupportedProviderException;
import com.titran.pingcortex.model.AiProvider;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AiClientResolver {
    private final Map<String, AiClient> clientsByProvider;

    public AiClientResolver(Map<String, AiClient> clientsByProvider) {
        this.clientsByProvider = clientsByProvider;
    }

    public AiClient resolve(AiProvider provider) {
        AiClient client = clientsByProvider.get(provider.name().toLowerCase());
        if (client == null) {
            throw new UnsupportedProviderException(provider.name());
        }
        return client;
    }
}
