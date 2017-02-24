package com.gitplex.server.web.page.depot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.manager.VisitInfoManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.tabbable.PageTab;
import com.gitplex.server.web.component.tabbable.Tabbable;
import com.gitplex.server.web.page.account.AccountPage;
import com.gitplex.server.web.page.account.overview.AccountOverviewPage;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;
import com.gitplex.server.web.page.depot.branches.DepotBranchesPage;
import com.gitplex.server.web.page.depot.comments.DepotCommentsPage;
import com.gitplex.server.web.page.depot.commit.CommitDetailPage;
import com.gitplex.server.web.page.depot.commit.DepotCommitsPage;
import com.gitplex.server.web.page.depot.compare.RevisionComparePage;
import com.gitplex.server.web.page.depot.moreinfo.MoreInfoPanel;
import com.gitplex.server.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.gitplex.server.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.gitplex.server.web.page.depot.setting.DepotSettingPage;
import com.gitplex.server.web.page.depot.setting.general.GeneralSettingPage;
import com.gitplex.server.web.page.depot.tags.DepotTagsPage;
import com.google.common.base.Preconditions;

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
		
		Depot depot = GitPlex.getInstance(DepotManager.class).find(getAccount(), depotName);
		
		if (depot == null) 
			throw new EntityNotFoundException("Unable to find repository " + getAccount().getName() + "/" + depotName);
		
		Long depotId = depot.getId();
		
		depotModel = new LoadableDetachableModel<Depot>() {

			@Override
			protected Depot load() {
				Depot depot = GitPlex.getInstance(DepotManager.class).load(depotId);
				
				/*
				 * Give child page a chance to cache object id of known revisions upon
				 * loading the depot object 
				 */
				for (Map.Entry<String, ObjectId> entry: getObjectIdCache().entrySet()) {
					depot.cacheObjectId(entry.getKey(), entry.getValue());
				}
				return depot;
			}
			
		};
		
		// we do not need to reload the depot this time as we already have that object on hand
		depotModel.setObject(depot);
		
		if (!(this instanceof NoBranchesPage) 
				&& !(this instanceof DepotSettingPage) 
				&& getDepot().getDefaultBranch() == null) { 
			throw new RestartResponseException(NoBranchesPage.class, paramsOf(getDepot()));
		}
	}
	
	protected Map<String, ObjectId> getObjectIdCache() {
		return new HashMap<>();
	}
	
	@Override
	protected void onAfterRender() {
		super.onAfterRender();
		if (getLoginUser() != null)
			GitPlex.getInstance(VisitInfoManager.class).visit(getLoginUser(), getDepot());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new DepotTab(Model.of("Files"), "fa fa-fw fa-file-text-o", 0, DepotBlobPage.class));
		tabs.add(new DepotTab(Model.of("Commits"), "fa fa-fw fa-ext fa-commit", 0,
				DepotCommitsPage.class, CommitDetailPage.class));
		tabs.add(new DepotTab(Model.of("Branches"), "fa fa-fw fa-code-fork", 
				0, DepotBranchesPage.class));
		tabs.add(new DepotTab(Model.of("Tags"), "fa fa-fw fa-tag", 
				0, DepotTagsPage.class));
		
		tabs.add(new DepotTab(Model.of("Pull Requests"), "fa fa-fw fa-ext fa-branch-compare", 
				0, RequestListPage.class, NewRequestPage.class, RequestDetailPage.class));
		
		tabs.add(new DepotTab(Model.of("Code Comments"), "fa fa-fw fa-commenting", 
				0, DepotCommentsPage.class, DepotCommentsPage.class));
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
		
		sidebar.add(new Tabbable("depotTabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new DepotResourceReference()));
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
		Link<Void> accountLink = new ViewStateAwarePageLink<>("accountLink", 
				AccountOverviewPage.class, AccountOverviewPage.paramsOf(getAccount()));
		accountLink.add(new Label("accountName", getAccount().getName()));
		fragment.add(accountLink);
		
		Link<Void> depotLink = new ViewStateAwarePageLink<>("depotLink", 
				DepotBlobPage.class, DepotBlobPage.paramsOf(getDepot()));
		depotLink.add(new Label("depotName", getDepot().getName()));
		fragment.add(depotLink);
		fragment.add(new DropdownLink("depotMoreInfo") {

			@Override
			protected Component newContent(String id) {
				return new MoreInfoPanel(id, depotModel) {

					@Override
					protected void onPromptForkOption(AjaxRequestTarget target) {
						close();
					}
					
				};
			}
			
		});
		return fragment;
	}

}
