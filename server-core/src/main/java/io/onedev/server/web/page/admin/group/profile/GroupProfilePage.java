package io.onedev.server.web.page.admin.group.profile;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.group.GroupListPage;
import io.onedev.server.web.page.admin.group.GroupPage;
import io.onedev.server.web.util.ConfirmClickModifier;

@SuppressWarnings("serial")
public class GroupProfilePage extends GroupPage {

	private BeanEditor editor;
		
	private String oldName;
	
	public GroupProfilePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		editor = BeanContext.editModel("editor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getGroup();
			}

			@Override
			public void setObject(Serializable object) {
				// check contract of GroupManager.save on why we assign oldName here
				oldName = getGroup().getName();
				editor.getDescriptor().copyProperties(object, getGroup());
			}
			
		});

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Group group = getGroup();
				GroupManager groupManager = OneDev.getInstance(GroupManager.class);
				Group groupWithSameName = groupManager.find(group.getName());
				if (groupWithSameName != null && !groupWithSameName.equals(group)) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another group.");
				} 
				if (editor.isValid()) {
					groupManager.save(group, oldName);
					setResponsePage(GroupProfilePage.class, GroupProfilePage.paramsOf(group));
					Session.get().success("Profile updated");
				}
			}
			
		};	
		form.add(editor);
		form.add(new FencedFeedbackPanel("feedback", form).setEscapeModelStrings(false));
		
		form.add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				OneDev.getInstance(GroupManager.class).delete(getGroup());
				Session.get().success("Group '" + getGroup().getName() + "' deleted");
				
				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Group.class);
				if (redirectUrlAfterDelete != null)
					throw new RedirectToUrlException(redirectUrlAfterDelete);
				else
					setResponsePage(GroupListPage.class);
			}
			
		}.add(new ConfirmClickModifier("Do you really want to delete group '" + getGroup().getName() + "'?")));
		
		add(form);
	}

}
