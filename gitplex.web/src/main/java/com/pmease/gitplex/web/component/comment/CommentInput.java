package com.pmease.gitplex.web.component.comment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.commons.wicket.behavior.markdown.InsertImagePanel;
import com.pmease.commons.wicket.behavior.markdown.MarkdownBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.pmease.gitplex.web.resource.AttachmentResource;
import com.pmease.gitplex.web.resource.AttachmentResourceReference;

@SuppressWarnings("serial")
public class CommentInput extends TextArea<String> {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<String> contentModel;
	
	public CommentInput(String id, IModel<PullRequest> requestModel, IModel<String> contentModel) {
		super(id);
		
		this.requestModel = requestModel;
		this.contentModel = contentModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new MarkdownBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String type = params.getParameterValue("type").toString();
				
				if (type.equals("userQuery")) {
					String userQuery = params.getParameterValue("param").toOptionalString();

					AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
					List<Map<String, String>> userList = new ArrayList<>();
					for (User user: queryUsers(userQuery, ATWHO_LIMIT)) {
						Map<String, String> userMap = new HashMap<>();
						userMap.put("name", user.getName());
						userMap.put("fullName", user.getFullName());
						if (user.getFullName() != null)
							userMap.put("searchKey", user.getName() + "~" + user.getFullName());
						else
							userMap.put("searchKey", user.getName());
						String avatarUrl = avatarManager.getAvatarUrl(user);
						userMap.put("avatarUrl", avatarUrl);
						userList.add(userMap);
					}
					
					String json;
					try {
						json = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(userList);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					String script = String.format("var $input = $('#%s');"
							+ "$input[0].atWhoUserRenderCallback(%s);"
							+ "$input[0].cachedUsers[$input[0].atWhoUserQuery] = %s;", 
							getComponent().getMarkupId(), json, json);
					target.appendJavaScript(script);
				} else {
					super.respond(target);
				}
			}

			@Override
			protected Component newInsertImagePanel(String id) {
				return new InsertImagePanel(id, this) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new AttachmentSupport() {

							@Override
							public File getStoreDir() {
								return GitPlex.getInstance(StorageManager.class).getAttachmentsDir(requestModel.getObject());
							}

							@Override
							public String getAttachmentUrl(String attachment) {
								PageParameters params = AttachmentResource.paramsOf(requestModel.getObject(), attachment);
								return RequestCycle.get().urlFor(new AttachmentResourceReference(), params).toString();
							}
							
						};
					}
					
				};
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(CommentInput.class, "comment-input.js")));
				String script = String.format("gitplex.comment('%s', %s, %s);", 
						component.getMarkupId(true), ATWHO_LIMIT,
						getCallbackFunction(CallbackParameter.explicit("type"), CallbackParameter.explicit("param")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});
	}
	
	protected List<User> queryUsers(String query, int count) {
		EntityCriteria<User> criteria = EntityCriteria.of(User.class);
		if (StringUtils.isNotBlank(query)) {
			criteria.add(Restrictions.or(
					Restrictions.ilike("name", query, MatchMode.ANYWHERE), 
					Restrictions.ilike("fullName", query, MatchMode.ANYWHERE)));
		}
		criteria.addOrder(Order.asc("name"));
		return GitPlex.getInstance(Dao.class).query(criteria, 0, count);
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		contentModel.detach();
		
		super.onDetach();
	}	
	
}
