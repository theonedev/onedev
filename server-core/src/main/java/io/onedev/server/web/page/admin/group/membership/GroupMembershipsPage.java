package io.onedev.server.web.page.admin.group.membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Membership;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.component.user.choice.AbstractUserChoiceProvider;
import io.onedev.server.web.component.user.choice.UserChoiceResourceReference;
import io.onedev.server.web.page.admin.group.GroupPage;
import io.onedev.server.web.page.admin.user.profile.UserProfilePage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class GroupMembershipsPage extends GroupPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private DataTable<Membership, Void> membershipsTable;
	
	private SelectionColumn<Membership, Void> selectionColumn;
	
	public GroupMembershipsPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	private EntityCriteria<Membership> getCriteria() {
		EntityCriteria<Membership> criteria = EntityCriteria.of(Membership.class);
		if (query != null)
			criteria.createCriteria("user").add(Restrictions.ilike("name", query, MatchMode.ANYWHERE)); 
		else
			criteria.setCacheable(true);
		criteria.add(Restrictions.eq("group", getGroup()));
		return criteria;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("filterUsers", Model.of(query)));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				query = searchField.getInput();
				if (StringUtils.isBlank(query))
					query = null;
				target.add(membershipsTable);
			}
			
		});
		
		add(new SelectToAddChoice<User>("addNew", new AbstractUserChoiceProvider() {

			@Override
			public void query(String term, int page, Response<User> response) {
				List<User> nonMembers = OneDev.getInstance(UserManager.class).query();
				nonMembers.removeAll(getGroup().getMembers());
				Collections.sort(nonMembers);
				Collections.reverse(nonMembers);
				
				nonMembers = MatchScoreUtils.filterAndSort(nonMembers, new MatchScoreProvider<User>() {

					@Override
					public double getMatchScore(User object) {
						return object.getMatchScore(term);
					}
					
				});
				
				new ResponseFiller<>(response).fill(nonMembers, page, WebConstants.PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add member...");
				getSettings().setFormatResult("onedev.server.userChoiceFormatter.formatResult");
				getSettings().setFormatSelection("onedev.server.userChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("onedev.server.userChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, User selection) {
				Membership membership = new Membership();
				membership.setGroup(getGroup());
				membership.setUser(OneDev.getInstance(UserManager.class).load(selection.getId()));
				OneDev.getInstance(MembershipManager.class).save(membership);
				target.add(membershipsTable);
				Session.get().success("Member added");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
			}
			
		});			
		
		AjaxLink<Void> deleteSelected = new AjaxLink<Void>("deleteSelected") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Collection<Membership> membershipsToDelete = new HashSet<>();
				for (IModel<Membership> model: selectionColumn.getSelections()) {
					membershipsToDelete.add(model.getObject());
				}
				OneDev.getInstance(MembershipManager.class).delete(membershipsToDelete);
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
				User user = rowModel.getObject().getUser();
				Fragment fragment = new Fragment(componentId, "nameFrag", GroupMembershipsPage.this);
				Link<Void> link = new BookmarkablePageLink<Void>("link", UserProfilePage.class, 
						UserProfilePage.paramsOf(user));
				link.add(new UserAvatar("avatar", user));
				link.add(new Label("name", user.getDisplayName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Membership, Void>(Model.of("Email")) {

			@Override
			public void populateItem(Item<ICellPopulator<Membership>> cellItem, String componentId,
					IModel<Membership> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getUser().getEmail()));
			}
		});
		
		SortableDataProvider<Membership, Void> dataProvider = new SortableDataProvider<Membership, Void>() {

			@Override
			public Iterator<? extends Membership> iterator(long first, long count) {
				EntityCriteria<Membership> criteria = getCriteria();
				criteria.addOrder(Order.desc("id"));
				return OneDev.getInstance(MembershipManager.class).query(criteria, (int)first, 
						(int)count).iterator();
			}

			@Override
			public long size() {
				return OneDev.getInstance(MembershipManager.class).count(getCriteria());
			}

			@Override
			public IModel<Membership> model(Membership object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Membership>() {

					@Override
					protected Membership load() {
						return OneDev.getInstance(MembershipManager.class).load(id);
					}
					
				};
			}
		};
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getGroup());
				params.add(PARAM_PAGE, currentPage+1);
				if (query != null)
					params.add(PARAM_QUERY, query);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		
		add(membershipsTable = new OneDataTable<Membership, Void>("memberships", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

}
