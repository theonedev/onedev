package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.jackson.ExternalView;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequest;

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
				ObjectMapper mapper = GitPlex.getInstance(ObjectMapper.class);
				mapper.setConfig(mapper.getSerializationConfig().withView(ExternalView.class));
				PullRequest request = GitPlex.getInstance(Dao.class).load(PullRequest.class, 11L);
				try {
					mapper.writeValueAsString(request);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}

}
