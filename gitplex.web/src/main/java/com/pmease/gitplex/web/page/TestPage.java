package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequestCommentReply;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	public TestPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				Dao dao = GitPlex.getInstance(Dao.class);
				System.out.println(dao.query(EntityCriteria.of(PullRequestCommentReply.class)).size());
			}
			
		});
	}
}
