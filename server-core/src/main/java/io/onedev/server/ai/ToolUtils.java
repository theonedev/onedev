package io.onedev.server.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ai.tools.GetDiffPatch;
import io.onedev.server.ai.tools.GetFileContent;
import io.onedev.server.ai.tools.QueryCodeSnippets;
import io.onedev.server.ai.tools.QuerySymbolDefinitions;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.util.IgnoreLinearRangeMixin;
import io.onedev.server.util.IgnorePlanarRangeMixin;

public class ToolUtils {

	private static final Logger logger = LoggerFactory.getLogger(ToolUtils.class);
	
	private static ObjectMapper getObjectMapper() {
		return OneDev.getInstance(ObjectMapper.class).copy();
	}
	
    public static String convertToJson(Object data) {
        try {
            var mapper = getObjectMapper().copy();
            mapper.addMixIn(PlanarRange.class, IgnorePlanarRangeMixin.class);
            mapper.addMixIn(LinearRange.class, IgnoreLinearRangeMixin.class);
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

	public static String getFilesAndFolders(Project project, ObjectId commitId, @Nullable String path) {
        var gitService = OneDev.getInstance(GitService.class);
		if (path != null) {
			var blobIdent = gitService.getBlobIdent(project, commitId, path);
			if (blobIdent == null) 
				return convertToJson(Map.of("successful", false, "failReason", "Folder not found"));
			if (!blobIdent.isTree())
				return convertToJson(Map.of("successful", false, "failReason", "Not a folder"));
			path = StringUtils.strip(path.replace('\\', '/'), "/");
		}			
		List<BlobIdent> children = gitService.getChildren(project, commitId,
			path, BlobIdentFilter.ALL, false);
		var filesAndFolders = new ArrayList<Map<String, Object>>();
		for (var child : children) {
			var fileAndFolder = new HashMap<String, Object>();
			fileAndFolder.put("name", child.getName());
			fileAndFolder.put("type", child.isTree() ? "folder" : "file");
			filesAndFolders.add(fileAndFolder);
		}
		return convertToJson(Map.of("successful", true, "filesAndFolders", filesAndFolders));
	}

	public static JsonNode getToolArguments(ToolExecutionRequest toolExecutionRequest) {
		var objectMapper = getObjectMapper();
		if (toolExecutionRequest.arguments() == null)
			return objectMapper.createObjectNode();

		try {
			return objectMapper.readTree(toolExecutionRequest.arguments());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void handleCallException(String toolName, Throwable exception) {
		if (ExceptionUtils.find(exception, UnauthorizedException.class) != null) 
			throw new ExplicitException("Permission denied calling tool: " + toolName);
		var explicitException = ExceptionUtils.find(exception, ExplicitException.class);
		if (explicitException != null) 
			throw explicitException;
		logger.error("Error calling tool: " + toolName, exception);
		throw new ExplicitException("Error calling tool '" + toolName + "', check server log for details");
	}

	public static ChatTool wrapForChat(TaskTool taskTool) {
		return new ChatTool() {

			@Override
			public ToolSpecification getSpecification() {
				return taskTool.getSpecification();
			}

			@Override
			public CompletableFuture<ToolExecutionResult> execute(@Nullable IPartialPageRequestHandler handler, Subject subject, JsonNode arguments) {
				return CompletableFuture.completedFuture(taskTool.execute(subject, arguments));
			}
		};
	}

	public static List<ChatTool> wrapForChat(List<TaskTool> taskTools) {
		return taskTools.stream()
			.map(ToolUtils::wrapForChat)
			.collect(Collectors.toList());
	}

	public static List<String> required(boolean inDiffContext, String... arguments) {
		if (inDiffContext) {
			var list = new ArrayList<String>(List.of(arguments));
			list.add("oldRevision");
			return list;
		} else {
			return List.of(arguments);
		}
	}

	public static List<TaskTool> getDiffTools(Long projectId, ObjectId oldCommitId, ObjectId newCommitId, @Nullable Long pullRequestId) {
		ObjectId comparisonBase;
		if (pullRequestId != null) {
			var pullRequestService = OneDev.getInstance(PullRequestService.class);
			var request = pullRequestService.load(pullRequestId);
			comparisonBase = pullRequestService.getComparisonBase(request, oldCommitId, newCommitId);
		} else {
			comparisonBase = oldCommitId;
		}
		return List.of(
			new GetFileContent(true) {
				@Override
				protected Long getProjectId() {
					return projectId;
				}
				
				@Override
				protected ObjectId getCommitId(boolean oldRevision) {
					return oldRevision ? comparisonBase : newCommitId;
				}
			},
			new QuerySymbolDefinitions(true) {

				@Override
				protected Long getProjectId() {
					return projectId;
				}
				
				@Override
				protected ObjectId getCommitId(boolean oldRevision) {
					return oldRevision ? comparisonBase : newCommitId;
				}
			},
			new QueryCodeSnippets(true) {

				@Override
				protected Long getProjectId() {
					return projectId;
				}
				
				@Override
				protected ObjectId getCommitId(boolean oldRevision) {
					return oldRevision ? comparisonBase : newCommitId;
				}
			},
			new GetDiffPatch() {

				@Override
				protected Long getProjectId() {
					return projectId;
				}
				
				@Override
				protected ObjectId getOldCommitId() {
					return comparisonBase;
				}
				
				@Override
				protected ObjectId getNewCommitId() {
					return newCommitId;
				}

			}
			/*
			new AddCodeComment(true) {
				
				@Override
				protected Long getProjectId() {
					return projectId;
				}
				
				@Override
				protected Long getPullRequestId() {
					return pullRequestId;
				}

				@Override
				protected ObjectId getCommitId(boolean oldRevision) {
					return oldRevision ? oldCommitId : newCommitId;
				}
			}
			*/
		);		
	}

	public static void filterDuplications(List<ToolSpecification> toolSpecifications) {
		var seenNames = new HashSet<String>();
		for (var it = toolSpecifications.iterator(); it.hasNext();) {
			if (!seenNames.add(it.next().name())) 
				it.remove();
		}
	}

}