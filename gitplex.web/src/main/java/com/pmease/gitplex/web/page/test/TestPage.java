package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.loader.Plugin;
import com.pmease.commons.loader.PluginManager;
import com.pmease.commons.wicket.page.CommonPage;
import com.pmease.gitplex.core.GitPlex;

@SuppressWarnings("serial")
public class TestPage extends CommonPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				for (Plugin plugin: GitPlex.getInstance(PluginManager.class).getPlugins()) {
					System.out.println(plugin.getId() + ":" + plugin.getVersion());
				}
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

}
