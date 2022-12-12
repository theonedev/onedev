package io.onedev.server.web.page.admin.buildsetting.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.AgentToken;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;

@SuppressWarnings("serial")
class AddAgentPanel extends Panel {

	public AddAgentPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new AjaxActionTab(Model.of("Run via Docker Container")) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component content = newDockerInstructions();
				target.add(content);
				AddAgentPanel.this.replace(content);
			}
			
		});
		tabs.add(new AjaxActionTab(Model.of("Run on Bare Metal/Virtual Machine")) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component content = newBareMetalInstructions();
				target.add(content);
				AddAgentPanel.this.replace(content);
			}
			
		});

		add(new Tabbable("tabs", tabs));
		
		add(newDockerInstructions());
	}
	
	private Component newDockerInstructions() {
		Fragment fragment = new Fragment("instructions", "dockerInstructionsFrag", this);
		fragment.add(new AjaxLink<Void>("showCommand") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				AgentToken token = new AgentToken();
				token.setValue(UUID.randomUUID().toString());
				OneDev.getInstance(AgentTokenManager.class).save(token);
				StringBuilder builder = new StringBuilder("docker run -t -v /var/run/docker.sock:/var/run/docker.sock -v $(pwd)/agent/work:/agent/work -e serverUrl=");
				builder.append(OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl());
				builder.append(" -e agentToken=").append(token.getValue()).append(" -h myagent").append(" 1dev/agent");
				Fragment commandFragment = new Fragment("command", "dockerCommandFrag", AddAgentPanel.this);
				commandFragment.add(new Label("command", builder.toString()));
				commandFragment.add(new CopyToClipboardLink("copy", Model.of(builder.toString())));
				fragment.replace(commandFragment);
				target.add(fragment);
			}
			
		});
		fragment.add(new WebMarkupContainer("command").setVisible(false));
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private Component newBareMetalInstructions() {
		Fragment fragment = new Fragment("instructions", "bareMetalInstructionsFrag", this);
		fragment.add(new ExternalLink("agent", "/~downloads/agent.zip"));
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
}
