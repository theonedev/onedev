package io.onedev.server.web.page.project.setting.team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.MembershipManager;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.util.ConfirmOnClick;

@SuppressWarnings("serial")
public class TeamEditPage extends ProjectSettingPage {

	private static final String PARAM_TEAM = "team";

	private final Long teamId;
	
	private final String oldName;
	
	public TeamEditPage(PageParameters params) {
		super(params);
		
		teamId = params.get(PARAM_TEAM).toOptionalLong();
		oldName = getTeam().getName();
	}

	private Team getTeam() {
		return getTeamManager().load(teamId);
	}
	
	private TeamManager getTeamManager() {
		return OneDev.getInstance(TeamManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Team team = getTeam();
		MembersEditBean membersEditBean = new MembersEditBean();
		
		List<User> members = new ArrayList<>(team.getMembers());
		Collections.sort(members);
		for (User member: members)
			membersEditBean.getMembers().add(member.getName());
		
		BeanEditor generalEditor = BeanContext.editBean("generalEditor", team);		
		BeanEditor membersEditor = BeanContext.editBean("membersEditor", membersEditBean);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Team teamWithSameName = getTeamManager().find(getProject(), team.getName());
				if (teamWithSameName != null && !teamWithSameName.equals(team)) {
					generalEditor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another team in the project");
				} 
				if (!generalEditor.hasErrors(true)) {
					Team reloaded = getTeam();
					generalEditor.getBeanDescriptor().copyProperties(team, reloaded);
					getTeamManager().save(reloaded, oldName);

					Collection<User> members = new ArrayList<>();
					for (String member: membersEditBean.getMembers()) {
						members.add(OneDev.getInstance(UserManager.class).findByName(member));
					}
						
					OneDev.getInstance(MembershipManager.class).assignMembers(reloaded, members);
					setResponsePage(TeamListPage.class, TeamListPage.paramsOf(getProject()));
					Session.get().success("Team saved");
				}
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		
		form.add(generalEditor);
		form.add(membersEditor);
		
		form.add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				OneDev.getInstance(TeamManager.class).delete(getTeam());
				setResponsePage(TeamListPage.class, TeamListPage.paramsOf(getProject()));
			}
			
		}.add(new ConfirmOnClick("Do you really want to delete this team?")));
		
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TeamCssResourceReference()));
	}
	
	public static PageParameters paramsOf(Team team) {
		PageParameters params = paramsOf(team.getProject());
		params.add(PARAM_TEAM, team.getId());
		return params;
	}
	
}
