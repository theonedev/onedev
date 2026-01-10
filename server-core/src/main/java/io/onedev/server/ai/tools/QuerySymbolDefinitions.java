package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import java.util.Map;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.server.OneDev;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.ai.ToolUtils;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.search.code.CodeSearchService;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.SymbolQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;

public abstract class QuerySymbolDefinitions implements TaskTool {

    private static final int PAGE_SIZE = 25;

    private final boolean inDiffContext;

    public QuerySymbolDefinitions(boolean inDiffContext) {
        this.inDiffContext = inDiffContext;
    }

    @Override
    public ToolSpecification getSpecification() {
        var paramsBuilder = JsonObjectSchema.builder()
            .addStringProperty("symbolNamePattern").description("Symbol name pattern to query definitions (supports wildcards * and ?)")
            .addBooleanProperty("caseSensitive").description("Whether to match symbol names case-sensitively");
        
        if (inDiffContext) 
            paramsBuilder.addBooleanProperty("oldRevision").description("Specify whether to query definitions from old revision");

        paramsBuilder
            .addIntegerProperty("currentPage").description("Current page for the query. First page is 1")
            .required(ToolUtils.required(inDiffContext, "symbolNamePattern", "caseSensitive", "currentPage"));
        return ToolSpecification.builder()
            .name("querySymbolDefinitions")
            .description("Query definitions of specified symbol name pattern (supports wildcards * and ?) and return result in json format")
            .parameters(paramsBuilder.build())
            .build();
    }

    @Override
    public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
        var project = OneDev.getInstance(ProjectService.class).load(getProjectId());
        if (!SecurityUtils.canReadCode(subject, project))
            throw new UnauthorizedException();

        if (arguments.get("symbolNamePattern") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'symbolNamePattern' is required")), false);
        var symbolNamePattern = arguments.get("symbolNamePattern").asText();
        if (arguments.get("caseSensitive") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'caseSensitive' is required")), false);
        var caseSensitive = arguments.get("caseSensitive").asBoolean();
        if (inDiffContext && arguments.get("oldRevision") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'oldRevision' is required")), false);
        var oldRevision = arguments.get("oldRevision") != null ? arguments.get("oldRevision").asBoolean() : false;
        if (arguments.get("currentPage") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'currentPage' is required")), false);
        var currentPage = arguments.get("currentPage").asInt();

        try {
            BlobQuery query = new SymbolQuery.Builder(symbolNamePattern)
                    .caseSensitive(caseSensitive)
                    .local(false)
                    .count(currentPage * PAGE_SIZE + 1)
                    .build();
            var symbolHits = OneDev.getInstance(CodeSearchService.class).search(project, getCommitId(oldRevision), query);
            int fromIndex = (currentPage - 1) * PAGE_SIZE;
            int toIndex = Math.min(currentPage * PAGE_SIZE, symbolHits.size());        
            var result = Map.of(	
                "successful", true,
                "definitions", symbolHits.subList(fromIndex, toIndex), 
                "hasMorePages", symbolHits.size() > toIndex);
            return new ToolExecutionResult(convertToJson(result), false);
        } catch (RuntimeException e) {
            if (ExceptionUtils.find(e, TooGeneralQueryException.class) != null) {
                var resultData = Map.of(
                    "successful", false, 
                    "failReason", "Symbol name pattern is too short or too general, try a more specific one");
                return new ToolExecutionResult(convertToJson(resultData), false);
            } else {
                throw e;
            }
        }
    } 

    protected abstract Long getProjectId();

    protected abstract ObjectId getCommitId(boolean oldRevision);

}