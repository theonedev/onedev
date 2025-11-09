package io.onedev.server.web.behavior.inputassist;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import io.onedev.commons.utils.ExplicitException;

public abstract class NaturalLanguageTranslator {

    private static final int MAX_QUERY_LENGTH = 512;

    private final ChatModel model;

    public NaturalLanguageTranslator(ChatModel model) {
        this.model = model;
    }

    public abstract String getQueryDescription();
    
    public String translate(String naturalLanguage) {
        if (naturalLanguage.length() > MAX_QUERY_LENGTH) {
            throw new ExplicitException("Query is too long. Max " + MAX_QUERY_LENGTH + " characters");
        }
        var systemMessage = new SystemMessage("""
            You are a query translator that converts natural language into structured query string described as below:

            """ + getQueryDescription() + """


            Also note that the user input might also be a structure query but with some syntax errors. In that case, 
            fix syntax errors and return the corrected query string.

            IMPORTANT: only structured query string should be returned, no other text or comments.
            """);
            
        var userMessage = new UserMessage(naturalLanguage);
        
        return model.chat(systemMessage, userMessage).aiMessage().text();
    }

}
