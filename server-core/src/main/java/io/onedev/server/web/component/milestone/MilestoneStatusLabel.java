package io.onedev.server.web.component.milestone;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Milestone;

@SuppressWarnings("serial")
public class MilestoneStatusLabel extends Label {

	private final IModel<Milestone> milestoneModel;
	
	public MilestoneStatusLabel(String id, IModel<Milestone> milestoneModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return milestoneModel.getObject().getStatusName();
			}
			
		});
		this.milestoneModel = milestoneModel;
	}

	@Override
	protected void onDetach() {
		milestoneModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "badge badge-" + (milestoneModel.getObject().isClosed()? "success": "warning");
			}
			
		}));		
	}

}
