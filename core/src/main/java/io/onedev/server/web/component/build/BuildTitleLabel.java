package io.onedev.server.web.component.build;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Build;

@SuppressWarnings("serial")
public class BuildTitleLabel extends Label {

	public BuildTitleLabel(String id, IModel<Build> model) {
		super(id, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Build build = model.getObject();
				if (build.getVersion() != null)
					return "#" + build.getNumber() + " (" + build.getVersion() + ")";
				else
					return "#" + build.getNumber();
			}
			
		});
	}

}
