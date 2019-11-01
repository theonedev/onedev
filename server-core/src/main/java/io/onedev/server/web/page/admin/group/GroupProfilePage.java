package io.onedev.server.web.page.admin.group;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.Path;
import io.onedev.server.web.editable.PathNode;
import io.onedev.server.web.util.ConfirmOnClick;

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
				try {
					OneDev.getInstance(GroupManager.class).delete(getGroup());
					setResponsePage(GroupListPage.class);
				} catch (OneException e) {
					error(HtmlUtils.formatAsHtml(e.getMessage()));
				}
			}
			
		}.add(new ConfirmOnClick("Do you really want to delete group '" + getGroup().getName() + "'?")));
		
		add(form);
	}

}
