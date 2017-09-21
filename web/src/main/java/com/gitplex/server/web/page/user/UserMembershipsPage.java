package com.gitplex.server.web.page.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
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

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.manager.MembershipManager;
import com.gitplex.server.model.Membership;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.facade.GroupFacade;
import com.gitplex.server.util.facade.MembershipFacade;
import com.gitplex.server.util.matchscore.MatchScoreProvider;
import com.gitplex.server.util.matchscore.MatchScoreUtils;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.datatable.HistoryAwareDataTable;
import com.gitplex.server.web.component.datatable.SelectionColumn;
import com.gitplex.server.web.component.groupchoice.AbstractGroupChoiceProvider;
import com.gitplex.server.web.component.groupchoice.GroupChoiceResourceReference;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;
import com.gitplex.server.web.component.select2.SelectToAddChoice;
import com.gitplex.server.web.page.group.GroupProfilePage;
import com.gitplex.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class UserMembershipsPage extends UserPage {

	private static final String PARAM_PAGE = "page";
	
	private String searchInput;
	
	private DataTable<Membership, Void> membershipsTable;
	
	private SelectionColumn<Membership, Void> selectionColumn;
	
	public UserMembershipsPage(PageParameters params) {
		super(params);
	}

	private EntityCriteria<Membership> getCriteria() {
		EntityCriteria<Membership> criteria = EntityCriteria.of(Membership.class);
		if (searchInput != null)
			criteria.createCriteria("group").add(Restrictions.ilike("name", searchInput, MatchMode.ANYWHERE)); 
		criteria.add(Restrictions.eq("user", getUser()));
		return criteria;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("filterGroups", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(membershipsTable);
			}
			
		});
		
		add(new SelectToAddChoice<GroupFacade>("addNew", new AbstractGroupChoiceProvider() {

			@Override
			public void query(String term, int page, Response<GroupFacade> response) {
				List<GroupFacade> notMembersOf = new ArrayList<>();
				CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
				Set<Long> groupIds = new HashSet<>();
				for (MembershipFacade membership: cacheManager.getMemberships().values()) {
					if (membership.getUserId().equals(getUser().getId()))
						groupIds.add(membership.getGroupId());
				}
				for (GroupFacade group: cacheManager.getGroups().values()) {
					if (!groupIds.contains(group.getId()))
						notMembersOf.add(group);
				}
				Collections.sort(notMembersOf);
				Collections.reverse(notMembersOf);
				
				notMembersOf = MatchScoreUtils.filterAndSort(notMembersOf, new MatchScoreProvider<GroupFacade>() {

					@Override
					public double getMatchScore(GroupFacade object) {
						return MatchScoreUtils.getMatchScore(object.getName(), term);
					}
					
				});
				
				new ResponseFiller<>(response).fill(notMembersOf, page, WebConstants.PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add group...");
				getSettings().setFormatResult("gitplex.server.groupChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.server.groupChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.server.groupChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, GroupFacade selection) {
				Membership membership = new Membership();
				membership.setUser(getUser());
				membership.setGroup(GitPlex.getInstance(GroupManager.class).load(selection.getId()));
				GitPlex.getInstance(MembershipManager.class).save(membership);
				target.add(membershipsTable);
				Session.get().success("Group added");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(new GroupChoiceResourceReference()));
			}
			
		});			
		
		AjaxLink<Void> deleteSelected = new AjaxLink<Void>("deleteSelected") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Collection<Membership> membershipsToDelete = new HashSet<>();
				for (IModel<Membership> model: selectionColumn.getSelections()) {
					membershipsToDelete.add(model.getObject());
				}
				GitPlex.getInstance(MembershipManager.class).delete(membershipsToDelete);
				target.add(membershipsTable);
				selectionColumn.getSelections().clear();
				target.add(this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!selectionColumn.getSelections().isEmpty());
			}
			
		};
		deleteSelected.setOutputMarkupPlaceholderTag(true);
		add(deleteSelected);

		List<IColumn<Membership, Void>> columns = new ArrayList<>();
		
		selectionColumn = new SelectionColumn<Membership, Void>() {
			
			@Override
			protected void onSelectionChange(AjaxRequestTarget target) {
				target.add(deleteSelected);
			}
			
		};
		if (SecurityUtils.isAdministrator())
			columns.add(selectionColumn);
		
		columns.add(new AbstractColumn<Membership, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Membership>> cellItem, String componentId,
					IModel<Membership> rowModel) {
				Membership membership = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "groupFrag", UserMembershipsPage.this);
				Link<Void> link = new BookmarkablePageLink<Void>("group", GroupProfilePage.class, 
						GroupProfilePage.paramsOf(membership.getGroup())) {

					@Override
					public IModel<?> getBody() {
						return Model.of(rowModel.getObject().getGroup().getName());
					}
					
				};
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Membership, Void>(Model.of("Description")) {

			@Override
			public void populateItem(Item<ICellPopulator<Membership>> cellItem, String componentId,
					IModel<Membership> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getGroup().getDescription()));
			}
		});
		
		SortableDataProvider<Membership, Void> dataProvider = new SortableDataProvider<Membership, Void>() {

			@Override
			public Iterator<? extends Membership> iterator(long first, long count) {
				EntityCriteria<Membership> criteria = getCriteria();
				criteria.addOrder(Order.desc("id"));
				return GitPlex.getInstance(MembershipManager.class).findRange(criteria, (int)first, 
						(int)count).iterator();
			}

			@Override
			public long size() {
				return GitPlex.getInstance(MembershipManager.class).count(getCriteria());
			}

			@Override
			public IModel<Membership> model(Membership object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Membership>() {

					@Override
					protected Membership load() {
						return GitPlex.getInstance(MembershipManager.class).load(id);
					}
					
				};
			}
		};
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getUser());
				params.add(PARAM_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		
		add(membershipsTable = new HistoryAwareDataTable<Membership, Void>("memberships", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}
	
}
