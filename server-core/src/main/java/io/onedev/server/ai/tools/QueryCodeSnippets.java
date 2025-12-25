package io.onedev.server.ai.tools;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ai.ChatTool;
import io.onedev.server.ai.ChatToolUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.CodeSearchService;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.TextQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import io.onedev.server.web.websocket.ChatToolExecution;
import io.onedev.server.web.websocket.ChatToolExecution.Result;

public abstract class QueryCodeSnippets implements ChatTool {

    private static final int PAGE_SIZE = 25;

	private static final int CONTEXT_LINES = 5;

	private static final String MATCHED_CODE_SNIPPET_BEGIN = "[MATCHED_CODE_SNIPPET_BEGIN]";

	private static final String MATCHED_CODE_SNIPPET_END = "[MATCHED_CODE_SNIPPET_END]";

    private static final String OMITTED_LINES = "...OMITTED LINES...";

    
    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("queryCodeSnippets")
            .description("Query code snippets matching specified perl-compatible regular expression and return result in json format")
            .parameters(JsonObjectSchema.builder()
                .addStringProperty("regularExpression").description("Perl-compatible regular expression to query code")
                .addBooleanProperty("caseSensitive").description("Whether to match code snippets case-sensitively")
                .addBooleanProperty("oldRevision").description("Optionally specify whether to query code snippets in old revision in a diff context")
                .addIntegerProperty("currentPage").description("Current page for the query. First page is 1")
                .required("regularExpression", "currentPage").build())
                .build();
    }

    @Override
    public CompletableFuture<Result> execute(IPartialPageRequestHandler handler, JsonNode arguments) {        
        var regularExpression = arguments.get("regularExpression").asText();
        regularExpression = "*";
        var caseSensitive = arguments.get("caseSensitive").asBoolean();
        var oldRevision = arguments.get("oldRevision") != null ? arguments.get("oldRevision").asBoolean() : false;
        var currentPage = arguments.get("currentPage").asInt();					

        var project = getProject();
        var commitId = getCommitId(oldRevision);
        try {
            BlobQuery query = new TextQuery.Builder(regularExpression)
                    .caseSensitive(caseSensitive)
                    .regex(true)
                    .count(currentPage * PAGE_SIZE + 1)
                    .build();
            var textHits = OneDev.getInstance(CodeSearchService.class).search(project, commitId, query);
            int fromIndex = (currentPage - 1) * PAGE_SIZE;
            int toIndex = Math.min(currentPage * PAGE_SIZE, textHits.size());

            List<Map<String, Object>> codeSnippets = new ArrayList<>();
            for (var i=fromIndex; i<toIndex; i++) {
                var textHit = textHits.get(i);
                var hitPos = textHit.getHitPos();
                var blobIdent = OneDev.getInstance(GitService.class).getBlobIdent(project, commitId, textHit.getFilePath());
                var lines = project.getBlob(blobIdent, true).getText().getLines();
                var codeSnippet = Map.of(
                    "filePath", textHit.getFilePath(),
                    "codeSnippet", hitPos.getContent(lines),
                    "contextAroundCodeSnippet", hitPos.getContext(lines, MATCHED_CODE_SNIPPET_BEGIN, MATCHED_CODE_SNIPPET_END, OMITTED_LINES, 0, CONTEXT_LINES, CONTEXT_LINES),
                    "note", String.format("Code snippet is between %s and %s in the context around it", MATCHED_CODE_SNIPPET_BEGIN, MATCHED_CODE_SNIPPET_END)
                );
                codeSnippets.add(codeSnippet);
            }
            var resultData = Map.of(
                "successful", true,
                "codeSnippets", codeSnippets, 
                "hasMorePages", textHits.size() > toIndex);
            return completedFuture(new ChatToolExecution.Result(ChatToolUtils.convertToJson(resultData), false));	
        } catch (RuntimeException e) {
            if (ExceptionUtils.find(e, TooGeneralQueryException.class) != null) {
                var resultData = Map.of(
                    "successful", false, 
                    "failReason", "Regular expression is too short or too general, try a more specific one");
                return completedFuture(new ChatToolExecution.Result(ChatToolUtils.convertToJson(resultData), false));
            } else {
                throw e;
            }
        }
    }

    protected abstract Project getProject();

    protected abstract ObjectId getCommitId(boolean oldRevision);

}
