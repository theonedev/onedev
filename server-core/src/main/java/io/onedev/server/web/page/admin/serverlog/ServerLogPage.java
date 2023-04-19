package io.onedev.server.web.page.admin.serverlog;

import com.google.common.base.Joiner;
import io.onedev.server.web.page.admin.ServerDetailPage;
import io.onedev.server.web.resource.ServerLogResource;
import io.onedev.server.web.resource.ServerLogResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

@SuppressWarnings("serial")
public class ServerLogPage extends ServerDetailPage {

	private static final int MAX_DISPLAY_LINES = 5000;
	
	public ServerLogPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ResourceLink<Void>("download", new ServerLogResourceReference(), 
				ServerLogResource.paramsOf(server)));
		
		List<String> lines = ServerLogResource.readServerLog(server);		
		String content;
		if (lines.size() > MAX_DISPLAY_LINES) {
			add(new Label("warning", "Too many log entries, displaying recent " + MAX_DISPLAY_LINES));
			content = Joiner.on("\n").join(lines.subList(lines.size()-MAX_DISPLAY_LINES, lines.size()));
		} else {
			add(new WebMarkupContainer("warning").setVisible(false));
			content = Joiner.on("\n").join(lines);
		}
		
		add(new Label("logContent", content));
	}

	@Override
	protected String newTopbarTitle() {
		return "Server Log";
	}

}
