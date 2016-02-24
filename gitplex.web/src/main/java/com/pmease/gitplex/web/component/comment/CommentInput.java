package com.pmease.gitplex.web.component.comment;

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
import com.pmease.commons.wicket.behavior.markdown.MarkdownBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.web.avatar.AvatarManager;

@SuppressWarnings("serial")
public class CommentInput extends TextArea<String> {

	private final IModel<PullRequest> requestModel;
	
	public CommentInput(String id, IModel<PullRequest> requestModel, IModel<String> contentModel) {
		super(id, contentModel);
		
		this.requestModel = requestModel;
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
						if (user.getNoSpaceFullName() != null)
							userMap.put("searchKey", user.getNoSpaceName() + " " + user.getNoSpaceFullName());
						else
							userMap.put("searchKey", user.getNoSpaceName());
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
					String script = String.format("$('#%s').data('atWhoUserRenderCallback')(%s);",
							getComponent().getMarkupId(), json);
					target.appendJavaScript(script);
				} else if (type.equals("requestQuery")) {
					String requestQuery = params.getParameterValue("param").toOptionalString();

					List<Map<String, String>> requestList = new ArrayList<>();
					for (PullRequest request: queryRequests(requestQuery, ATWHO_LIMIT)) {
						Map<String, String> requestMap = new HashMap<>();
						requestMap.put("requestId", request.getIdStr());
						requestMap.put("requestTitle", request.getTitle());
						requestMap.put("searchKey", request.getIdStr() + " " + request.getNoSpaceTitle());
						requestList.add(requestMap);
					}
					
					String json;
					try {
						json = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(requestList);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
					String script = String.format("$('#%s').data('atWhoRequestRenderCallback')(%s);", 
							getComponent().getMarkupId(), json);
					target.appendJavaScript(script);
				} else {
					super.respond(target);
				}
			}

			@Override
			public AttachmentSupport getAttachmentSupport() {
				return new CommentAttachmentSupport(requestModel.getObject().getId());
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
					Restrictions.ilike("noSpaceName", query, MatchMode.ANYWHERE), 
					Restrictions.ilike("noSpaceFullName", query, MatchMode.ANYWHERE)));
		}
		criteria.addOrder(Order.asc("name"));
		return GitPlex.getInstance(Dao.class).query(criteria, 0, count);
	}

	protected List<PullRequest> queryRequests(String query, int count) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		if (StringUtils.isNotBlank(query)) {
			query = StringUtils.deleteWhitespace(query);
			criteria.add(Restrictions.or(
					Restrictions.ilike("noSpaceTitle", query, MatchMode.ANYWHERE), 
					Restrictions.ilike("idStr", query, MatchMode.START)));
		}
		criteria.addOrder(Order.desc("id"));
		return GitPlex.getInstance(Dao.class).query(criteria, 0, count);
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}	
	
}
