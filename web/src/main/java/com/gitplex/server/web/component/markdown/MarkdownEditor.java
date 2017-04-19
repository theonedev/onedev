package com.gitplex.server.web.component.markdown;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.crypt.Base64;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.MarkdownManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;
import com.gitplex.server.web.component.markdown.emoji.EmojiOnes;
import com.gitplex.server.web.page.base.BasePage;
import com.gitplex.server.web.util.avatar.AvatarManager;
import com.google.common.base.Charsets;

@SuppressWarnings("serial")
public class MarkdownEditor extends FormComponentPanel<String> {

	protected static final int ATWHO_LIMIT = 5;
	
	private final boolean compactMode;
	
	private final boolean initialSplit;
	
	private WebMarkupContainer container;
	
	private TextArea<String> input;

	private AbstractPostAjaxBehavior ajaxBehavior;
	
	/**
	 * @param id 
	 * 			component id of the editor
	 * @param model
	 * 			markdown model of the editor
	 * @param compactMode
	 * 			editor in compact mode occupies horizontal space and is suitable 
	 * 			to be used in places such as comment aside the code
	 */
	public MarkdownEditor(String id, IModel<String> model, boolean compactMode) {
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
	}
	
	@Override
	protected void onModelChanged() {
		super.onModelChanged();
		input.setModelObject(getModelObject());
	}

