package io.onedev.server.web.component.job.secret;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.model.support.JobSecret;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public abstract class JobSecretListPanel extends Panel {

	private final JobSecretsBean bean;
	
	public JobSecretListPanel(String id, JobSecretsBean bean) {
		super(id);
		this.bean = bean;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "secrets");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Set<String> names = new HashSet<>();
				for (JobSecret secret: bean.getSecrets()) {
					if (names.contains(secret.getName())) {
						error("Duplicate name found: " + secret.getName());
						return;
					} else {
						names.add(secret.getName());
					}
				}
				getSession().success("Job secrets saved");
				onSaved(bean.getSecrets());
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	protected abstract void onSaved(List<JobSecret> secrets);
}
