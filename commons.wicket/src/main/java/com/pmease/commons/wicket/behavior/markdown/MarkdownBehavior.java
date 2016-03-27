package com.pmease.commons.wicket.behavior.markdown;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.crypt.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.commons.wicket.CommonPage;
import com.pmease.commons.wicket.assets.atwho.AtWhoResourceReference;
import com.pmease.commons.wicket.assets.caret.CaretResourceReference;
import com.pmease.commons.wicket.assets.codemirror.HighlightResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewerResourceReference;

@SuppressWarnings("serial")
public class MarkdownBehavior extends AbstractDefaultAjaxBehavior {

	protected static final int ATWHO_LIMIT = 5;
	
	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
		String type = params.getParameterValue("type").toString();
		
		if (type.equals("markdownPreview")) {
			String markdown = params.getParameterValue("param").toOptionalString();
			String preview;
			if (StringUtils.isNotBlank(markdown)) {
				preview = AppLoader.getInstance(MarkdownManager.class).parseAndProcess(markdown);
			} else { 
				preview = "<i>Nothing to preview.</i>";
			}
			String script = String.format(""
					+ "var $preview=$('#%s~.md-preview');"
					+ "$preview.html('%s');"
					+ "pmease.commons.initMarkdownViewer($preview);",
					getComponent().getMarkupId(), StringEscapeUtils.escapeEcmaScript(preview));
			target.appendJavaScript(script);
		} else if (type.equals("emojiQuery")){
			List<String> emojiNames = new ArrayList<>();
			String emojiQuery = params.getParameterValue("param").toOptionalString();
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
							EmojiOnes.class, "emoji/" + emojiCode + ".png"), new PageParameters());
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
			String script = String.format("$('#%s').data('atWhoEmojiRenderCallback')(%s);",
					getComponent().getMarkupId(), json);
			target.appendJavaScript(script);
		} else if (type.equals("loadEmojis")) {
			List<Map<String, String>> emojis = new ArrayList<>();
			for (Map.Entry<String, String> entry: EmojiOnes.getInstance().all().entrySet()) {
				Map<String, String> emoji = new HashMap<>();
				emoji.put("name", entry.getKey());
				emoji.put("url", RequestCycle.get().urlFor(new PackageResourceReference(
						EmojiOnes.class, "emoji/" + entry.getValue() + ".png"), new PageParameters()).toString());
				emojis.add(emoji);
			}

			String json;
			try {
				json = AppLoader.getInstance(ObjectMapper.class).writeValueAsString(emojis);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}

			String script = String.format("pmease.commons.markdown.onEmojisLoaded('%s', %s);", 
					getComponent().getMarkupId(), json);
			target.appendJavaScript(script);
		} else if (type.equals("selectImage") || type.equals("selectLink")) {
			CommonPage page = (CommonPage) getComponent().getPage();
			SelectUrlPanel urlSelector = new SelectUrlPanel(
					page.getRootComponents().newChildId(), this, type.equals("selectImage"));
			urlSelector.setOutputMarkupId(true);
			page.getRootComponents().add(urlSelector);
			urlSelector.setMarkupId(getComponent().getMarkupId() + "-urlselector");
			target.add(urlSelector);
		} else if (type.equals("insertUrl")) {
			String name;
			try {
				name = URLDecoder.decode(params.getParameterValue("param").toString(), Charsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			String replaceMessage = params.getParameterValue("param2").toString();
			String url = getAttachmentSupport().getAttachmentUrl(name);
			insertUrl(target, isWebSafeImage(name), url, name, replaceMessage);
		} else {
			throw new IllegalStateException("Unknown callback type: " + type);
		}
	}
	
	public void insertUrl(AjaxRequestTarget target, boolean isImage, String url, 
			@Nullable String name, String replaceMessage) {
		String script = String.format("pmease.commons.markdown.insertUrl('%s', %s, '%s', %s, %s);",
				getComponent().getMarkupId(), isImage, StringEscapeUtils.escapeEcmaScript(url), 
				name!=null?"'"+StringEscapeUtils.escapeEcmaScript(name)+"'":"undefined", 
				replaceMessage!=null?"'"+replaceMessage+"'":"undefined");
		target.appendJavaScript(script);
	}
	
	public void closeUrlSelector(AjaxRequestTarget target, Component urlSelector) {
		CommonPage page = (CommonPage) urlSelector.getPage();
		page.getRootComponents().remove(urlSelector);
		String script = String.format("$('#%s-urlselector').closest('.modal').modal('hide');", 
				getComponent().getMarkupId());
		target.appendJavaScript(script);
	}
	
	public boolean isWebSafeImage(String fileName) {
		fileName = fileName.toLowerCase();
		return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") 
				|| fileName.endsWith(".gif") || fileName.endsWith(".png");
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		
		attributes.setMethod(Method.POST);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(MarkdownBehavior.class, "bootstrap-markdown.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(MarkdownBehavior.class, "bootstrap-markdown.min.css")));
		response.render(JavaScriptHeaderItem.forReference(CaretResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(AtWhoResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(HighlightResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(MarkdownBehavior.class, "markdown.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(MarkdownBehavior.class, "markdown.css")));
		response.render(JavaScriptHeaderItem.forReference(MarkdownViewerResourceReference.INSTANCE));
		
		String encodedAttachmentSupport;
		AttachmentSupport attachmentSupport = getAttachmentSupport();
		if (attachmentSupport != null) {
			encodedAttachmentSupport = Base64.encodeBase64String(SerializationUtils.serialize(attachmentSupport));
			encodedAttachmentSupport = StringUtils.deleteWhitespace(encodedAttachmentSupport);
			encodedAttachmentSupport = StringEscapeUtils.escapeEcmaScript(encodedAttachmentSupport);
			encodedAttachmentSupport = "'" + encodedAttachmentSupport + "'";
		} else {
			encodedAttachmentSupport = "undefined";
		}
		String uploadUrl = RequestCycle.get().getUrlRenderer().renderRelativeUrl(Url.parse("attachment_upload"));
		String script = String.format("pmease.commons.markdown.init('%s', %s, %s, '%s', %s, %d);", 
				component.getMarkupId(true), 
				ATWHO_LIMIT,
				getCallbackFunction(explicit("type"), explicit("param"), explicit("param2")), 
				uploadUrl, 
				encodedAttachmentSupport, 
				attachmentSupport!=null?attachmentSupport.getAttachmentMaxSize():0);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Nullable
	public AttachmentSupport getAttachmentSupport() {
		return null;
	}

}
