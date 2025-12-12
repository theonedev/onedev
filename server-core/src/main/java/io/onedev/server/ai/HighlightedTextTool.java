package io.onedev.server.ai;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.service.support.ChatTool;

public class HighlightedTextTool implements ChatTool {
    
	private static final String HIGHLIGHT_BEGIN = "[HIGHLIGHT_BEGIN]";

	private static final String HIGHLIGHT_END = "[HIGHLIGHT_END]";

    private static final String OMITTED_LINES = "...OMITTED LINES...";

    private static final int CONTEXT_SIZE = 100;

    private final String fileName;

    private final List<String> fileContent;
    
    private final PlanarRange highlightRange;

    public HighlightedTextTool(String fileName, List<String> fileContent, PlanarRange highlightRange) {
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.highlightRange = highlightRange;
    }

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("getHighlightedTextInformation")
            .description("""
                Get information of highlighted text in json format, including highlighted text itself, 
                name of the file containing highlighted text, and context of highlighted text
                """)
            .build();
    }

    @Override
    public String execute(JsonNode arguments) {
        var map = Map.of(
            "highlightedText", Joiner.on('\n').join(highlightRange.getContent(fileContent)),
            "nameOfFileContainingHighlightedText", fileName,
            "contextOfHighlightedText", Joiner.on('\n').join(highlightRange.getContext(fileContent, HIGHLIGHT_BEGIN, HIGHLIGHT_END, OMITTED_LINES, CONTEXT_SIZE, CONTEXT_SIZE, CONTEXT_SIZE)),
            "note", String.format("Highlighted text is between %s and %s in the context", HIGHLIGHT_BEGIN, HIGHLIGHT_END)
        );

        try {
            return OneDev.getInstance(ObjectMapper.class).writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}