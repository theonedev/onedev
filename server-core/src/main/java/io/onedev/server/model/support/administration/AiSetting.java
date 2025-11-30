package io.onedev.server.model.support.administration;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import dev.langchain4j.model.chat.ChatModel;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.AiModelSetting;

@Editable
public class AiSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PROP_LITE_MODEL_SETTING = "liteModelSetting";
    
    private AiModelSetting liteModelSetting;
    
    @Editable(order=100)
    @Nullable
    public AiModelSetting getLiteModelSetting() {
        return liteModelSetting;
    }

    public void setLiteModelSetting(AiModelSetting liteModelSetting) {
        this.liteModelSetting = liteModelSetting;
    }

    @Nullable
    public ChatModel getLiteModel() {
        return liteModelSetting != null ? liteModelSetting.getChatModel() : null;
    }

}
