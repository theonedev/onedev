package io.onedev.server.model.support;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

public class AiSetting implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private AiModelSetting modelSetting;

    private boolean entitleToAll;

    private String systemPrompt;

    public AiModelSetting getModelSetting() {
        return modelSetting;
    }

    public void setModelSetting(AiModelSetting modelSetting) {
        this.modelSetting = modelSetting;
    }

    public boolean isEntitleToAll() {
        return entitleToAll;
    }

    public void setEntitleToAll(boolean entitleToAll) {
        this.entitleToAll = entitleToAll;
    }

    @Nullable
    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(@Nullable String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

}
