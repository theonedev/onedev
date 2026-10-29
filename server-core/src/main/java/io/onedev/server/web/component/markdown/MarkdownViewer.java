package io.onedev.server.web.component.markdown;

import java.util.Date;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.StaleStateException;
import org.jspecify.annotations.Nullable;
import org.unbescape.html.HtmlEscape;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entityreference.BuildReference;
import io.onedev.server.entityreference.EntityReference;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.entityreference.PullRequestReference;
import io.onedev.server.entityreference.WorkspaceReference;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.asset.lozad.LozadResourceReference;
import io.onedev.server.web.avatar.AvatarService;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.workspace.WorkspaceService;

public class MarkdownViewer extends GenericPanel<String> {

	private static final String TASK_CHECKED = "taskchecked";
	
	private static final String REFERENCE_TYPE = "referenceType";
	
	private static final String REFERENCE_ID = "referenceId";
	
	private static final String SUGGESTION_ACTION = "suggestionAction";
	
	private static final String SUGGESTION_CONTENT = "suggestionContent";
	
	private final ContentVersionSupport contentVersionSupport;
	
	private long lastContentVersion;
	
	private AbstractPostAjaxBehavior taskBehavior;
	
	private AbstractPostAjaxBehavior referenceBehavior;
	
	private AbstractPostAjaxBehavior suggestionBehavior;

	@Inject
	private MarkdownService markdownService;

	@Inject
	private BuildService buildService;

	@Inject
	private AvatarService avatarService;

	@Inject
	private UserService userService;

	@Inject
	private ProjectService projectService;

	@Inject
	private WorkspaceService workspaceService;
	
	private final IModel<String> renderedModel = new LoadableDetachableModel<String>() {

		@Override
		protected String load() {
			String markdown = getModelObject();
			if (markdown != null) {
				return renderMarkdown(markdown);
			} else {
				return null;
			}
		}
		
	};
	
	public MarkdownViewer(String id, IModel<String> model, @Nullable ContentVersionSupport contentVersionSupport) {
		super(id, model);
		this.contentVersionSupport = contentVersionSupport;
		if (contentVersionSupport != null)
			lastContentVersion = contentVersionSupport.getVersion();
	}
	
	protected String renderMarkdown(String markdown) {
		return markdownService.process(markdownService.render(markdown), getProject(),
				getRenderContext(), getSuggestionSupport(), false);
	}

	protected BlobRenderContext getRenderContext() {
		return null;
	}

	@Nullable
	private Project getProject() {
		if (getRenderContext() != null)
			return getRenderContext().getProject();
		else if (getPage() instanceof ProjectPage)
			return ((ProjectPage) getPage()).getProject(); 
		else
			return null;
	}
	
