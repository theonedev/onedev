package com.pmease.gitplex.web.page.test;

import java.io.IOException;

import org.apache.wicket.markup.html.link.Link;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.pmease.commons.git.jackson.ObjectIdDeserializer;
import com.pmease.commons.git.jackson.ObjectIdSerializer;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
		        ObjectMapper mapper = new ObjectMapper();
		        SimpleModule module = new SimpleModule();
				module.addSerializer(ObjectId.class, new ObjectIdSerializer());
				module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
		        mapper.registerModule(module);
				try {
					System.out.println(mapper.writeValueAsString(ObjectId.zeroId()));
					System.out.println(mapper.readValue(ObjectId.zeroId().name(), ObjectId.class));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
	}

}
