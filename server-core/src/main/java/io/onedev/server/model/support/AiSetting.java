package io.onedev.server.model.support;

import java.io.Serializable;

public class AiSetting implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private AiModelSetting modelSetting;

    private boolean entitleToAll = true;

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
    
}
