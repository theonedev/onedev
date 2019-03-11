package io.onedev.server.web.component.project.privilege.source;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.permission.DefaultPrivilege;
import io.onedev.server.security.permission.ProjectPrivilege;

@SuppressWarnings("serial")
public class PrivilegeSourcePanel extends Panel {

	private final IModel<User> userModel;
	
	private final IModel<Project> projectModel;
	
	private final ProjectPrivilege privilege;
	
	public PrivilegeSourcePanel(String id, User user, Project project, ProjectPrivilege privilege) {
		super(id);
	
		Long userId = user.getId();
		userModel = new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return OneDev.getInstance(UserManager.class).load(userId);
			}
			
		};
		
		Long projectId = project.getId();
		projectModel = new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				return OneDev.getInstance(ProjectManager.class).load(projectId);
			}
			
		};
		
		this.privilege = privilege;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		User user = userModel.getObject();
		Project project = projectModel.getObject();
		add(new Label("user", user.getDisplayName()));
		add(new Label("project", project.getName()));
		add(new Label("privilege", WordUtils.toWords(privilege.name())));
		
		add(new WebMarkupContainer("userIsRoot").setVisible(user.isRoot()));
		DefaultPrivilege defaultPrivilege = project.getDefaultPrivilege();
		if (defaultPrivilege != null && defaultPrivilege.getProjectPrivilege().implies(privilege)) {
			add(new Label("projectHasDefaultPrivilege", 
					"Project has default permission \"" + WordUtils.toWords(defaultPrivilege.getProjectPrivilege().name()) + "\""));
		} else {
			add(new WebMarkupContainer("projectHasDefaultPrivilege").setVisible(false));
		}
		add(new ListView<Group>("groups", new LoadableDetachableModel<List<Group>>() {

			@Override
			protected List<Group> load() {
				List<Group> groups = new ArrayList<>();
				for (Membership membership: user.getMemberships()) {
					if (membership.getGroup().isAdministrator()) {
						groups.add(membership.getGroup());
					} else {
						for (GroupAuthorization authorization: membership.getGroup().getAuthorizations()) {
							if (authorization.getProject().equals(project) 
									&& authorization.getPrivilege() == privilege) {
								groups.add(membership.getGroup());
								break;
							}
						}
					}
				}
				return groups;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Group> item) {
				item.add(new Label("group", item.getModelObject().getName()));
			}
			
		});
	}

	@Override
	protected void onDetach() {
		userModel.detach();
		projectModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new PrivilegeSourceCssResourceReference()));
	}

}
