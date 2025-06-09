package io.onedev.server.web.page.admin.serverlog;

import static io.onedev.agent.job.LogRequest.toZoneId;
import static io.onedev.server.util.DateUtils.getZoneId;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Joiner;

import io.onedev.server.web.page.admin.ServerDetailPage;
import io.onedev.server.web.resource.ServerLogResource;
import io.onedev.server.web.resource.ServerLogResourceReference;

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
			add(new Label("warning", MessageFormat.format(_T("Too many log entries, displaying recent {0}"), MAX_DISPLAY_LINES)));
			content = Joiner.on("\n").join(toZoneId(lines.subList(lines.size()-MAX_DISPLAY_LINES, lines.size()), getZoneId()));
		} else {
			add(new WebMarkupContainer("warning").setVisible(false));
			content = Joiner.on("\n").join(toZoneId(lines, getZoneId()));
		}
		
		add(new Label("logContent", content));
	}

	@Override
	protected String newTopbarTitle() {
		return _T("Server Log");
	}

}
