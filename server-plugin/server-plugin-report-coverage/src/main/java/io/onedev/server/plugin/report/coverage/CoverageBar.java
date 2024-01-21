package io.onedev.server.plugin.report.coverage;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

@SuppressWarnings("serial")
public class CoverageBar extends GenericPanel<Integer> {

	public CoverageBar(String id, IModel<Integer> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("coverage").add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (getCoverage() < 50)
					return "bg-danger";
				else if (getCoverage() < 75)
					return "bg-warning";
				else
					return "bg-success";
			}
			
		})).add(AttributeAppender.append("style", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return "width: " + getCoverage() + "%";
			}
			
		})));
	}
	
	private int getCoverage() {
		return getModelObject();
	}

}
