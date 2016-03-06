package com.pmease.gitplex.web.page.test;

import java.io.IOException;

import org.apache.wicket.markup.html.link.Link;
import org.eclipse.jgit.lib.ObjectId;

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
		        ObjectMapper mapper = GitPlex.getInstance(ObjectMapper.class);
				try {
					System.out.println(mapper.writeValueAsString(ObjectId.zeroId()));
					System.out.println(mapper.readValue(mapper.writeValueAsString(ObjectId.zeroId()), ObjectId.class));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
	}

}
