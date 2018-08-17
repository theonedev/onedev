package io.onedev.server.web.component;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Build;

@SuppressWarnings("serial")
public class BuildStatusLabel extends Label {

	private final IModel<Build> buildModel;
	
	public BuildStatusLabel(String id, IModel<Build> buildModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return buildModel.getObject().getStatus().getDescription();
			}
			
		});
		this.buildModel = buildModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Build build = getBuild();
				if (build.getStatus() == Build.Status.ERROR || build.getStatus() == Build.Status.FAILURE)
					return "label label-danger request-status referenceable-status";
				else if (build.getStatus() == Build.Status.RUNNING)
					return "label label-warning request-status referenceable-status";
				else 
					return "label label-success request-status referenceable-status";
			}
			
		}));
	}

	private Build getBuild() {
		return buildModel.getObject();
	}

	@Override
	protected void onDetach() {
		buildModel.detach();
		super.onDetach();
	}
	
}
