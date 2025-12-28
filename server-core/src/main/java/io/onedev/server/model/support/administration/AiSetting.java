package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.validation.constraints.Min;

import org.jspecify.annotations.Nullable;

import dev.langchain4j.model.chat.ChatModel;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.model.support.AiModelSetting;

@Editable
public class AiSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PROP_LITE_MODEL_SETTING = "liteModelSetting";
    
    public static final String PROP_CHAT_PRESERVE_DAYS = "chatPreserveDays";
    
    private AiModelSetting liteModelSetting;
    
    private int chatPreserveDays = 30;
    
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

    @Editable(order=200)
    @Min(value = 1, message = "At least 1 day should be specified")
    @OmitName
    public int getChatPreserveDays() {
        return chatPreserveDays;
    }

    public void setChatPreserveDays(int chatPreserveDays) {
        this.chatPreserveDays = chatPreserveDays;
    }

}
