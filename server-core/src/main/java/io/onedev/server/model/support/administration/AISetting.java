package io.onedev.server.model.support.administration;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import dev.langchain4j.model.chat.ChatModel;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.AIModelSetting;

@Editable
public class AISetting implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private AIModelSetting naturalLanguageQueryModelSetting;
    
    @Editable(order=100, name="Natural Language Query Model", description=
        """
        If specified, one will be able to query issues, pull requests and builds via natural language. Suggested models in terms of performance and cost for this task:
        <ul>
            <li>Google/gemini-2.5-flash</li>
            <li>OpenAI/gpt-4.1-mini</li>
            <li>Qwen/Qwen-2.5-72B-instruct</li>
        </ul>
        """)
    @Nullable
    public AIModelSetting getNaturalLanguageQueryModelSetting() {
        return naturalLanguageQueryModelSetting;
    }

    public void setNaturalLanguageQueryModelSetting(AIModelSetting naturalLanguageQueryModelSetting) {
        this.naturalLanguageQueryModelSetting = naturalLanguageQueryModelSetting;
    }

    @Nullable
    public ChatModel getNaturalLanguageQueryModel() {
        return naturalLanguageQueryModelSetting != null ? naturalLanguageQueryModelSetting.getChatModel() : null;
    }

}
