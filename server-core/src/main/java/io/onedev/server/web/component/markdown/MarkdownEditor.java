package io.onedev.server.web.component.markdown;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.FilenameUtils;
import io.onedev.server.validation.validator.ProjectPathValidator;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.avatar.AvatarManager;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.markdown.SuggestionSupport.Selection;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;

@SuppressWarnings("serial")
public class MarkdownEditor extends FormComponentPanel<String> {

	protected static final int ATWHO_LIMIT = 10;
	
	private static final Logger logger = LoggerFactory.getLogger(MarkdownEditor.class);
	
	private final boolean compactMode;
	
	private final boolean initialSplit;
	
	private final BlobRenderContext blobRenderContext;
	
	private WebMarkupContainer container;
	
	private TextArea<String> input;
	
	private AbstractPostAjaxBehavior actionBehavior;
	
	private AbstractPostAjaxBehavior attachmentUploadBehavior;
	
	/**
	 * @param id 
	 * 			component id of the editor
	 * @param model
	 * 			markdown model of the editor
	 * @param compactMode
	 * 			editor in compact mode occupies horizontal space and is suitable 
	 * 			to be used in places such as comment aside the code
	 */
	public MarkdownEditor(String id, IModel<String> model, boolean compactMode, 
			@Nullable BlobRenderContext blobRenderContext) {
		super(id, model);
		this.compactMode = compactMode;
		
		String cookieKey;
		if (compactMode)
			cookieKey = "markdownEditor.compactMode.split";
		else
			cookieKey = "markdownEditor.normalMode.split";
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(cookieKey);
		initialSplit = cookie!=null && "true".equals(cookie.getValue());
		
		this.blobRenderContext = blobRenderContext;
	}
	
	@Override
	protected void onModelChanged() {
		super.onModelChanged();
		input.setModelObject(getModelObject());
	}
	
	public void clearMarkdown() {
		setModelObject("");
		input.setConvertedInput(null);
	}

	private String renderInput(String input) {
		if (StringUtils.isNotBlank(input)) {
			// Normalize line breaks to make source position tracking information comparable 
			// to textarea caret position when sync edit/preview scroll bar
			input = StringUtils.replace(input, "\r\n", "\n");
			return renderMarkdown(input);
		} else {
			return "<div class='message'>Nothing to preview</div>";
		}
	}
	
	protected String renderMarkdown(String markdown) {
		Project project ;
		if (getPage() instanceof ProjectPage)
			project = ((ProjectPage) getPage()).getProject();
		else
			project = null;
		
		SuggestionSupport suggestionSupport = new SuggestionSupport() {

			@Override
			public Selection getSelection() {
				return MarkdownEditor.this.getSuggestionSupport().getSelection();
			}

			@Override
			public boolean isOutdated() {
				return MarkdownEditor.this.getSuggestionSupport().isOutdated();
			}

			@Override
			public ApplySupport getApplySupport() {
				return null;
			}

			@Override
			public String getFileName() {
				return MarkdownEditor.this.getSuggestionSupport().getFileName();
			}
			
		};
		MarkdownManager manager = OneDev.getInstance(MarkdownManager.class);
		return manager.process(manager.render(markdown), project, blobRenderContext, 
				suggestionSupport, false);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		
		add(container);
		
		WebMarkupContainer editLink = new WebMarkupContainer("editLink");
		WebMarkupContainer splitLink = new WebMarkupContainer("splitLink");
		WebMarkupContainer preview = new WebMarkupContainer("preview");
		WebMarkupContainer edit = new WebMarkupContainer("edit");
		container.add(editLink);
		container.add(splitLink);
		container.add(preview);
		container.add(edit);
		
		container.add(AttributeAppender.append("class", compactMode?"compact-mode":"normal-mode"));
		
		container.add(new DropdownLink("doReference") {


			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new Fragment(id, "referenceMenuFrag", MarkdownEditor.this) {

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						String script = String.format("onedev.server.markdown.setupActionMenu($('#%s'), $('#%s'));", 
								container.getMarkupId(), getMarkupId());
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				}.setOutputMarkupId(true);
			}
			
		}.setVisible(getReferenceSupport() != null));
		
