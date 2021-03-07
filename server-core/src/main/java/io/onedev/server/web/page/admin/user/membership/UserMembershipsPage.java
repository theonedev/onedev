package io.onedev.server.web.page.admin.user.membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Membership;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.groupchoice.AbstractGroupChoiceProvider;
import io.onedev.server.web.component.groupchoice.GroupChoiceResourceReference;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.page.admin.group.profile.GroupProfilePage;
import io.onedev.server.web.page.admin.user.UserPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class UserMembershipsPage extends UserPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private DataTable<Membership, Void> membershipsTable;
	
	private SelectionColumn<Membership, Void> selectionColumn;
	
	public UserMembershipsPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	private EntityCriteria<Membership> getCriteria() {
		EntityCriteria<Membership> criteria = EntityCriteria.of(Membership.class);
		if (query != null)
			criteria.createCriteria("group").add(Restrictions.ilike("name", query, MatchMode.ANYWHERE)); 
		else
			criteria.setCacheable(true);
		criteria.add(Restrictions.eq("user", getUser()));
		return criteria;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("externalManagedNote", "Group membership of this user is managed from " + getUser().getAuthSource())
				.setVisible(getUser().isMembershipExternalManaged()));
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("filterGroups", Model.of(query)));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				query = searchField.getInput();
				if (StringUtils.isBlank(query))
					query = null;
				target.add(membershipsTable);
			}
			
		});
		
		add(new SelectToAddChoice<Group>("addNew", new AbstractGroupChoiceProvider() {

			@Override
			public void query(String term, int page, Response<Group> response) {
				List<Group> notMembersOf = OneDev.getInstance(GroupManager.class).query();
				notMembersOf.removeAll(getUser().getGroups());
				Collections.sort(notMembersOf);
				Collections.reverse(notMembersOf);
				
				notMembersOf = MatchScoreUtils.filterAndSort(notMembersOf, new MatchScoreProvider<Group>() {

					@Override
					public double getMatchScore(Group object) {
						return MatchScoreUtils.getMatchScore(object.getName(), term);
					}
					
				});
				
				new ResponseFiller<>(response).fill(notMembersOf, page, WebConstants.PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add to group...");
				getSettings().setFormatResult("onedev.server.groupChoiceFormatter.formatResult");
				getSettings().setFormatSelection("onedev.server.groupChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("onedev.server.groupChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Group selection) {
				Membership membership = new Membership();
				membership.setUser(getUser());
				membership.setGroup(OneDev.getInstance(GroupManager.class).load(selection.getId()));
				OneDev.getInstance(MembershipManager.class).save(membership);
				target.add(membershipsTable);
				Session.get().success("Group added");
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(new GroupChoiceResourceReference()));
			}
			
		}.setVisible(!getUser().isMembershipExternalManaged()));			
		
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
				setVisible(selectionColumn != null && !selectionColumn.getSelections().isEmpty());
			}
			
		};
		deleteSelected.setOutputMarkupPlaceholderTag(true);
		add(deleteSelected);

		List<IColumn<Membership, Void>> columns = new ArrayList<>();
		
		if (!getUser().isMembershipExternalManaged()) {
			selectionColumn = new SelectionColumn<Membership, Void>() {
				
				@Override
				protected void onSelectionChange(AjaxRequestTarget target) {
					target.add(deleteSelected);
				}
				
			};
			columns.add(selectionColumn);
		}
		
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
				return OneDev.getInstance(MembershipManager.class).query(criteria, (int)first, (int)count).iterator();
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
				PageParameters params = paramsOf(getUser());
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
