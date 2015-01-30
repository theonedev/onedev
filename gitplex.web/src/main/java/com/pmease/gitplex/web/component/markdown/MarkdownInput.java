package com.pmease.gitplex.web.component.markdown;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.resource.atwho.AtWhoResourceReference;
import com.pmease.commons.wicket.resource.caret.CaretResourceReference;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.MarkdownManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.avatar.AvatarManager;

@SuppressWarnings("serial")
public class MarkdownInput extends FormComponentPanel<String> {

	private TextArea<String> input;
	
	private WebMarkupContainer emojis;
	
	public MarkdownInput(String id, IModel<String> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new TextArea<String>("input", getModel()));
		input.setOutputMarkupId(true);
		
		add(emojis = new WebMarkupContainer("emojis"));
		emojis.setOutputMarkupId(true);

		input.add(new AbstractDefaultAjaxBehavior() {

			private static final int ATWHO_LIMIT = 5;
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String type = params.getParameterValue("type").toString();
				
				if (type.equals("markdownPreview")) {
					String markdown = params.getParameterValue("param").toOptionalString();
					String preview;
					if (StringUtils.isNotBlank(markdown)) {
						preview = GitPlex.getInstance(MarkdownManager.class).toHtml(markdown, true, true);
						preview = StringUtils.replace(preview, "'", "\\'");
						preview = StringUtils.replace(preview, "\n", " ");
					} else { 
						preview = "<i>Nothing to preview.</i>";
					}
					String script = String.format("$('#%s~.md-preview').html('%s');", 
							input.getMarkupId(), preview);
					target.appendJavaScript(script);
				} else if (type.equals("userQuery")) {
					String userQuery = params.getParameterValue("param").toOptionalString();
					Dao dao = GitPlex.getInstance(Dao.class);
					EntityCriteria<User> criteria = EntityCriteria.of(User.class);
					if (StringUtils.isNotBlank(userQuery)) {
						criteria.add(Restrictions.or(
								Restrictions.ilike("name", userQuery, MatchMode.START), 
								Restrictions.ilike("fullName", userQuery, MatchMode.START)));
					}
					criteria.addOrder(Order.asc("name"));

					AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
					List<Map<String, String>> items = new ArrayList<>();
					for (User user: dao.query(criteria, 0, ATWHO_LIMIT)) {
						Map<String, String> item = new HashMap<>();
						item.put("name", user.getName());
						item.put("fullName", user.getFullName());
						String avatarUrl = avatarManager.getAvatarUrl(user);
						item.put("avatarUrl", avatarUrl);
						items.add(item);
					}
					
					String json;
					try {
						json = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(items);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					String script = String.format("var $element = $('#%s');"
							+ "$element[0].atWhoUserRenderCallback(%s);"
							+ "$element[0].cachedMentions[$element[0].atWhoUserQuery] = %s;", 
							input.getMarkupId(), json, json);
					target.appendJavaScript(script);
				} else if (type.equals("emojiQuery")){
					Map<String, String> emojiCodes = new LinkedHashMap<>();
					String emojiQuery = params.getParameterValue("param").toOptionalString();
					if (emojiQuery != null)
						emojiQuery = emojiQuery.toLowerCase();
					for (Map.Entry<String, String> entry: EmojiOnes.getInstance().all().entrySet()) {
						if (emojiCodes.size() < ATWHO_LIMIT 
								&& (StringUtils.isBlank(emojiQuery) || entry.getKey().toLowerCase().startsWith(emojiQuery))) {
							emojiCodes.put(entry.getKey(), entry.getValue());
						}
					}
					
					List<Map<String, String>> items = new ArrayList<>();
					for (Map.Entry<String, String> entry: emojiCodes.entrySet()) {
						CharSequence url = RequestCycle.get().urlFor(new PackageResourceReference(
								EmojiOnes.class, "emoji/" + entry.getValue() + ".png"), new PageParameters());
						Map<String, String> item = new HashMap<>();
						item.put("name", entry.getKey());
						item.put("url", url.toString());
						items.add(item);
					}
					String json;
					try {
						json = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(items);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					String script = String.format("var $element = $('#%s');"
							+ "$element[0].atWhoEmojiRenderCallback(%s);"
							+ "$element[0].cachedEmojis[$element[0].atWhoEmojiQuery] = %s;", 
							input.getMarkupId(), json, json);
					target.appendJavaScript(script);
				} else if (type.equals("toggleEmojis")) {
					if (!(emojis instanceof Fragment)) {
						emojis = new Fragment("emojis", "emojisFrag", MarkdownInput.this);
						emojis.add(new ListView<Emoji>("emojis", new LoadableDetachableModel<List<Emoji>>() {

							@Override
							protected List<Emoji> load() {
								List<Emoji> emojis = new ArrayList<>();
								for (Map.Entry<String, String> entry: EmojiOnes.getInstance().all().entrySet()) {
									Emoji emoji = new Emoji();
									emoji.name = entry.getKey();
									emoji.url = RequestCycle.get().urlFor(new PackageResourceReference(
											EmojiOnes.class, "emoji/" + entry.getValue() + ".png"), new PageParameters()).toString();
									emojis.add(emoji);
								}
								return emojis;
							}
							
						}){

							@Override
							protected void populateItem(ListItem<Emoji> item) {
								Emoji emoji = item.getModelObject();
								WebMarkupContainer link = new WebMarkupContainer("emoji");
								link.add(AttributeAppender.append("name", emoji.name));
								link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("src", emoji.url)));
								item.add(link);
							}
							
						});
						emojis.setOutputMarkupPlaceholderTag(true);
						emojis.setMarkupId(input.getMarkupId() + "-emojis");
						replace(emojis);
						String script = String.format("gitplex.markdown.setupEmojiSelection('%s');", input.getMarkupId());
						target.add(emojis);
						target.appendJavaScript(script);
					}
					target.appendJavaScript(String.format("$('#%s').prevAll('.md-emojis').toggle();", input.getMarkupId()));
				} else {
					throw new IllegalStateException("Unknown callback type: " + type);
				}
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
						new JavaScriptResourceReference(MarkdownInput.class, "bootstrap-markdown.js")));
				response.render(CssHeaderItem.forReference(
						new CssResourceReference(MarkdownInput.class, "bootstrap-markdown.min.css")));
				response.render(JavaScriptHeaderItem.forReference(CaretResourceReference.INSTANCE));
				response.render(JavaScriptHeaderItem.forReference(AtWhoResourceReference.INSTANCE));
				
				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(MarkdownInput.class, "markdown.js")));
				response.render(CssHeaderItem.forReference(
						new CssResourceReference(MarkdownInput.class, "markdown.css")));
				
				String script = String.format("gitplex.markdown.setup('%s', %s, %s);", 
						component.getMarkupId(true), ATWHO_LIMIT,
						getCallbackFunction(CallbackParameter.explicit("type"), CallbackParameter.explicit("param")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
	}

	@Override
	protected void convertInput() {
		setConvertedInput(input.getConvertedInput());
	}

	private static class Emoji {
		String name;
		
		String url;
	}
}
