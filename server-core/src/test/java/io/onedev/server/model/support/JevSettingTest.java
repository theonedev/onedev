package io.onedev.server.model.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import io.onedev.server.model.support.JevSetting.ChoiceQuestion;

class JevSettingTest {

    private HttpClient client(String body) throws Exception {
        var client = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        var response = (HttpResponse<String>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(body);
        when(client.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);
        return client;
    }

    @Test
    void onlyAcceptsValidChoicesAtOrAboveThreshold() throws Exception {
        var questions = new LinkedHashMap<String, ChoiceQuestion>();
        for (var id: new String[]{"boundary", "below", "high", "missing", "text", "range", "invalid", "absent"})
            questions.put(id, new ChoiceQuestion("Select a type", Map.of("bug", "Bug", "feature", "Feature")));
        var client = client("""
            {"answers": {
              "boundary": {"choice": "bug", "confidence": 0.75},
              "below": {"choice": "bug", "confidence": 0.7499},
              "high": {"choice": "feature", "confidence": 1},
              "missing": {"choice": "bug"},
              "text": {"choice": "bug", "confidence": "0.9"},
              "range": {"choice": "bug", "confidence": 1.1},
              "invalid": {"choice": "unknown", "confidence": 0.99}
            }}
            """);
        assertEquals(Map.of("boundary", "bug", "high", "feature"),
                new JevSetting().choose(client, "Issue", questions, 0.75));
        verify(client, times(1)).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void existingChoiceCallDoesNotRequireConfidence() throws Exception {
        var client = client("{\"answers\":{\"selection\":{\"choice\":\"bug\"}}}");
        assertEquals("bug", new JevSetting().choose(client, "Issue", "Select type", Map.of("bug", "Bug")));
    }

    @Test
    void propagatesNetworkFailuresAndSkipsEmptyRequests() throws Exception {
        var client = mock(HttpClient.class);
        var setting = new JevSetting();
        assertTrue(setting.choose(client, "Issue", Map.of(), 0.75).isEmpty());
        verifyNoInteractions(client);
        when(client.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Unavailable"));
        assertThrows(IOException.class, () -> setting.choose(client, "Issue",
                Map.of("type", new ChoiceQuestion("Select type", Map.of("bug", "Bug"))), 0.75));
    }
}
