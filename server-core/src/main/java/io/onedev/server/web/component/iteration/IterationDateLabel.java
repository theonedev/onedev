package io.onedev.server.web.component.iteration;

import static io.onedev.server.util.DateUtils.formatDate;
import static io.onedev.server.util.DateUtils.toDate;
import static io.onedev.server.web.translation.Translation._T;
import static java.time.LocalDate.ofEpochDay;

import java.time.LocalDate;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Iteration;
import io.onedev.server.web.component.svg.SpriteImage;

public class IterationDateLabel extends Label {

	private final IModel<Iteration> iterationIModel;
	
	public IterationDateLabel(String id, IModel<Iteration> iterationIModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String arrow = "<svg class='icon'><use xlink:href='" + SpriteImage.getVersionedHref("arrow3") + "'/></svg>";
				Iteration iteration = iterationIModel.getObject();
				if (iteration.getStartDay() != null && iteration.getDueDay() != null) {
					return ""
							+ "<span data-tippy-content='" + _T("Start date") + "'>" + formatDate(toDate(ofEpochDay(iteration.getStartDay()).atStartOfDay())) + "</span>" 
							+ " " + arrow + " "  
							+ "<span data-tippy-content='" + _T("Due date") + "'>" + formatDate(toDate(ofEpochDay(iteration.getDueDay()).atStartOfDay())) + "</span>";
				} else if (iteration.getStartDay() != null) {
					return "<span data-tippy-content='" + _T("Start date") + "'>" + formatDate(toDate(ofEpochDay(iteration.getStartDay()).atStartOfDay())) + "</span> " + arrow;
				} else if (iteration.getDueDay() != null) {
					return arrow + " <span data-tippy-content='" + _T("Due date") + "'>" + formatDate(toDate(ofEpochDay(iteration.getDueDay()).atStartOfDay())) + "</span>";
				} else {
					return "<i>" + _T("No start/due date") + "</i>";
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
					var today = LocalDate.now().toEpochDay();
					if (iteration.getStartDay() != null && iteration.getDueDay() != null) {
						if (today < iteration.getStartDay())
							return "";
						else if (today >= iteration.getStartDay() && today < iteration.getDueDay())
							return "text-warning";
						else
							return "text-danger";
					} else if (iteration.getStartDay() != null) {
						if (today < iteration.getStartDay())
							return "";
						else 
							return "text-warning";
					} else if (iteration.getDueDay() != null) {
						if (today < iteration.getDueDay())
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
