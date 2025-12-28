package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import java.util.HashMap;
import java.util.List;

import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.web.util.DiffPlanarRange;

public class GetHighlightedText implements TaskTool {
    
	private static final String HIGHLIGHT_BEGIN = "[HIGHLIGHT_BEGIN]";

	private static final String HIGHLIGHT_END = "[HIGHLIGHT_END]";

    private static final String OMITTED_LINES = "...OMITTED LINES...";

    private final String filePath;

    private final List<String> fileContent;
    
    private final PlanarRange highlightRange;

    public GetHighlightedText(String filePath, List<String> fileContent, PlanarRange highlightRange) {
        this.filePath = filePath;
        this.fileContent = fileContent;
        this.highlightRange = highlightRange;
    }

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("getHighlightedTextInformation")
            .description("""
                Get information of highlighted text in json format, including highlighted text, 
                file path and content containing highlighted text
                """)
            .build();
    }

    @Override
    public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
        var map = new HashMap<String, Object>();
        map.put("highlightedText", Joiner.on('\n').join(highlightRange.getContent(fileContent)));
        map.put("filePath", filePath);
        map.put("fileContent", Joiner.on('\n').join(highlightRange.getContext(fileContent, HIGHLIGHT_BEGIN, HIGHLIGHT_END, OMITTED_LINES, 0, Integer.MAX_VALUE, Integer.MAX_VALUE)));
        if (highlightRange instanceof DiffPlanarRange diffPlanarRange)
            map.put("oldRevision", diffPlanarRange.isLeftSide());
        map.put("note", """
            1. Highlighted text is between %s and %s in file content
            2. If property 'oldRevision' is available, it means that text is highlighted in a diff context, and this flag 
               should be passed back to tools with this argument when calling them"""
            .formatted(HIGHLIGHT_BEGIN, HIGHLIGHT_END));        
        return new ToolExecutionResult(convertToJson(map), false);
    }

}