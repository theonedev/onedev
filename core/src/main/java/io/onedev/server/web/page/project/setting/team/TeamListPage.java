package io.onedev.server.web.page.project.setting.team;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.model.Team;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class TeamListPage extends ProjectSettingPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private DataTable<Team, Void> teamsTable;
	
	private String searchInput;
	
	public TeamListPage(PageParameters params) {
		super(params);
	}
	
	private EntityCriteria<Team> getCriteria() {
		EntityCriteria<Team> criteria = EntityCriteria.of(Team.class);
		criteria.add(Restrictions.eq("project", getProject()));
		if (searchInput != null) 
			criteria.add(Restrictions.ilike("name", searchInput, MatchMode.ANYWHERE));
		return criteria;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("filterTeams", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(teamsTable);
			}

		});
		
		add(new Link<Void>("addNew") {

			@Override
			public void onClick() {
				setResponsePage(NewTeamPage.class, NewTeamPage.paramsOf(getProject()));
			}
			
		});
		
		List<IColumn<Team, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Team, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem, String componentId, IModel<Team> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", TeamListPage.this);
				Team team = rowModel.getObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", TeamEditPage.class, 
						TeamEditPage.paramsOf(team));
				link.add(new Label("label", team.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Team, Void>(Model.of("Privilege")) {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem, String componentId,
					IModel<Team> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getPrivilege().toString()));
			}
			
		});
		
		columns.add(new AbstractColumn<Team, Void>(Model.of("Members")) {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem, String componentId,
					IModel<Team> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getMemberships().size()));
			}
			
		});
		
		columns.add(new AbstractColumn<Team, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<Team>> cellItem, String componentId,
					IModel<Team> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", TeamListPage.this);
				fragment.add(AttributeAppender.append("class", "actions"));
				
				Team team = rowModel.getObject();
				fragment.add(new Link<Void>("delete") {

					@Override
					public void onClick() {
						OneDev.getInstance(TeamManager.class).delete(rowModel.getObject());
						setResponsePage(TeamListPage.class, TeamListPage.paramsOf(getProject()));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.isAdministrator());
					}
					
				}.add(new ConfirmOnClick("Do you really want to delete team '" + team.getName() + "'?")));
				
				cellItem.add(fragment);
			}
			
		});
		
		SortableDataProvider<Team, Void> dataProvider = new SortableDataProvider<Team, Void>() {

			@Override
			public Iterator<? extends Team> iterator(long first, long count) {
				EntityCriteria<Team> criteria = getCriteria();
				criteria.addOrder(Order.asc("name"));
				return OneDev.getInstance(TeamManager.class).query(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return OneDev.getInstance(TeamManager.class).count(getCriteria());
			}

			@Override
			public IModel<Team> model(Team object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Team>() {

					@Override
					protected Team load() {
						return OneDev.getInstance(TeamManager.class).load(id);
					}
					
				};
			}
		};

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = new PageParameters();
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(teamsTable = new HistoryAwareDataTable<>("teams", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TeamCssResourceReference()));
	}
	
}
