package io.onedev.server.web.component.createtag;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.security.SecurityUtils;

@SuppressWarnings("serial")
public abstract class CreateTagPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final String revision;
	
	private String tagName;
	
	private String tagMessage;
	
	public CreateTagPanel(String id, IModel<Project> projectModel, @Nullable String tagName, String revision) {
		super(id);
		this.projectModel = projectModel;
		this.tagName = tagName;
		this.revision = revision;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		form.add(new NotificationPanel("feedback", form));
		
		Component nameInput;
		if (tagName == null) {
			form.add(nameInput = new TextField<String>("name", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return tagName;
				}

				@Override
				public void setObject(String object) {
					tagName = object;
				}
				
			}));
			nameInput.setOutputMarkupId(true);
		} else {
			form.add(nameInput = new WebMarkupContainer("name").setVisible(false));
		}
		
		form.add(new TextArea<String>("message", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return tagMessage;
			}

			@Override
			public void setObject(String object) {
				tagMessage = object;
			}
			
		}));
		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (tagName == null) {
					form.error("Tag name is required.");
					target.focusComponent(nameInput);
					target.add(form);
				} else if (!Repository.isValidRefName(Constants.R_TAGS + tagName)) {
					form.error("Tag name is not valid.");
					target.focusComponent(nameInput);
					target.add(form);
				} else if (projectModel.getObject().getObjectId(GitUtils.tag2ref(tagName), false) != null) {
					form.error("Tag '" + tagName + "' already exists, please choose a different name.");
					target.focusComponent(nameInput);
					target.add(form);
				} else {
					Project project = projectModel.getObject();
					User user = Preconditions.checkNotNull(SecurityUtils.getUser());

					TagProtection protection = project.getTagProtection(tagName, user);
					if (protection.isPreventCreation()) {
						form.error("Unable to create protected tag");
						target.focusComponent(nameInput);
						target.add(form);
					} else {
						project.createTag(tagName, revision, user.asPerson(), tagMessage);
						onCreate(target, tagName);
					}
				}
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		add(form);
	}
	
	protected abstract void onCreate(AjaxRequestTarget target, String tag);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	protected void onDetach() {
		projectModel.detach();
		
		super.onDetach();
	}

}
