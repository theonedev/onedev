package io.onedev.server.web.component.createtag;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

public abstract class CreateTagPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final String revision;
	
	private CreateTagBean helperBean = new CreateTagBean();
	
	public CreateTagPanel(String id, IModel<Project> projectModel, @Nullable String tagName, String revision) {
		super(id);
		this.projectModel = projectModel;
		this.revision = revision;
		helperBean.setName(tagName);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		form.add(new FencedFeedbackPanel("feedback", form));
		
		BeanEditor editor;
		form.add(editor = BeanContext.edit("editor", helperBean));
		
		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				Project project = projectModel.getObject();
				String tagName = helperBean.getName();
				User user = SecurityUtils.getAuthUser();
				if (project.getObjectId(GitUtils.tag2ref(tagName), false) != null) {
					editor.error(new Path(new PathNode.Named("name")), 
							MessageFormat.format(_T("Tag \"{0}\" already exists, please choose a different name."), helperBean.getName()));
					target.add(form);
				} else if (project.getTagProtection(tagName, user).isPreventCreation()) {
					editor.error(new Path(new PathNode.Named("name")), _T("Unable to create protected tag")); 
					target.add(form);
				} else {
					try {
						OneDev.getInstance(GitService.class).createTag(project, tagName, revision, user.asPerson(), 
								helperBean.getMessage(), project.isTagSignatureRequired(user, tagName));
						onCreate(target, tagName);
					} catch (Exception e) {
						ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
						if (explicitException != null) {
							editor.error(new Path(new PathNode.Named("name")), explicitException.getMessage());
							target.add(form);
						} else {
							throw ExceptionUtils.unchecked(e);
						}
					}
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
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
