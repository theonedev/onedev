package io.onedev.server.web.component.iteration;

import java.util.Date;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Iteration;
import io.onedev.server.util.date.DateUtils;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class IterationDateLabel extends Label {

	private final IModel<Iteration> iterationIModel;
	
	public IterationDateLabel(String id, IModel<Iteration> iterationIModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String arrow = "<svg class='icon'><use xlink:href='" + SpriteImage.getVersionedHref("arrow3") + "'/></svg>";
				Iteration iteration = iterationIModel.getObject();
				if (iteration.getStartDate() != null && iteration.getDueDate() != null) {
					return ""
							+ "<span title='Start date'>" + DateUtils.formatDate(iteration.getStartDate()) + "</span>" 
							+ " " + arrow + " "  
							+ "<span title='Due date'>" + DateUtils.formatDate(iteration.getDueDate()) + "</span>";
				} else if (iteration.getStartDate() != null) {
					return "<span title='Start date'>" + DateUtils.formatDate(iteration.getStartDate()) + "</span> " + arrow;
				} else if (iteration.getDueDate() != null) {
					return arrow + " <span title='Due date'>" + DateUtils.formatDate(iteration.getDueDate()) + "</span>";
				} else {
					return "<i> No start/due date</i>";
				}
			}
			
		});
		this.iterationIModel = iterationIModel;
	}

	@Override
	protected void onDetach() {
		iterationIModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Iteration iteration = iterationIModel.getObject();
				if (!iteration.isClosed()) {
					Date now = new Date();
					if (iteration.getStartDate() != null && iteration.getDueDate() != null) {
						if (now.before(iteration.getStartDate()))
							return "";
						else if (now.after(iteration.getStartDate()) && now.before(iteration.getDueDate()))
							return "text-warning";
						else
							return "text-danger";
					} else if (iteration.getStartDate() != null) {
						if (now.before(iteration.getStartDate()))
							return "";
						else 
							return "text-warning";
					} else if (iteration.getDueDate() != null) {
						if (now.before(iteration.getDueDate()))
							return "";
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
