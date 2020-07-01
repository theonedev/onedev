package io.onedev.server.web.component.milestone;

import java.util.Date;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Milestone;
import io.onedev.server.util.DateUtils;

@SuppressWarnings("serial")
public class MilestoneDueLabel extends Label {

	private final IModel<Milestone> milestoneModel;
	
	public MilestoneDueLabel(String id, IModel<Milestone> milestoneModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Milestone milestone = milestoneModel.getObject();
				if (milestone.getDueDate() != null)
					return DateUtils.formatDate(milestone.getDueDate());
				else
					return "<i>No Due Date</i>";
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

		add(AttributeAppender.append("style", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Milestone milestone = milestoneModel.getObject();
				if (!milestone.isClosed() 
						&& milestone.getDueDate() != null 
						&& milestone.getDueDate().before(new Date())) { 
					return "color: red;";		
				} else {
					return "";
				}
			}
			
		}));
		
		setEscapeModelStrings(milestoneModel.getObject().getDueDate() != null);
	}

}
