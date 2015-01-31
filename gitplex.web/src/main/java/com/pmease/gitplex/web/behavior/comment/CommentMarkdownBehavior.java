package com.pmease.gitplex.web.behavior.comment;

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
import com.pmease.commons.wicket.behavior.markdown.MarkdownBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.avatar.AvatarManager;

@SuppressWarnings("serial")
public class CommentMarkdownBehavior extends MarkdownBehavior {

	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
		String type = params.getParameterValue("type").toString();
		
		if (type.equals("userQuery")) {
			String userQuery = params.getParameterValue("param").toOptionalString();
			Dao dao = GitPlex.getInstance(Dao.class);
			EntityCriteria<User> criteria = EntityCriteria.of(User.class);
			if (StringUtils.isNotBlank(userQuery)) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("name", userQuery, MatchMode.ANYWHERE), 
						Restrictions.ilike("fullName", userQuery, MatchMode.ANYWHERE)));
			}
			criteria.addOrder(Order.asc("name"));

			AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
			List<Map<String, String>> userList = new ArrayList<>();
			for (User user: dao.query(criteria, 0, ATWHO_LIMIT)) {
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
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(CommentMarkdownBehavior.class, "comment-markdown.js")));
		String script = String.format("gitplex.markdown.comment('%s', %s, %s);", 
				component.getMarkupId(true), ATWHO_LIMIT,
				getCallbackFunction(CallbackParameter.explicit("type"), CallbackParameter.explicit("param")));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