	private String render(String markdown) {
		if (StringUtils.isNotBlank(markdown)) {
			MarkdownManager markdownManager = GitPlex.getInstance(MarkdownManager.class);
			
			// Normalize line breaks to make source position tracking information comparable 
			// to textarea caret position when sync edit/preview scroll bar
			markdown = StringUtils.replace(markdown, "\r\n", "\n");
			return markdownManager.render(markdown, getBaseUrl(), true);
		} else {
			return "<div class='message'>Nothing to preview</div>";
		}
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
		
		WebMarkupContainer splitIcon = new WebMarkupContainer("icon");
		splitLink.add(splitIcon);

		container.add(AttributeAppender.append("class", compactMode?"compact-mode":"normal-mode"));
			
		edit.add(input = new TextArea<String>("input", Model.of(getModelObject())));
		for (AttributeModifier modifier: getInputModifiers()) {
			input.add(modifier);
		}

		if (initialSplit) {
			container.add(AttributeAppender.append("class", "split-mode"));
			preview.add(new Label("rendered", render(getModelObject())) {

				@Override
				public void renderHead(IHeaderResponse response) {
					super.renderHead(response);
					String script = String.format(
							"gitplex.server.markdown.initRendered($('#%s>.body>.preview>.markdown-rendered'));", 
							container.getMarkupId());
					response.render(OnDomReadyHeaderItem.forScript(script));
				}
				
			}.setEscapeModelStrings(false));
			splitLink.add(AttributeAppender.append("class", "active"));
		} else {
			container.add(AttributeAppender.append("class", "edit-mode"));
			preview.add(new WebMarkupContainer("rendered"));
			editLink.add(AttributeAppender.append("class", "active"));
		}
		
		container.add(new WebMarkupContainer("canAttachFile").setVisible(getAttachmentSupport()!=null));
		
		container.add(ajaxBehavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("markdown-preview", AjaxChannel.Type.DROP));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String action = params.getParameterValue("action").toString(); 
				switch (action) {
				case "render":
					String markdown = params.getParameterValue("param1").toString();
					String rendered = render(markdown);
					String script = String.format("gitplex.server.markdown.onRendered('%s', '%s');", 
							container.getMarkupId(), JavaScriptEscape.escapeJavaScript(rendered));
					target.appendJavaScript(script);
					break;
				case "emojiQuery":
					List<String> emojiNames = new ArrayList<>();
					String emojiQuery = params.getParameterValue("param1").toOptionalString();
					if (StringUtils.isNotBlank(emojiQuery)) {
						emojiQuery = emojiQuery.toLowerCase();
						for (String emojiName: EmojiOnes.getInstance().all().keySet()) {
							if (emojiName.toLowerCase().contains(emojiQuery))
								emojiNames.add(emojiName);
						}
						emojiNames.sort((name1, name2) -> name1.length() - name2.length());
					} else {
						emojiNames.add("smile");
						emojiNames.add("worried");
						emojiNames.add("blush");
						emojiNames.add("+1");
						emojiNames.add("-1");
					}

					List<Map<String, String>> emojis = new ArrayList<>();
					for (String emojiName: emojiNames) {
						if (emojis.size() < ATWHO_LIMIT) {
							String emojiCode = EmojiOnes.getInstance().all().get(emojiName);
							CharSequence url = RequestCycle.get().urlFor(new PackageResourceReference(
									EmojiOnes.class, "icon/" + emojiCode + ".png"), new PageParameters());
							Map<String, String> emoji = new HashMap<>();
							emoji.put("name", emojiName);
							emoji.put("url", url.toString());
							emojis.add(emoji);
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
					emojis = new ArrayList<>();
					for (Map.Entry<String, String> entry: EmojiOnes.getInstance().all().entrySet()) {
						Map<String, String> emoji = new HashMap<>();
						emoji.put("name", entry.getKey());
						emoji.put("url", RequestCycle.get().urlFor(new PackageResourceReference(
								EmojiOnes.class, "icon/" + entry.getValue() + ".png"), new PageParameters()).toString());
						emojis.add(emoji);
					}

					try {
						json = AppLoader.getInstance(ObjectMapper.class).writeValueAsString(emojis);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}

					script = String.format("gitplex.server.markdown.onEmojisLoaded('%s', %s);", container.getMarkupId(), json);
					target.appendJavaScript(script);
					break;
				case "userQuery":
					String userQuery = params.getParameterValue("param1").toOptionalString();

					AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
					List<Map<String, String>> userList = new ArrayList<>();
					for (Account user: getUserMentionSupport().findUsers(userQuery, ATWHO_LIMIT)) {
						Map<String, String> userMap = new HashMap<>();
						userMap.put("name", user.getName());
						if (user.getFullName() != null)
							userMap.put("fullName", user.getFullName());
						if (user.getNoSpaceFullName() != null)
							userMap.put("searchKey", user.getNoSpaceName() + " " + user.getNoSpaceFullName());
						else
							userMap.put("searchKey", user.getNoSpaceName());
						String avatarUrl = avatarManager.getAvatarUrl(user);
						userMap.put("avatarUrl", avatarUrl);
						userList.add(userMap);
					}
					
					try {
						json = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(userList);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					script = String.format("$('#%s').data('atWhoUserRenderCallback')(%s);", container.getMarkupId(), json);
					target.appendJavaScript(script);	
					break;
				case "requestQuery":
					String requestQuery = params.getParameterValue("param1").toOptionalString();

					List<Map<String, String>> requestList = new ArrayList<>();
					for (PullRequest request: getPullRequestReferenceSupport().findRequests(requestQuery, ATWHO_LIMIT)) {
						Map<String, String> requestMap = new HashMap<>();
						requestMap.put("requestNumber", request.getNumberStr());
						requestMap.put("requestTitle", request.getTitle());
						requestMap.put("searchKey", request.getNumberStr() + " " + request.getNoSpaceTitle());
						requestList.add(requestMap);
					}
					
					try {
						json = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(requestList);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					script = String.format("$('#%s').data('atWhoRequestRenderCallback')(%s);", container.getMarkupId(), json);
					target.appendJavaScript(script);
					break;
				case "selectImage":
				case "selectLink":
					BasePage page = (BasePage) getPage();
					InsertUrlPanel urlSelector = new InsertUrlPanel(
							page.getRootComponents().newChildId(), MarkdownEditor.this, action.equals("selectImage"));
					urlSelector.setOutputMarkupId(true);
					page.getRootComponents().add(urlSelector);
					urlSelector.setMarkupId(container.getMarkupId() + "-urlselector");
					target.add(urlSelector);
					break;
				case "insertUrl":
					String name;
					try {
						name = URLDecoder.decode(params.getParameterValue("param1").toString(), Charsets.UTF_8.name());
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
					String replaceMessage = params.getParameterValue("param2").toString();
					String url = getAttachmentSupport().getAttachmentUrl(name);
					insertUrl(target, isWebSafeImage(name), url, name, replaceMessage);
					break;
				default:
					throw new IllegalStateException("Unknown action: " + action);
				}		
			}
			
		});
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
	}

	@Override
	public void convertInput() {
		setConvertedInput(input.getConvertedInput());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
		
		String encodedAttachmentSupport;
		if (getAttachmentSupport() != null) {
			encodedAttachmentSupport = Base64.encodeBase64String(SerializationUtils.serialize(getAttachmentSupport()));
			encodedAttachmentSupport = StringUtils.deleteWhitespace(encodedAttachmentSupport);
			encodedAttachmentSupport = StringEscapeUtils.escapeEcmaScript(encodedAttachmentSupport);
			encodedAttachmentSupport = "'" + encodedAttachmentSupport + "'";
		} else {
			encodedAttachmentSupport = "undefined";
		}
		String callback = ajaxBehavior.getCallbackFunction(explicit("action"), explicit("param1"), explicit("param2"), 
				explicit("param3")).toString();
		
		String autosaveKey = getAutosaveKey();
		if (autosaveKey != null)
			autosaveKey = "'" + JavaScriptEscape.escapeJavaScript(autosaveKey) + "'";
		else
			autosaveKey = "undefined";
		
		String script = String.format("gitplex.server.markdown.onDomReady('%s', %s, %d, %s, %d, %b, %b, %s);", 
				container.getMarkupId(), 
				callback, 
				ATWHO_LIMIT, 
				encodedAttachmentSupport, 
				getAttachmentSupport()!=null?getAttachmentSupport().getAttachmentMaxSize():0,
				getUserMentionSupport() != null,
				getPullRequestReferenceSupport() != null, 
				autosaveKey);
		response.render(OnDomReadyHeaderItem.forScript(script));
		
		script = String.format("gitplex.server.markdown.onWindowLoad('%s');", container.getMarkupId());
		response.render(OnLoadHeaderItem.forScript(script));
	}

	public void insertUrl(AjaxRequestTarget target, boolean isImage, String url, 
			@Nullable String name, String replaceMessage) {
		String script = String.format("gitplex.server.markdown.insertUrl('%s', %s, '%s', %s, %s);",
				container.getMarkupId(), isImage, StringEscapeUtils.escapeEcmaScript(url), 
				name!=null?"'"+StringEscapeUtils.escapeEcmaScript(name)+"'":"undefined", 
				replaceMessage!=null?"'"+replaceMessage+"'":"undefined");
		target.appendJavaScript(script);
	}
	
	public void closeUrlInserter(AjaxRequestTarget target, Component urlInserter) {
		BasePage page = (BasePage) urlInserter.getPage();
		page.getRootComponents().remove(urlInserter);
		String script = String.format("$('#%s-urlselector').closest('.modal').modal('hide');", container.getMarkupId());
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
	protected PullRequestReferenceSupport getPullRequestReferenceSupport() {
		return null;
	}
	
	protected List<AttributeModifier> getInputModifiers() {
		return new ArrayList<>();
	}
	
	protected BlobReferenceSupport getBlobReferenceSupport() {
		return null;
	}
	
	@Nullable
	protected String getAutosaveKey() {
		return null;
	}
	
	protected String getBaseUrl() {
		return null;
	};
	
}
