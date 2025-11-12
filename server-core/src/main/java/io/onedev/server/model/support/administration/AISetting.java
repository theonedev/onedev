package io.onedev.server.model.support.administration;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import dev.langchain4j.model.chat.ChatModel;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.AIModelSetting;

@Editable
public class AISetting implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PROP_LITE_MODEL_SETTING = "liteModelSetting";
    
    private AIModelSetting liteModelSetting;
    
    @Editable(order=100)
    @Nullable
    public AIModelSetting getLiteModelSetting() {
        return liteModelSetting;
    }

    public void setLiteModelSetting(AIModelSetting liteModelSetting) {
        this.liteModelSetting = liteModelSetting;
    }

    @Nullable
    public ChatModel getLiteModel() {
        return liteModelSetting != null ? liteModelSetting.getChatModel() : null;
    }

}
