package io.onedev.server.web.component;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Agent;

public class AgentStatusBadge extends Label {

	private final IModel<Agent> agentModel;
	
	public AgentStatusBadge(String id, IModel<Agent> agentModel) {
		super(id);
		this.agentModel = agentModel;
		setDefaultModel(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String status;
				if (getAgent().isOnline())  
					status = _T("Online");
				else 
					status = _T("Offline");
				
				if (getAgent().isPaused()) 
					status += " / " + _T("Paused");
				return status;
			}
			
		});
	}
	
	private Agent getAgent() {
		return agentModel.getObject();
	}

	@Override
	protected void onDetach() {
		agentModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (getAgent().isPaused()) {
					if (getAgent().isOnline())
						return "badge badge-warning";
					else
						return "badge badge-danger";
				} else {
					if (getAgent().isOnline())
						return "badge badge-success";
					else
						return "badge badge-danger";
				}
			}
			
		}));
	}

}
