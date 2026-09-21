package io.onedev.server.model.support;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.rest.annotation.Api;
import nl.altindag.ssl.SSLFactory;

@Editable
public class JevSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_BASE_URL = "https://api.typesafe.ai/v1";

    @Api(order=100, description="Base URL of the Jev API, including any custom port")
    private String baseUrl = DEFAULT_BASE_URL;

    @Api(order=200, description="Jev API key for authentication")
    private String apiKey;

    @Api(order=300, description="Timeout in seconds to get model response")
    private int timeoutSeconds = 30;

    @Editable(order=100, name="Base URL", description="Base URL of the Jev API, including any custom port. "
        + "Defaults to https://api.typesafe.ai/v1. The /systemone endpoint is appended automatically")
    @NotEmpty
    @Pattern(regexp="https?://.+", message="Base URL should be a valid http/https URL")
    public String getBaseUrl() {
        // Settings saved before baseUrl was introduced do not contain this field.
        return baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Editable(order=200, name="API Key", description="Specify your Jev API key for authentication")
    @Password
    @NotEmpty
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Editable(order=300, name="Timeout", description="Specify how long to wait for the model response in seconds")
    @Min(value=5, message="Timeout should be at least 5 seconds")
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String choose(String state, String instructions, Map<String, String> criteria) throws IOException {
        return choose(newClient(), state, instructions, criteria);
    }

    private HttpClient newClient() {
        return HttpClient.newBuilder()
            .sslContext(OneDev.getInstance(SSLFactory.class).getSslContext())
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .build();
    }

    String choose(HttpClient client, String state, String instructions, Map<String, String> criteria) throws IOException {
        var questions = Map.of("selection", Map.of(
            "type", "choice", "instructions", instructions, "criteria", criteria));
        var result = ask(client, state, questions);
        var choice = result.path("answers").path("selection").path("choice");
        if (!choice.isTextual() || !criteria.containsKey(choice.textValue()))
            throw new ExplicitException("Jev returned an invalid choice");
        return choice.textValue();
    }

    public Map<String, String> choose(String state, Map<String, ChoiceQuestion> questions,
                                     double minimumConfidence) throws IOException {
        return choose(newClient(), state, questions, minimumConfidence);
    }

    Map<String, String> choose(HttpClient client, String state, Map<String, ChoiceQuestion> questions,
                               double minimumConfidence) throws IOException {
        var requestQuestions = new LinkedHashMap<String, Object>();
        questions.forEach((id, question) -> requestQuestions.put(id, Map.of(
            "type", "choice", "instructions", question.instructions(), "criteria", question.criteria())));
        var selections = new LinkedHashMap<String, String>();
        if (!questions.isEmpty()) {
            var answers = ask(client, state, requestQuestions).path("answers");
            questions.forEach((id, question) -> {
                var answer = answers.path(id);
                var choice = answer.path("choice");
                var confidence = answer.path("confidence");
                if (choice.isTextual() && question.criteria().containsKey(choice.textValue())
                        && confidence.isNumber() && confidence.doubleValue() >= minimumConfidence
                        && confidence.doubleValue() <= 1)
                    selections.put(id, choice.textValue());
            });
        }
        return selections;
    }

    private JsonNode ask(HttpClient client, String state, Map<String, ?> questions) throws IOException {
        var objectMapper = new ObjectMapper();
        var body = Map.of("model", "jev-latest", "state", state, "questions", questions);
        var baseUrl = getBaseUrl();
        var request = HttpRequest.newBuilder(URI.create(
            (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/") + "systemone"))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
            .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for Jev response", e);
        }
        if (response.statusCode() != 200)
            throw new ExplicitException("Jev request failed (HTTP " + response.statusCode() + ")");
        var result = objectMapper.readTree(response.body());
        if (result == null)
            throw new ExplicitException("Jev returned an empty response");
        return result;
    }

    public record ChoiceQuestion(String instructions, Map<String, String> criteria) {
    }

}
