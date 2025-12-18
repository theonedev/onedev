package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.Validator;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.ai.BuildSpecSchema;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.data.migration.VersionedYamlDoc;
import io.onedev.server.service.TemporalFutureService;
import io.onedev.server.web.behavior.ChatTool;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditPanel;
import io.onedev.server.web.page.project.blob.render.edit.plain.PlainEditSupport;
import io.onedev.server.web.websocket.ChatToolExecution;

public class BuildSpecBlobEditPanel extends BlobEditPanel implements PlainEditSupport {

	@Inject
	private Validator validator;

	@Inject
	private TemporalFutureService temporalFutureService;

	public BuildSpecBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	private String getBuildSpecFutureId() {
		return "buildspec:" + getSession().getId() + ":" + getPath();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (getCurrentTab() == Tab.EDIT || getCurrentTab() == Tab.EDIT_PLAIN) {
			add(new ChatTool() {

				@Override
				public ToolSpecification getSpecification() {
					return ToolSpecification.builder()
						.name("getBuildSpecEditInstructions")
						.description("Get instructions on how to edit CI/CD spec")
						.build();
				}
	
				@Override
				public CompletableFuture<ChatToolExecution.Result> execute(IPartialPageRequestHandler handler, JsonNode arguments) {
					var instructions = """
						OneDev CI/CD spec is a yaml file conforming to below schema:
	
						<!SCHEMA BEGIN!>
						%s
						<!SCHEMA END!>							
						
						When editing CI/CD spec, remember that:

						1. If command step is used, turn on the "run in container" if possible, unless requested by user explicitly
						2. Different steps run in isolated environments, with shared job workspace. So it will not work installing dependencies in one step, and run commands relying on them in another step. You should put them in a single step unless requested by user explicitly
						3. If cache step is used:
							3.1 It should be placed before the step building or testing the project
							3.2 If the project has lock files (package.json, pom.xml, etc.):
								3.2.1 A generate checksum step should be placed before the cache step, to generate checksum of all relevant lock files and store it in a file named checksum.txt						
								3.2.2 The key property should be configured as <keyname>-@file:checksum.txt@
								3.2.3 The load keys property should be configured as <keyname>
								3.2.4 The upload strategy property should be configured as UPLOAD_IF_NOT_HIT
						4. If user wants to pass files between different jobs, one job should publish files via the publish artifact step, and another jobs can then download them into job workspace via job dependency
						5. Call tools such as getRootFilesAndFolders, getFilesAndSubfolders and getTextContent to get project structure, and figure out what docker image and commands to use to build or test the project if requested by user
						6. After editing the CI/CD spec, call saveBuildSpec tool to save the result. If saving fails, fix errors according to error messages and schema above and save again						
						""".formatted(BuildSpecSchema.get());
					return completedFuture(new ChatToolExecution.Result(instructions, true));
				}
				
			});
			
			add(new ChatTool() {
	
				@Override
				public ToolSpecification getSpecification() {
					return ToolSpecification.builder()
						.name("getCurrentBuildSpec")
						.description("Get current CI/CD spec in yaml format")
						.build();
				}
	
				@Override
				public CompletableFuture<ChatToolExecution.Result> execute(IPartialPageRequestHandler handler, JsonNode arguments) {
					requestToFlush(handler);
					var future = new CompletableFuture<ChatToolExecution.Result>();
					temporalFutureService.addFuture(getBuildSpecFutureId(), future);
					return future;
				}
	
			});
	
			add(new ChatTool() {
	
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
				public CompletableFuture<ChatToolExecution.Result> execute(IPartialPageRequestHandler handler, JsonNode arguments) {
					if (getCurrentTab() == Tab.EDIT || getCurrentTab() == Tab.EDIT_PLAIN) {
						var buildSpecString = arguments.get("buildSpec").asText();
						BuildSpec buildSpec;
						try {
							buildSpec = BuildSpec.parse(buildSpecString.getBytes(StandardCharsets.UTF_8));
						} catch (Throwable t) {					
							return completedFuture(new ChatToolExecution.Result("Malformed CI/CD spec, please fix and save again", false));
						}
						var violations = validator.validate(buildSpec);
						String result;
						if (!violations.isEmpty()) {
							result = "Failed to validate CI/CD spec, please fix errors below and save again:\n\n" + 
									Joiner.on("\n").join(violations.stream().map(it -> String.format("\t-> Location: %s, Error: %s", it.getPropertyPath(), it.getMessage())).collect(Collectors.toList()));
						} else {
							updateEditingContent(handler, buildSpecString.getBytes(StandardCharsets.UTF_8));
							result = "CI/CD spec saved successfully";
						}
						return completedFuture(new ChatToolExecution.Result(result, false));
					} else {
						throw new ExplicitException("CI/CD spec is not being edited currently");
					}
				}	

			});			
		}
	}

	@Override
	protected void onFlushed(IPartialPageRequestHandler handler, byte[] editingContent) {
		CompletableFuture<ChatToolExecution.Result> future = temporalFutureService.removeFuture(getBuildSpecFutureId());
		if (future != null) {
			if (getCurrentTab() == Tab.EDIT || getCurrentTab() == Tab.EDIT_PLAIN) {
				var buildSpecString = new String(editingContent, StandardCharsets.UTF_8);
				try {
					var buildSpec = BuildSpec.parse(buildSpecString.getBytes(StandardCharsets.UTF_8));
					buildSpecString = VersionedYamlDoc.fromBean(buildSpec).toYaml();
				} catch (Throwable t) {					
				}
				future.complete(new ChatToolExecution.Result(buildSpecString, false));
			} else {
				future.completeExceptionally(new ExplicitException("CI/CD spec is not being edited currently"));
			}
		}
	}

	@Override
	protected void onFlushError(IPartialPageRequestHandler handler) {
		var future = temporalFutureService.removeFuture(getBuildSpecFutureId());
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

}