		container.add(new DropdownLink("actionMenuTrigger") {


			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new Fragment(id, "actionMenuFrag", MarkdownEditor.this) {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new WebMarkupContainer("doMention").setVisible(getUserMentionSupport() != null));
						
						if (getReferenceSupport() != null) 
							add(new Fragment("doReference", "referenceMenuFrag", MarkdownEditor.this));
						else 
							add(new WebMarkupContainer("doReference").setVisible(false));
					}

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						String script = String.format("onedev.server.markdown.setupActionMenu($('#%s'), $('#%s'));", 
								container.getMarkupId(), getMarkupId());
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				}.setOutputMarkupId(true);
			}
			
		});
		
		container.add(new WebMarkupContainer("doMention").setVisible(getUserMentionSupport() != null));
		
		container.add(new WebMarkupContainer("doSuggestion") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				Selection suggestFor = getSuggestionSupport().getSelection();
				tag.put("data-content", Joiner.on('\n').join(suggestFor.getContent()));
				tag.put("data-from", suggestFor.getRange().getFrom());
				tag.put("data-to", suggestFor.getRange().getTo());
				
				if (getSuggestionSupport().isOutdated())
					tag.put("disabled", "disabled");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getSuggestionSupport() != null);
			}
			
		});
		
		edit.add(input = new TextArea<>("input", Model.of(getModelObject())) {

			@Override
			protected boolean shouldTrimInput() {
				return MarkdownEditor.this.shouldTrimInput();
			}

		});
		for (Behavior behavior: getInputBehaviors()) 
			input.add(behavior);

		if (initialSplit) {
			container.add(AttributeAppender.append("class", "split-mode"));

			String rendered = renderInput(input.getConvertedInput());
			preview.add(new Label("rendered", rendered) {

				@Override
				public void renderHead(IHeaderResponse response) {
					super.renderHead(response);
					String script = String.format(
							"onedev.server.markdown.initRendered('%s');", container.getMarkupId());
					response.render(OnDomReadyHeaderItem.forScript(script));
				}
				
			}.setEscapeModelStrings(false));
			
			container.add(new LazyResourceLoader("lazyResourceLoader", Model.of(rendered)));
			splitLink.add(AttributeAppender.append("class", "active"));
		} else {
			container.add(AttributeAppender.append("class", "edit-mode"));
			preview.add(new WebMarkupContainer("rendered"));
			editLink.add(AttributeAppender.append("class", "active"));
			container.add(new LazyResourceLoader("lazyResourceLoader", Model.of((String)null)));
		}
		
		container.add(new WebMarkupContainer("canAttachFile").setVisible(getAttachmentSupport()!=null));
		
		container.add(actionBehavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("markdown-preview", AjaxChannel.Type.DROP));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String action = params.getParameterValue("action").toString("");
				switch (action) {
				case "render":
					String markdown = params.getParameterValue("param1").toString();
					String rendered = renderInput(markdown);
					LazyResourceLoader lazyResourceLoader = new LazyResourceLoader("lazyResourceLoader", Model.of(rendered));
					container.replace(lazyResourceLoader);
					target.add(lazyResourceLoader);
					String script = String.format(
							"onedev.server.markdown.onRendered('%s', '%s');", 
							container.getMarkupId(), 
							JavaScriptEscape.escapeJavaScript(rendered));
					target.appendJavaScript(script);
					break;
				case "emojiQuery":
					List<String> emojiAliases = new ArrayList<>();
					String emojiQuery = params.getParameterValue("param1").toOptionalString();
					if (StringUtils.isNotBlank(emojiQuery)) {
						emojiQuery = emojiQuery.toLowerCase();
						for (String emojiAlias: Emojis.getInstance().getAliases()) {
							if (emojiAlias.toLowerCase().contains(emojiQuery))
								emojiAliases.add(emojiAlias);
						}
						emojiAliases.sort((name1, name2) -> name1.length() - name2.length());
					} else {
						emojiAliases.add("smiley");
						emojiAliases.add("worried");
						emojiAliases.add("ok_hand");
						emojiAliases.add("thumbsup");
						emojiAliases.add("thumbsdown");
						emojiAliases.add("heart");
					}
					List<Map<String, String>> emojis = new ArrayList<>();
					for (String emojiAlias: emojiAliases) {
						if (emojis.size() < ATWHO_LIMIT) {
							String emojiUnicode = Emojis.getInstance().getUnicode(emojiAlias);
							emojis.add(CollectionUtils.newHashMap(
									"name", emojiAlias, 
									"unicode", emojiUnicode));
						}
					}
					String json;
					try {
						json = AppLoader.getInstance(ObjectMapper.class).writeValueAsString(emojis);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					script = String.format("$('#%s').data('atWhoEmojiRenderCallback')(%s);", container.getMarkupId(), json);
					target.appendJavaScript(script);
					break;
				case "loadEmojis":
					try {
						json = AppLoader.getInstance(ObjectMapper.class).writeValueAsString(Emojis.getInstance().list());
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}

					script = String.format("onedev.server.markdown.onEmojisLoaded('%s', %s);", container.getMarkupId(), json);
					target.appendJavaScript(script);
					break;
				case "userQuery":
					String userQuery = params.getParameterValue("param1").toOptionalString();

					AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
					List<Map<String, String>> userList = new ArrayList<>();
					for (User user: getUserMentionSupport().findUsers(userQuery, ATWHO_LIMIT)) {
						Map<String, String> userMap = new HashMap<>();
						userMap.put("name", user.getName());
						if (user.getFullName() != null)
							userMap.put("fullName", user.getFullName());
						String noSpaceName = StringUtils.deleteWhitespace(user.getName());
						if (user.getFullName() != null) {
							String noSpaceFullName = StringUtils.deleteWhitespace(user.getFullName());
							userMap.put("searchKey", noSpaceName + " " + noSpaceFullName);
						} else {
							userMap.put("searchKey", noSpaceName);
						}
						String avatarUrl = avatarManager.getUserAvatarUrl(user.getId());
						userMap.put("avatarUrl", avatarUrl);
						userList.add(userMap);
					}
					
					try {
						json = OneDev.getInstance(ObjectMapper.class).writeValueAsString(userList);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					script = String.format("$('#%s').data('atWhoUserRenderCallback')(%s);", container.getMarkupId(), json);
					target.appendJavaScript(script);	
					break;
				case "referenceQuery":
					String referenceQuery = params.getParameterValue("param1").toOptionalString();
					String referenceQueryType = params.getParameterValue("param2").toOptionalString();
					String referenceProjectPath = params.getParameterValue("param3").toOptionalString();
					List<Map<String, String>> referenceList = new ArrayList<>();
					Project referenceProject;
					if (StringUtils.isNotBlank(referenceProjectPath)) 
						referenceProject = OneDev.getInstance(ProjectManager.class).findByPath(referenceProjectPath);
					else
						referenceProject = null;
					if (referenceProject != null || StringUtils.isBlank(referenceProjectPath)) {
						if (referenceQueryType.length() == 0 || "issue".equals(referenceQueryType)) {
							for (Issue issue: getReferenceSupport().findIssues(referenceProject, referenceQuery, ATWHO_LIMIT)) {
								Map<String, String> referenceMap = new HashMap<>();
								referenceMap.put("referenceType", "issue");
								referenceMap.put("referenceNumber", String.valueOf(issue.getNumber()));
								referenceMap.put("referenceTitle", Emojis.getInstance().apply(issue.getTitle()));
								referenceMap.put("searchKey", issue.getNumber() + " " + StringUtils.deleteWhitespace(issue.getTitle()));
								referenceList.add(referenceMap);
							}
						} else if ("pr".equals(referenceQueryType) || "pullrequest".equals(referenceQueryType)) {
							for (PullRequest request: getReferenceSupport().findPullRequests(referenceProject, referenceQuery, ATWHO_LIMIT)) {
								Map<String, String> referenceMap = new HashMap<>();
								referenceMap.put("referenceType", "pull request");
								referenceMap.put("referenceNumber", String.valueOf(request.getNumber()));
								referenceMap.put("referenceTitle", Emojis.getInstance().apply(request.getTitle()));
								referenceMap.put("searchKey", request.getNumber() + " " + StringUtils.deleteWhitespace(request.getTitle()));
								referenceList.add(referenceMap);
							}
						} else if ("build".equals(referenceQueryType)) {
							for (Build build: getReferenceSupport().findBuilds(referenceProject, referenceQuery, ATWHO_LIMIT)) {
								Map<String, String> referenceMap = new HashMap<>();
								referenceMap.put("referenceType", "build");
								referenceMap.put("referenceNumber", String.valueOf(build.getNumber()));
								
								String title;
								if (build.getVersion() != null) 
									title = "(" + build.getVersion() + ") " + build.getJobName();
								else
									title = build.getJobName();
								referenceMap.put("referenceTitle", title);
								referenceMap.put("searchKey", build.getNumber() + " " + StringUtils.deleteWhitespace(title));
								referenceList.add(referenceMap);
							}
						}
					}
					
					try {
						json = OneDev.getInstance(ObjectMapper.class).writeValueAsString(referenceList);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					script = String.format("$('#%s').data('atWhoReferenceRenderCallback')(%s);", container.getMarkupId(), json);
					target.appendJavaScript(script);
					break;
				case "selectImage":
				case "selectLink":
					String selectedText = params.getParameterValue("param1").toOptionalString();
					new ModalPanel(target) {
						
						@Override
						protected Component newContent(String id) {
							return new InsertUrlPanel(id, MarkdownEditor.this, selectedText, action.equals("selectImage")) {

								@Override
								protected void onClose(AjaxRequestTarget target) {
									close();
								}

							};
						}

						@Override
						protected void onClosed() {
							super.onClosed();
							AjaxRequestTarget target = 
									Preconditions.checkNotNull(RequestCycle.get().find(AjaxRequestTarget.class));
							target.appendJavaScript(String.format("$('#%s textarea').focus();", container.getMarkupId()));
						}
						
					};
					break;
				case "insertUrl":
					String name;
					try {
						name = URLDecoder.decode(params.getParameterValue("param1").toString(), StandardCharsets.UTF_8.name());
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
					String replaceMessage = params.getParameterValue("param2").toString();
					String url = getAttachmentSupport().getAttachmentUrlPath(name);
					insertUrl(target, isWebSafeImage(name), url, name, replaceMessage);
					
					break;
				default:
					throw new IllegalStateException("Unknown action: " + action);
				}		
			}
			
		});
		
		container.add(attachmentUploadBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				Preconditions.checkNotNull(getAttachmentSupport(), "Unexpected attachment upload request");
				HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
				HttpServletResponse response = (HttpServletResponse) RequestCycle.get().getResponse().getContainerResponse();
				try {
					String fileName = FilenameUtils.sanitizeFilename(
							URLDecoder.decode(request.getHeader("File-Name"), StandardCharsets.UTF_8.name()));
					String attachmentName = getAttachmentSupport().saveAttachment(fileName, request.getInputStream());
					response.getWriter().print(URLEncoder.encode(attachmentName, StandardCharsets.UTF_8.name()));
					response.setStatus(HttpServletResponse.SC_OK);
				} catch (Exception e) {
					logger.error("Error uploading attachment.", e);
					try {
						if (e.getMessage() != null)
							response.getWriter().print(e.getMessage());
						else
							response.getWriter().print("Internal server error");
					} catch (IOException e2) {
						throw new RuntimeException(e2);
					}
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
			
		});
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof ContentQuoted) {
			ContentQuoted contentQuoted = (ContentQuoted) event.getPayload();
			
			String script = String.format("onedev.server.markdown.onQuote('%s', '%s');", 
					container.getMarkupId(), JavaScriptEscape.escapeJavaScript(contentQuoted.getContent()));
			contentQuoted.getHandler().appendJavaScript(script);			
			event.stop();
		}
	}

	@Override
	public void convertInput() {
		setConvertedInput(input.getConvertedInput());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
		
		String actionCallback = actionBehavior.getCallbackFunction(explicit("action"), explicit("param1"), explicit("param2"), 
				explicit("param3")).toString();
		String attachmentUploadUrl = attachmentUploadBehavior.getCallbackUrl().toString();
		
		String script = String.format("onedev.server.markdown.onDomReady('%s', %s, %d, %s, %d, %b, %b, '%s');", 
				container.getMarkupId(), 
				actionCallback, 
				ATWHO_LIMIT, 
				getAttachmentSupport()!=null? "'" + attachmentUploadUrl + "'": "undefined", 
				getAttachmentSupport()!=null? getAttachmentSupport().getAttachmentMaxSize(): 0,
				getUserMentionSupport() != null,
				getReferenceSupport() != null, 
				JavaScriptEscape.escapeJavaScript(ProjectPathValidator.PATTERN.pattern()));
		response.render(OnDomReadyHeaderItem.forScript(script));
		
		script = String.format("onedev.server.markdown.onLoad('%s');", container.getMarkupId());
		response.render(OnLoadHeaderItem.forScript(script));
	}

	public void insertUrl(AjaxRequestTarget target, boolean isImage, String url, 
			String name, @Nullable String replaceMessage) {
		String script = String.format("onedev.server.markdown.insertUrl('%s', %s, '%s', '%s', %s);",
				container.getMarkupId(), isImage, StringEscapeUtils.escapeEcmaScript(url), 
				StringEscapeUtils.escapeEcmaScript(name), 
				replaceMessage!=null?"'"+replaceMessage+"'":"undefined");
		target.appendJavaScript(script);
	}
	
	public boolean isWebSafeImage(String fileName) {
		fileName = fileName.toLowerCase();
		return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") 
				|| fileName.endsWith(".gif") || fileName.endsWith(".png");
	}
	
	@Nullable
	protected AttachmentSupport getAttachmentSupport() {
		return null;
	}
	
	@Nullable
	protected UserMentionSupport getUserMentionSupport() {
		return null;
	}
	
	@Nullable
	protected AtWhoReferenceSupport getReferenceSupport() {
		return null;
	}
	
	@Nullable
	protected SuggestionSupport getSuggestionSupport() {
		return null;
	}
	
	protected List<Behavior> getInputBehaviors() {
		return new ArrayList<>();
	}
	
	@Nullable
	public BlobRenderContext getBlobRenderContext() {
		return blobRenderContext;
	}
	
}
