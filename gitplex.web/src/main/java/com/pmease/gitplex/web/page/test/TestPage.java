package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				System.out.println(ObjectId.zeroId().name());
				ObjectMapper mapper = GitPlex.getInstance(ObjectMapper.class);
				try {
					System.out.println(mapper.writeValueAsString(ObjectId.zeroId()));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}

}
