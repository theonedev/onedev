package io.onedev.server.web.page.admin;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;

public abstract class ServerDetailPage extends AdministrationPage {

	public static final String PARAM_SERVER = "server";

	protected final String server;

	public ServerDetailPage(PageParameters params) {
		super(params);
		server = params.get(PARAM_SERVER).toString();
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new Label("title", newTopbarTitle()));
		fragment.add(new Label("server", server).setVisible(server != null));
		return fragment;
	}
	
	protected abstract String newTopbarTitle();
	
	public static PageParameters paramsOf(@Nullable String server) {
		var params = new PageParameters();
		if (server != null)
			params.add(PARAM_SERVER, server);
		return params;
	}
	
}
