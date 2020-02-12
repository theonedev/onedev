package io.onedev.server.web.component.build.perservation;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.model.support.build.BuildPreservation;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public abstract class BuildPreservationListPanel extends Panel {

	private final BuildPreservationsBean bean;
	
	public BuildPreservationListPanel(String id, BuildPreservationsBean bean) {
		super(id);
		this.bean = bean;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "buildPreservations");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSession().success("Build preserve rules saved");
				onSaved(bean.getBuildPreservations());
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	protected abstract void onSaved(List<BuildPreservation> buildPreservations);
}
