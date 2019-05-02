package io.onedev.server.web.component.build.status;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Build;

@SuppressWarnings("serial")
public class BuildStatusLabel extends Label {

	public BuildStatusLabel(String id, IModel<Build> model) {
		super(id, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return StringUtils.capitalize(model.getObject().getStatus().name().replace('_', ' ').toLowerCase());
			}

			@Override
			public void detach() {
				model.detach();
				super.detach();
			}
			
		});
	}

}
