package io.onedev.server.web.component.coveragebar;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.util.Coverage;

@SuppressWarnings("serial")
public class CoverageBar extends GenericPanel<Coverage> {

	public CoverageBar(String id, IModel<Coverage> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("coverage").add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (getCoverage().getPercent() < 50)
					return "bg-danger";
				else if (getCoverage().getPercent() < 75)
					return "bg-warning";
				else
					return "bg-success";
			}
			
		})).add(AttributeAppender.append("style", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return "width: " + getCoverage().getPercent() + "%";
			}
			
		})));
	}
	
	private Coverage getCoverage() {
		return getModelObject();
	}

}
