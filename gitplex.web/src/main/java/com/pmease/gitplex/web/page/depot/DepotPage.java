package com.pmease.gitplex.web.page.depot;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.WebSession;
import com.pmease.gitplex.web.model.DepotModel;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.depot.branches.DepotBranchesPage;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.commit.DepotCommitsPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.overview.DepotOverviewPage;
import com.pmease.gitplex.web.page.depot.pullrequest.PullRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.page.depot.setting.DepotSettingPage;
import com.pmease.gitplex.web.page.depot.setting.general.GeneralSettingPage;
import com.pmease.gitplex.web.page.depot.tags.DepotTagsPage;

@SuppressWarnings("serial")
public abstract class DepotPage extends AccountPage {

	private static final String PARAM_DEPOT = "depot";

	protected final IModel<Depot> depotModel;
	
	public static PageParameters paramsOf(Depot depot) {
		return paramsOf(depot.getAccount(), depot.getName());
	}
	
	public static PageParameters paramsOf(Account account, String depotName) {
		PageParameters params = paramsOf(account);
		params.set(PARAM_DEPOT, depotName);
		return params;
	}
	
	public DepotPage(PageParameters params) {
		super(params);
		
		String depotName = params.get(PARAM_DEPOT).toString();
		Preconditions.checkNotNull(depotName);
		
		if (depotName.endsWith(Constants.DOT_GIT_EXT))
			depotName = depotName.substring(0, depotName.length() - Constants.DOT_GIT_EXT.length());
		
		Depot depot = GitPlex.getInstance(DepotManager.class).findBy(getAccount(), depotName);
		
		if (depot == null) 
			throw new EntityNotFoundException("Unable to find repository " + getAccount().getName() + "/" + depotName);
		
		depotModel = new DepotModel(depot);
		
		if (!(this instanceof NoCommitsPage) 
				&& !(this instanceof DepotSettingPage) 
				&& !getDepot().git().hasRefs()) { 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getDepot()));
		}
	}
	
	@Override
	protected void onBeforeRender() {
		WebSession.get().getDepotVisits().visit(getDepot());
		
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new DepotTab(Model.of("Overview"), "fa fa-fw fa-list-alt", 0, DepotOverviewPage.class));
		tabs.add(new DepotTab(Model.of("Files"), "fa fa-fw fa-file-text-o", 0, DepotFilePage.class));
		tabs.add(new DepotTab(Model.of("Commits"), "fa fa-fw fa-ext fa-commit", 
				GitPlex.getInstance(AuxiliaryManager.class).getCommitCount(getDepot()), 
				DepotCommitsPage.class, CommitDetailPage.class));
		tabs.add(new DepotTab(Model.of("Branches"), "fa fa-fw fa-code-fork", 
				getDepot().getRefs(Constants.R_HEADS).size(), DepotBranchesPage.class));
		tabs.add(new DepotTab(Model.of("Tags"), "fa fa-fw fa-tag", 
				getDepot().getRefs(Constants.R_TAGS).size(), DepotTagsPage.class));
		
		int openRequests = GitPlex.getInstance(PullRequestManager.class).countOpen(getDepot());
		tabs.add(new DepotTab(Model.of("Pull Requests"), "fa fa-fw fa-ext fa-branch-compare", 
				openRequests, RequestListPage.class, PullRequestPage.class));
		tabs.add(new DepotTab(Model.of("Compare"), "fa fa-fw fa-ext fa-file-diff", 0, RevisionComparePage.class));
		
		if (SecurityUtils.canManage(getDepot()))
			tabs.add(new DepotTab(Model.of("Setting"), "fa fa-fw fa-cog", 0, GeneralSettingPage.class, DepotSettingPage.class));
		
		WebMarkupContainer sidebar = new WebMarkupContainer("sidebar");
		add(sidebar);
		
		/*
		 * Add mini class here instead of repository.js as we want the sidebar to 
		 * be minimized initially even if the page takes some time to load 
		 */
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie("repository.miniSidebar");
		if (cookie != null && "yes".equals(cookie.getValue()))
			sidebar.add(AttributeAppender.append("class", " mini"));
		
		sidebar.add(new Tabbable("repoTabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(DepotPage.class, "depot.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(DepotPage.class, "depot.css")));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canRead(getDepot());
	}
	
	@Override
	public Depot getDepot() {
		return depotModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}

	@Override
	protected Component newContextHead(String componentId) {
		Fragment fragment = new Fragment(componentId, "contextHeadFrag", this);
		Link<Void> accountLink = new BookmarkablePageLink<>("accountLink", 
				AccountOverviewPage.class, AccountOverviewPage.paramsOf(getAccount()));
		accountLink.add(new Label("accountName", getAccount().getName()));
		fragment.add(accountLink);
		
		Link<Void> repoLink = new BookmarkablePageLink<>("repoLink", 
				DepotOverviewPage.class, DepotOverviewPage.paramsOf(getDepot()));
		repoLink.add(new Label("repoName", getDepot().getName()));
		fragment.add(repoLink);
		return fragment;
	}

}
