package com.turbodev.server.web.page.group;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.turbodev.utils.matchscore.MatchScoreProvider;
import com.turbodev.utils.matchscore.MatchScoreUtils;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.CacheManager;
import com.turbodev.server.manager.MembershipManager;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.Membership;
import com.turbodev.server.persistence.dao.EntityCriteria;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.util.facade.MembershipFacade;
import com.turbodev.server.util.facade.UserFacade;
import com.turbodev.server.web.WebConstants;
import com.turbodev.server.web.behavior.OnTypingDoneBehavior;
import com.turbodev.server.web.component.avatar.AvatarLink;
import com.turbodev.server.web.component.datatable.HistoryAwareDataTable;
import com.turbodev.server.web.component.datatable.SelectionColumn;
import com.turbodev.server.web.component.link.UserLink;
import com.turbodev.server.web.component.select2.Response;
import com.turbodev.server.web.component.select2.ResponseFiller;
import com.turbodev.server.web.component.select2.SelectToAddChoice;
import com.turbodev.server.web.component.userchoice.AbstractUserChoiceProvider;
import com.turbodev.server.web.component.userchoice.UserChoiceResourceReference;
import com.turbodev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class GroupMembershipsPage extends GroupPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private String searchInput;
	
	private DataTable<Membership, Void> membershipsTable;
	
	private SelectionColumn<Membership, Void> selectionColumn;
	
	public GroupMembershipsPage(PageParameters params) {
		super(params);
	}

	private EntityCriteria<Membership> getCriteria() {
		EntityCriteria<Membership> criteria = EntityCriteria.of(Membership.class);
		if (searchInput != null)
			criteria.createCriteria("user").add(Restrictions.ilike("name", searchInput, MatchMode.ANYWHERE)); 
		criteria.add(Restrictions.eq("group", getGroup()));
		return criteria;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("filterUsers", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(membershipsTable);
			}
			
		});
		
		add(new SelectToAddChoice<UserFacade>("addNew", new AbstractUserChoiceProvider() {

			@Override
			public void query(String term, int page, Response<UserFacade> response) {
				List<UserFacade> nonMembers = new ArrayList<>();
				CacheManager cacheManager = TurboDev.getInstance(CacheManager.class);
				Set<Long> memberIds = new HashSet<>();
				for (MembershipFacade membership: cacheManager.getMemberships().values()) {
					if (membership.getGroupId().equals(getGroup().getId()))
						memberIds.add(membership.getUserId());
				}
				for (UserFacade user: cacheManager.getUsers().values()) {
					if (!memberIds.contains(user.getId()))
						nonMembers.add(user);
				}
				Collections.sort(nonMembers);
				Collections.reverse(nonMembers);
				
				nonMembers = MatchScoreUtils.filterAndSort(nonMembers, new MatchScoreProvider<UserFacade>() {

					@Override
					public double getMatchScore(UserFacade object) {
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
				getSettings().setFormatResult("turbodev.server.userChoiceFormatter.formatResult");
				getSettings().setFormatSelection("turbodev.server.userChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("turbodev.server.userChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, UserFacade selection) {
				Membership membership = new Membership();
				membership.setGroup(getGroup());
				membership.setUser(TurboDev.getInstance(UserManager.class).load(selection.getId()));
				TurboDev.getInstance(MembershipManager.class).save(membership);
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
				TurboDev.getInstance(MembershipManager.class).delete(membershipsToDelete);
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
				Fragment fragment = new Fragment(componentId, "nameFrag", GroupMembershipsPage.this);
				fragment.add(new AvatarLink("avatarLink", membership.getUser()));
				fragment.add(new UserLink("nameLink", membership.getUser()));
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
				return TurboDev.getInstance(MembershipManager.class).findRange(criteria, (int)first, 
						(int)count).iterator();
			}

			@Override
			public long size() {
				return TurboDev.getInstance(MembershipManager.class).count(getCriteria());
			}

			@Override
			public IModel<Membership> model(Membership object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Membership>() {

					@Override
					protected Membership load() {
						return TurboDev.getInstance(MembershipManager.class).load(id);
					}
					
				};
			}
		};
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getGroup());
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(membershipsTable = new HistoryAwareDataTable<Membership, Void>("memberships", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}
	
}
