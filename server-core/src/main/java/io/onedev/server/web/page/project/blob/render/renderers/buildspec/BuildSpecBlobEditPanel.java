package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import static io.onedev.server.ai.ToolUtils.convertToJson;
import static io.onedev.server.ai.ToolUtils.wrapForChat;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.Validator;

import org.apache.shiro.subject.Subject;
import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.ai.ChatTool;
import io.onedev.server.ai.ChatToolAware;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.ai.tools.GetBuildSpecEditInstructions;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.data.migration.VersionedYamlDoc;
import io.onedev.server.service.ManagedFutureService;
import io.onedev.server.web.component.diff.text.PlainTextDiffPanel;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;

public class BuildSpecBlobEditPanel extends BlobEditPanel implements PlainEditSupport, ChatToolAware {

	private static final int GET_BUILD_SPEC_TIMEOUT_SECONDS = 60;

	@Inject
	private Validator validator;

	@Inject
	private ManagedFutureService managedFutureService;

	public BuildSpecBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	private String getBuildSpecFutureId() {
		return "buildspec:" + getSession().getId() + ":" + getPath();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onFlushed(IPartialPageRequestHandler handler, byte[] editingContent) {
		var future = (CompletableFuture<ToolExecutionResult>) (Object) managedFutureService.removeFuture(getBuildSpecFutureId());
		if (future != null) {
			if (getCurrentTab() != Tab.SAVE) {
				var buildSpecString = new String(editingContent, StandardCharsets.UTF_8);
				try {
					var buildSpec = BuildSpec.parse(buildSpecString.getBytes(StandardCharsets.UTF_8));
					buildSpecString = VersionedYamlDoc.fromBean(buildSpec).toYaml();
				} catch (Throwable t) {					
				}
				future.complete(new ToolExecutionResult(buildSpecString, false));
			} else {
				future.completeExceptionally(new ExplicitException("CI/CD spec is not being edited currently"));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onFlushError(IPartialPageRequestHandler handler) {
		var future = (CompletableFuture<ToolExecutionResult>) (Object) managedFutureService.removeFuture(getBuildSpecFutureId());
		if (future != null) 
			future.completeExceptionally(new ExplicitException("There are errors in current CI/CD spec, please fix first"));
	}

	@Override
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return new BuildSpecEditPanel(componentId, context, initialContent);
	}

	@Override
	public FormComponentPanel<byte[]> newPlainEditor(String componentId, byte[] initialContent) {
		return new PlainEditPanel(componentId, BuildSpec.BLOB_PATH, initialContent);
	}

	@Override
	public Component renderTabHead(String componentId) {
		return new BuildSpecPlainTabHead(componentId);
	}

	@Override
	protected Component newChangesViewer(String componentId, byte[] initialContent, byte[] editingContent) {
		List<String> oldLines;
		if (initialContent.length > 0)
			oldLines = Splitter.on("\n").splitToList(new String(initialContent, StandardCharsets.UTF_8));
		else
			oldLines = new ArrayList<>();
		List<String> newLines;
		if (editingContent.length > 0)
			newLines = Splitter.on("\n").splitToList(new String(editingContent, StandardCharsets.UTF_8));
		else
			newLines = new ArrayList<>();
		return new PlainTextDiffPanel(componentId, oldLines, newLines, true, BuildSpec.BLOB_PATH);
	}

	@Override
	public List<ChatTool> getChatTools() {
		var tools = new ArrayList<ChatTool>();
		if (getCurrentTab() != Tab.SAVE) {
			tools.add(wrapForChat(new GetBuildSpecEditInstructions(true)));
			
			tools.add(new ChatTool() {
	
				@Override
				public ToolSpecification getSpecification() {
					return ToolSpecification.builder()
						.name("getCurrentBuildSpec")
						.description("Get current CI/CD spec in yaml format")
						.build();
				}
	
				@Override
				public CompletableFuture<ToolExecutionResult> execute(IPartialPageRequestHandler handler, Subject subject, JsonNode arguments) {
					requestToFlush(handler);
					var future = new CompletableFuture<ToolExecutionResult>();
					managedFutureService.addFuture(getBuildSpecFutureId(), future, GET_BUILD_SPEC_TIMEOUT_SECONDS);
					return future;
				}
	
			});
	
			tools.add(new ChatTool() {
	
				@Override
				public ToolSpecification getSpecification() {
					return ToolSpecification.builder()
						.name("saveBuildSpec")					
						.description("Save CI/CD spec")
						.parameters(JsonObjectSchema.builder()
							.addStringProperty("buildSpec").description("CI/CD spec yaml to save")
							.required("buildSpec").build())
						.build();
				}
	
				@Override
				public CompletableFuture<ToolExecutionResult> execute(IPartialPageRequestHandler handler, Subject subject, JsonNode arguments) {
					if (getCurrentTab() != Tab.SAVE) {
						if (arguments.get("buildSpec") == null)
							return completedFuture(new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'buildSpec' is required")), false));
						var buildSpecString = arguments.get("buildSpec").asText();
						BuildSpec buildSpec;
						try {
							buildSpec = BuildSpec.parse(buildSpecString.getBytes(StandardCharsets.UTF_8));
						} catch (Throwable t) {					
							return completedFuture(new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Malformed CI/CD spec, please fix and save again")), false));
						}

						var violations = validator.validate(buildSpec);
						Map<String, Object> resultData;
						if (!violations.isEmpty()) {
							var errorMessage = "Failed to validate CI/CD spec, please fix errors below and save again:\n\n" + 
									Joiner.on("\n").join(violations.stream().map(it -> String.format("\t-> Location: %s, Error: %s", it.getPropertyPath(), it.getMessage())).collect(Collectors.toList()));
							resultData = Map.of( "successful", false, "failReason", errorMessage);
						} else {
							updateEditingContent(handler, buildSpecString.getBytes(StandardCharsets.UTF_8));
							resultData = Map.of("successful", true);
						}
						return completedFuture(new ToolExecutionResult(convertToJson(resultData), false));
					} else {
						throw new ExplicitException("CI/CD spec is not being edited currently");
					}
				}	

			});			
		}
		return tools;
	}

}
