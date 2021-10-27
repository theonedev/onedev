package io.onedev.server.web.component.milestone;

import java.util.Date;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Milestone;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class MilestoneDateLabel extends Label {

	private final IModel<Milestone> milestoneModel;
	
	public MilestoneDateLabel(String id, IModel<Milestone> milestoneModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String arrow = "<svg class='icon'><use xlink:href='" + SpriteImage.getVersionedHref("arrow3") + "'/></svg>";
				Milestone milestone = milestoneModel.getObject();
				if (milestone.getStartDate() != null && milestone.getDueDate() != null) {
					return ""
							+ "<span title='Start date'>" + DateUtils.formatDate(milestone.getStartDate()) + "</span>" 
							+ " " + arrow + " "  
							+ "<span title='Due date'>" + DateUtils.formatDate(milestone.getDueDate()) + "</span>";
				} else if (milestone.getStartDate() != null) {
					return "<span title='Start date'>" + DateUtils.formatDate(milestone.getStartDate()) + "</span> " + arrow;
				} else if (milestone.getDueDate() != null) {
					return arrow + " <span title='Due date'>" + DateUtils.formatDate(milestone.getDueDate()) + "</span>";
				} else {
					return "<i> No Start/Due Date</i>";
				}
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

		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Milestone milestone = milestoneModel.getObject();
				if (!milestone.isClosed()) {
					Date now = new Date();
					if (milestone.getStartDate() != null && milestone.getDueDate() != null) {
						if (now.before(milestone.getStartDate()))
							return "text-info";
						else if (now.after(milestone.getStartDate()) && now.before(milestone.getDueDate()))
							return "text-warning";
						else
							return "text-danger";
					} else if (milestone.getStartDate() != null) {
						if (now.before(milestone.getStartDate()))
							return "text-info";
						else 
							return "text-warning";
					} else if (milestone.getDueDate() != null) {
						if (now.before(milestone.getDueDate()))
							return "text-info";
						else 
							return "text-danger";
					} else {
						return "";
					}
				} else {
					return "";
				}
			}
			
		}));
		
		setEscapeModelStrings(false);
	}

}
