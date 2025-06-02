package io.onedev.server.web.component.iteration;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Iteration;

public class IterationStatusLabel extends Label {

	private final IModel<Iteration> iterationModel;
	
	public IterationStatusLabel(String id, IModel<Iteration> iterationModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return _T(iterationModel.getObject().getStatusName());
			}
			
		});
		this.iterationModel = iterationModel;
	}

	@Override
	protected void onDetach() {
		iterationModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "badge badge-sm badge-" + (iterationModel.getObject().isClosed()? "success": "warning");
			}
			
		}));		
	}

}