	@Override
	protected void onDetach() {
		renderedModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		FencedFeedbackPanel feedback = new FencedFeedbackPanel("feedback", this);
		feedback.setOutputMarkupPlaceholderTag(true);
		add(feedback);
		
		if (RequestCycle.get().find(AjaxRequestTarget.class).orElse(null) != null) {
			/*
			 *  Render it as html will cause issue when markdown contains some html
			 *  entities such as "&#27;" and when the component is rendered via ajax. 
			 *  
			 *  This is because some html valid chars are not valid xml chars (the 
			 *  component html will be sent to browser via xml when render via ajax) 
			 */
			add(new WebMarkupContainer("content") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					String rendered = renderedModel.getObject();
					if (rendered != null)
						tag.put("data-content", rendered);
				}
				
			});
		} else {
			add(new Label("content", renderedModel).setEscapeModelStrings(false));
		}
		
		add(new LazyResourceLoader("lazyResourceLoader", renderedModel));
		
		add(taskBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				int taskPosition = params.getParameterValue(SourcePositionTrackExtension.DATA_START_ATTRIBUTE).toInt();
				boolean taskChecked = params.getParameterValue(TASK_CHECKED).toBoolean();
				String markdown = HtmlEscape.unescapeHtml(getComponent().getDefaultModelObjectAsString());
				String beforeTask = markdown.substring(0, taskPosition);
				String fromTask = markdown.substring(taskPosition);
				String beforeBracket = StringUtils.substringBefore(fromTask, "[");
				String afterBracket = StringUtils.substringAfter(fromTask, "]");
				String taskStatus = taskChecked?"x":" ";
				markdown = beforeTask + beforeBracket + "[" + taskStatus + "]" + afterBracket;

				try {
					if (contentVersionSupport.getVersion() != lastContentVersion)
						throw new StaleStateException("");
					setDefaultModelObject(markdown);
				} catch (StaleStateException e) {
					warn("Some one changed the content you are editing. The content has now been reloaded, "
							+ "please try again.");
				}
				target.add(MarkdownViewer.this);
				lastContentVersion = contentVersionSupport.getVersion();
			}
			
		});
		
		add(referenceBehavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String referenceType = params.getParameterValue(REFERENCE_TYPE).toString("");
				String referenceId = params.getParameterValue(REFERENCE_ID).toString();
				switch (referenceType) {
				case "issue":
					ReferenceTooltipBehavior.renderTooltip(target, IssueReference.of(referenceId, null));
					break;
				case "pull request":
					ReferenceTooltipBehavior.renderTooltip(target, PullRequestReference.of(referenceId, null));
					break;
				case "build":
					EntityReference reference = BuildReference.of(referenceId, null);
					var build = buildService.find(reference.getProject(), reference.getNumber());
					// check permission here as build project may not be the same as current project
					if (build != null && SecurityUtils.canAccessProject(build.getProject())) {
						String iconHref = SpriteImage.getVersionedHref(BuildStatusIcon.getIconHref(build.getStatus()));
						String iconCss = BuildStatusIcon.getIconClass(build.getStatus());
						
						String title = build.getJobName();
						if (build.getVersion() != null)
							title += " : " + build.getVersion();
						String script = String.format("onedev.server.markdown.renderBuildTooltip('%s', '%s', '%s');", 
								JavaScriptEscape.escapeJavaScript(title), iconHref, iconCss);
						target.appendJavaScript(script);
					} else {
						target.appendJavaScript("onedev.server.markdown.renderBuildTooltip();");
					}
					break;
				case "user":
					User user = userService.findByName(referenceId);
					if (user != null) {
						String avatarUrl = avatarService.getUserAvatarUrl(user.getId());
						String script = String.format("onedev.server.markdown.renderUserTooltip('%s', '%s')", 
								JavaScriptEscape.escapeJavaScript(avatarUrl), 
								JavaScriptEscape.escapeJavaScript(user.getDisplayName()));
						target.appendJavaScript(script);
					}
					break;
				case "commit":
					Project commitProject = getProject();
					String commitHash;
					if (referenceId.contains(":")) {
						commitProject = projectService.findByPath(StringUtils.substringBefore(referenceId, ":"));
						commitHash = StringUtils.substringAfter(referenceId, ":");
					} else {
						commitHash = referenceId;
					}
					RevCommit commit = null;
					if (commitProject != null)
						commit = commitProject.getRevCommit(ObjectId.fromString(commitHash), false);
					if (commit != null && SecurityUtils.canReadCode(commitProject)) {
						String script = String.format("onedev.server.markdown.renderCommitTooltip('%s', '%s', '%s', '%s');",
								JavaScriptEscape.escapeJavaScript(commit.getAuthorIdent().getName()), 
								JavaScriptEscape.escapeJavaScript(DateUtils.formatAge(Date.from(commit.getCommitterIdent().getWhenAsInstant()))),
								JavaScriptEscape.escapeJavaScript(DateUtils.formatDateTime(Date.from(commit.getCommitterIdent().getWhenAsInstant()))),
								JavaScriptEscape.escapeJavaScript(commit.getFullMessage()));
						target.appendJavaScript(script);
					} else {
						target.appendJavaScript("onedev.server.markdown.renderCommitTooltip();");
					}
					break;
				case "workspace":
					var workspaceRef = WorkspaceReference.of(referenceId, null);
					var workspace = workspaceService.find(workspaceRef.getProject(), workspaceRef.getNumber());
					if (workspace != null && SecurityUtils.canReadCode(workspace.getProject())) {
						String title = workspace.getUser().getDisplayName() + " on " + workspace.getOnDescription() + " for " + workspace.getSpecName() + " (" + workspace.getStatus().name() + ")";
						String script = String.format("onedev.server.markdown.renderWorkspaceTooltip('%s');",
								JavaScriptEscape.escapeJavaScript(title));
						target.appendJavaScript(script);
					} else {
						target.appendJavaScript("onedev.server.markdown.renderWorkspaceTooltip();");
					}
					break;
				default:
					throw new NotAcceptableException("Unrecognized reference type: " + referenceType);
				}
			}
			
		});
		
		add(suggestionBehavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String suggestionAction = params.getParameterValue(SUGGESTION_ACTION).toString();				
				String suggestionContent = params.getParameterValue(SUGGESTION_CONTENT).toString();
				List<String> suggestion = StringUtils.splitToLines(suggestionContent);
				switch (suggestionAction) {
				case "apply":
					getSuggestionSupport().getApplySupport().applySuggestion(target, suggestion);
					break;
				case "addToBatch":
					getSuggestionSupport().getApplySupport().getBatchSupport().addToBatch(target, suggestion);
					break;
				case "removeFromBatch":
					getSuggestionSupport().getApplySupport().getBatchSupport().removeFromBatch(target);
					break;
				default:
					throw new NotAcceptableException("Unrecognized suggestion action: " + suggestionAction);
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Nullable
	protected SuggestionSupport getSuggestionSupport() {
		return null;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new LozadResourceReference()));
		response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
		
		CharSequence taskCallback = taskBehavior.getCallbackFunction(
				explicit(SourcePositionTrackExtension.DATA_START_ATTRIBUTE), 
				explicit(TASK_CHECKED));
		
		CharSequence referenceCallback = referenceBehavior.getCallbackFunction(
				explicit(REFERENCE_TYPE), explicit(REFERENCE_ID));
		
		CharSequence suggestionCallback = suggestionBehavior.getCallbackFunction(
				explicit(SUGGESTION_ACTION), explicit(SUGGESTION_CONTENT));
		
		String script = String.format("onedev.server.markdown.onViewerDomReady('%s', %s, '%s', %s, %s, %s);", 
				getMarkupId(), 
				contentVersionSupport!=null?taskCallback:"undefined", 
				SourcePositionTrackExtension.DATA_START_ATTRIBUTE, 
				referenceCallback, suggestionCallback, JavascriptTranslations.get());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
