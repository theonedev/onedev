package com.pmease.gitplex.web.page.organization.team;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.StaleObjectStateException;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.component.Team;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.AccountOverviewPage;
import com.pmease.gitplex.web.page.organization.OrganizationResourceReference;

@SuppressWarnings("serial")
public class TeamEditPage extends AccountLayoutPage {

	private static final String PARAM_TEAM = "team";
	
	private final Team team;
	
	private final long version;
	
	public static PageParameters paramsOf(Account organization, Team team) {
		PageParameters params = paramsOf(organization);
		params.add(PARAM_TEAM, team.getName());
		return params;
	}
	
	public TeamEditPage(PageParameters params) {
		super(params);

		version = getAccount().getVersion();
		String teamName = params.get(PARAM_TEAM).toString();
		team = Preconditions.checkNotNull(getAccount().getTeams().get(teamName));
		
		Preconditions.checkState(getAccount().isOrganization());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String oldName = team.getName();
		BeanEditor<Serializable> editor = BeanContext.editBean("editor", team);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				Account organization = getAccount();
				if (version != organization.getVersion()) {
					throw new StaleObjectStateException(Account.class.getName(), organization.getId());
				}
				if (!oldName.equals(team.getName()) && organization.getTeams().containsKey(team.getName())) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another team in the organization");
				} else {
					GitPlex.getInstance(TeamManager.class).save(organization, team, oldName);
					setResponsePage(TeamMemberListPage.class, TeamMemberListPage.paramsOf(getAccount(), team));
				}
			}
			
		};
		form.add(editor);
		
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(TeamListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}

}
