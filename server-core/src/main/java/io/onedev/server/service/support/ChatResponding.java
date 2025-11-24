package io.onedev.server.service.support;

import org.jspecify.annotations.Nullable;

public interface ChatResponding {

    @Nullable
    String getContent();

    void cancel();
    
}