package io.onedev.server.web.component.user;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.project.ProjectQueryLexer;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.modal.confirm.ConfirmModal;
import io.onedev.server.web.component.modal.message.MessageModal;
import io.onedev.server.web.page.project.ProjectListPage;

@SuppressWarnings("serial")
public abstract class UserDeleteLink extends AjaxLink<Void> {

	public UserDeleteLink(String id) {
		super(id);
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		if (!getUser().getProjects().isEmpty()) {
			new MessageModal(target) {

				@Override
				protected Component newMessageContent(String componentId) {
					String query = Criteria.quote(Project.FIELD_OWNER) + " " 
							+ ProjectQuery.getRuleName(ProjectQueryLexer.Is) + " " 
							+ Criteria.quote(getUser().getName());
					PageParameters params = ProjectListPage.paramsOf(query, 0, 0); 
					String url = RequestCycle.get().urlFor(ProjectListPage.class, params).toString();
					String html = String.format("There are <a href='%s'>projects owned by this user</a>. "
							+ "Please change owner of these projects to be under other users, or delete "
							+ "them if no longer needed", url); 
					return new Label(componentId, html).setEscapeModelStrings(false);
				}
				
			};
		} else {
			new ConfirmModal(target) {

				@Override
				protected void onConfirm(AjaxRequestTarget target) {
					OneDev.getInstance(UserManager.class).delete(getUser());
					onDeleted(target);
				}

				@Override
				protected String getConfirmInput() {
					return null;
				}

				@Override
				protected String getConfirmMessage() {
					return "Do you really want to delete user '" + getUser().getDisplayName() + "'?";
				}
				
			};
		}
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.isAdministrator() 
				&& !getUser().isRoot() 
				&& !getUser().equals(SecurityUtils.getUser()));
	}
	
	protected abstract User getUser();
	
	protected abstract void onDeleted(AjaxRequestTarget target);
	
}
