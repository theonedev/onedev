package io.onedev.server.web.page.admin.buildsetting.agent;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Agent;
import io.onedev.server.web.component.svg.SpriteImage;

public class AgentIcon extends SpriteImage {

	private final IModel<Agent> agentModel;
	
	public AgentIcon(String id, IModel<Agent> agentModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String osName = agentModel.getObject().getOsName().toLowerCase();
				if (osName.contains("windows"))
					return "windows";
				else if (osName.contains("linux"))
					return "linux";
				else if (osName.contains("mac"))
					return "macosx";
				else if (osName.contains("freebsd"))
					return "freebsd";
				else
					return "computer";
			}
			
		});
		this.agentModel = agentModel;
	}

	@Override
	protected void onDetach() {
		agentModel.detach();
		super.onDetach();
	}

}
