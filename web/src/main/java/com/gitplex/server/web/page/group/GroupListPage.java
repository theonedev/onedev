package com.gitplex.server.web.page.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.ComponentRenderer;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.datatable.DefaultDataTable;
import com.gitplex.server.web.component.link.LabelLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.layout.LayoutPage;
import com.gitplex.server.web.util.ConfirmOnClick;

@SuppressWarnings("serial")
public class GroupListPage extends LayoutPage {

	private DataTable<Group, Void> groupsTable;
	
	private String searchInput;
	
	private EntityCriteria<Group> getCriteria() {
		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		if (searchInput != null) 
			criteria.add(Restrictions.ilike("name", searchInput, MatchMode.ANYWHERE));
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
		
		List<IColumn<Group, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId, IModel<Group> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", GroupListPage.this);
				fragment.add(new LabelLink<Void>("name", Model.of(rowModel.getObject().getName())) {

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
				cellItem.add(new Label(componentId, rowModel.getObject().isCanCreateProjects()));
			}
			
		});
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
					IModel<Group> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", GroupListPage.this);
				fragment.add(AttributeAppender.append("class", "actions"));
				
				Group group = rowModel.getObject();
				
				fragment.add(new Link<Void>("profile") {

					@Override
					public void onClick() {
						setResponsePage(GroupProfilePage.class, GroupProfilePage.paramsOf(rowModel.getObject()));
					}

				});
				fragment.add(new Link<Void>("delete") {

					@Override
					public void onClick() {
						GitPlex.getInstance(GroupManager.class).delete(rowModel.getObject());
						setResponsePage(GroupListPage.class);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.isAdministrator());
					}
					
				}.add(new ConfirmOnClick("Do you really want to delete group '" + group.getName() + "'?")));
				
				cellItem.add(fragment);
			}
			
		});
		
		SortableDataProvider<Group, Void> dataProvider = new SortableDataProvider<Group, Void>() {

			@Override
			public Iterator<? extends Group> iterator(long first, long count) {
				EntityCriteria<Group> criteria = getCriteria();
				criteria.addOrder(Order.asc("name"));
				return GitPlex.getInstance(GroupManager.class).findRange(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return GitPlex.getInstance(GroupManager.class).count(getCriteria());
			}

			@Override
			public IModel<Group> model(Group object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Group>() {

					@Override
					protected Group load() {
						return GitPlex.getInstance(GroupManager.class).load(id);
					}
					
				};
			}
		};
		
		add(groupsTable = new DefaultDataTable<Group, Void>("groups", columns, dataProvider, 
				WebConstants.PAGE_SIZE));
	}

	@Override
	protected List<ComponentRenderer> getBreadcrumbs() {
		List<ComponentRenderer> breadcrumbs = super.getBreadcrumbs();
		
		breadcrumbs.add(new ComponentRenderer() {

			@Override
			public Component render(String componentId) {
				return new ViewStateAwarePageLink<Void>(componentId, GroupListPage.class) {

					@Override
					public IModel<?> getBody() {
						return Model.of("Groups");
					}
					
				};
			}
			
		});

		return breadcrumbs;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GroupResourceReference()));
	}
	
}
