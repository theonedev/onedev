package io.onedev.server.model.support;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.util.EditContext;

@Editable(order=100)
public class AiModelSetting implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private static final int MODEL_LIST_TIMEOUT_SECONDS = 30;

    private static final Logger logger = LoggerFactory.getLogger(AiModelSetting.class);

    private String baseUrl;

    private String apiKey;

    private String name;

    private int timeoutSeconds = 30;

    @Editable(order=200, name="Base URL", placeholder="https://api.openai.com/v1", description="""
        Base URL of <b class='text-info'>OpenAI compatible</b> API endpoint. Leave empty to use OpenAI official endpoint. 
        <b class='text-danger'>NOTE:</b> Make sure base URL specified here supports HTTP/2 connection. HTTP/1.1 is not 
        supported and will get a connection timeout error""")
    @Pattern(regexp="https?://.+", message="Base URL should be a valid http/https URL")
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Editable(order=300, name="API Key", description="Optionally specify API key for authentication")
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Editable(order=400, name="Name", description="""
        Specify the model name to use. <b class='text-danger'>NOTE: </b> Right now OneDev only supports 
        models with chat completions API. OpenAI models with the new responses API is not supported yet""")
    @ChoiceProvider("getModels")
    @NotEmpty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Editable(order=500, name="Timeout", description="Specify how long to wait for the model response in seconds")
    @Min(value = 5, message = "Timeout should be at least 5 seconds")
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @SuppressWarnings("unused")
    private static List<String> getModels() {
        var baseUrl = (String) EditContext.get().getInputValue("baseUrl");
        if (baseUrl == null)
            baseUrl = "https://api.openai.com/v1";

        var apiKey = (String) EditContext.get().getInputValue("apiKey");
        
        try {
            var modelsUrl = baseUrl.endsWith("/") ? baseUrl + "models" : baseUrl + "/models";
            
            HttpClient client = HttpClient.newBuilder().build();

            var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(modelsUrl))
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(MODEL_LIST_TIMEOUT_SECONDS));
            if (apiKey != null) 
                requestBuilder.header("Authorization", "Bearer " + apiKey);

            HttpRequest request = requestBuilder.build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                var models = new ArrayList<String>();
                var jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                var dataArray = jsonResponse.getAsJsonArray("data");
                
                for (var element : dataArray) {
                    models.add(element.getAsJsonObject().get("id").getAsString());
                }
                
                models.sort(String::compareTo);
                return models;
            } else {
                logger.error("Error getting models (status code: {}, response body: {})", 
                        response.statusCode(), response.body());
                return List.of("<Error getting models, check server log for details>");
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error getting models", e);
            return List.of("<Error getting models, check server log for details>");
        }
    }

    public ChatModel getChatModel() {
        return OpenAiChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(name)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .build(); 
    }

    public StreamingChatModel getStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(name)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .build();
    }

}
