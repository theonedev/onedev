package com.gitplex.server.web.page.account.teams;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.TeamManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Team;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.BeanEditor;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.page.account.AccountLayoutPage;
import com.gitplex.server.web.page.account.overview.AccountOverviewPage;

@SuppressWarnings("serial")
public class NewTeamPage extends AccountLayoutPage {

	public NewTeamPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Team team = new Team();
		
		BeanEditor<Serializable> editor = BeanContext.editBean("editor", team);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				TeamManager teamManager = GitPlex.getInstance(TeamManager.class);
				Account organization = getAccount();
				team.setOrganization(organization);
				if (teamManager.find(organization, team.getName()) != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another team in the organization");
				} else {
					GitPlex.getInstance(TeamManager.class).save(team, null);
					setResponsePage(TeamMemberListPage.class, TeamMemberListPage.paramsOf(team));
				}
			}
			
		};
		form.add(editor);
		
		add(form);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(TeamListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}

}
