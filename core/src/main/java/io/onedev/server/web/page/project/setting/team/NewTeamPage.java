package io.onedev.server.web.page.project.setting.team;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.MembershipManager;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class NewTeamPage extends ProjectSettingPage {

	public NewTeamPage(PageParameters params) {
		super(params);
	}

	private TeamManager getTeamManager() {
		return OneDev.getInstance(TeamManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Team team = new Team();
		MembersEditBean membersEditBean = new MembersEditBean();
		
		BeanEditor generalEditor = BeanContext.editBean("generalEditor", team);		
		BeanEditor membersEditor = BeanContext.editBean("membersEditor", membersEditBean);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Team teamWithSameName = getTeamManager().find(getProject(), team.getName());
				if (teamWithSameName != null) {
					generalEditor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another team in the project");
				} 
				if (!generalEditor.hasErrors(true)) {
					team.setProject(getProject());
					getTeamManager().save(team, null);

					MembershipManager membershipManager = OneDev.getInstance(MembershipManager.class);
					for (String member: membersEditBean.getMembers()) {
						User user = OneDev.getInstance(UserManager.class).findByName(member);
						Preconditions.checkNotNull(user);
						Membership membership = new Membership();
						membership.setTeam(team);
						membership.setUser(user);
						membershipManager.save(membership);
					}
					Session.get().success("Configuration created");
					setResponsePage(TeamListPage.class, TeamListPage.paramsOf(getProject()));
				}
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		
		form.add(generalEditor);
		form.add(membersEditor);
		
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TeamCssResourceReference()));
	}
	
}
