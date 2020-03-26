package io.onedev.server.web.page.admin.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
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
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.group.create.NewGroupPage;
import io.onedev.server.web.page.admin.group.profile.GroupProfilePage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class GroupListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private DataTable<Group, Void> groupsTable;
	
	private String query;

	public GroupListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}
	
	private EntityCriteria<Group> getCriteria() {
		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		if (query != null) 
			criteria.add(Restrictions.ilike("name", query, MatchMode.ANYWHERE));
		else
			criteria.setCacheable(true);
		return criteria;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("filterGroups", Model.of(query)));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				query = searchField.getInput();
				if (StringUtils.isBlank(query))
					query = null;
				target.add(groupsTable);
			}

		});
		
		add(new Link<Void>("addNew") {

			@Override
			public void onClick() {
				setResponsePage(NewGroupPage.class);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		add(new FencedFeedbackPanel("feedback", this).setEscapeModelStrings(false));
		
		List<IColumn<Group, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId, IModel<Group> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", GroupListPage.this);
				fragment.add(new Link<Void>("name") {

					@Override
					public IModel<?> getBody() {
						return Model.of(rowModel.getObject().getName());
					}

					@Override
					public void onClick() {
						setResponsePage(GroupProfilePage.class, GroupProfilePage.paramsOf(rowModel.getObject()));
					}
					
				});
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("Is Administrator")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
					IModel<Group> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().isAdministrator()));
			}
			
		});
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("Can Create Projects")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
					IModel<Group> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().isCreateProjects()));
			}
			
		});
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId, IModel<Group> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", GroupListPage.this);
				
				Group group = rowModel.getObject();
				
				fragment.add(new Link<Void>("delete") {

					@Override
					public void onClick() {
						try {
							OneDev.getInstance(GroupManager.class).delete(rowModel.getObject());
							setResponsePage(GroupListPage.class);
						} catch (OneException e) {
							getPage().error(e.getMessage());
						}
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.isAdministrator());
					}
					
				}.add(new ConfirmOnClick("Do you really want to delete group '" + group.getName() + "'?")));
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});
		
		SortableDataProvider<Group, Void> dataProvider = new SortableDataProvider<Group, Void>() {

			@Override
			public Iterator<? extends Group> iterator(long first, long count) {
				EntityCriteria<Group> criteria = getCriteria();
				criteria.addOrder(Order.asc("name"));
				return OneDev.getInstance(GroupManager.class).query(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return OneDev.getInstance(GroupManager.class).count(getCriteria());
			}

			@Override
			public IModel<Group> model(Group object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Group>() {

					@Override
					protected Group load() {
						return OneDev.getInstance(GroupManager.class).load(id);
					}
					
				};
			}
		};

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = new PageParameters();
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
		
		add(groupsTable = new DefaultDataTable<>("groups", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GroupCssResourceReference()));
	}
	
}
