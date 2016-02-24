package com.pmease.gitplex.web.page.account.notifications;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.component.datatable.DefaultDataTable;
import com.pmease.commons.wicket.component.datatable.EntityDataProvider;
import com.pmease.commons.wicket.component.datatable.SelectionColumn;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Notification;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.BranchLink;
import com.pmease.gitplex.web.component.pullrequest.requestlink.RequestLink;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
public class AccountNotificationsPage extends AccountLayoutPage {

	public AccountNotificationsPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected String getPageTitle() {
		return "Notifications - " + getAccount();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		EntityDataProvider<Notification> dataProvider = new EntityDataProvider<Notification>(
				Notification.class, new SortParam<String>("date", false)) {

				@Override
				protected void restrict(EntityCriteria<? extends AbstractEntity> criteria) {
					criteria.add(Restrictions.eq("user", getAccount()));
				}
			
		};
		
		final SelectionColumn<Notification, String> selectionColumn;
		
		List<IColumn<Notification, String>> columns = new ArrayList<>();
		selectionColumn = new SelectionColumn<Notification, String>();
		if (getAccount().equals(getCurrentUser()))
			columns.add(selectionColumn);
		columns.add(new AbstractColumn<Notification, String>(Model.of("Pull Request"), "request") {

			@Override
			public void populateItem(
					Item<ICellPopulator<Notification>> cellItem,
					String componentId, IModel<Notification> rowModel) {
				Notification notification = rowModel.getObject();
				cellItem.add(new RequestLink(componentId, new EntityModel<PullRequest>(notification.getRequest())) {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("a");
					}
					
				});
			}
			
		});
		columns.add(new AbstractColumn<Notification, String>(Model.of("To Branch")) {

			@Override
			public void populateItem(
					Item<ICellPopulator<Notification>> cellItem,
					String componentId, IModel<Notification> rowModel) {
				PullRequest request = rowModel.getObject().getRequest();
				cellItem.add(new BranchLink(componentId, request.getTarget()) {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("a");
					}
					
				});
			}
			
		});
		columns.add(new AbstractColumn<Notification, String>(Model.of("From Branch")) {

			@Override
			public void populateItem(
					Item<ICellPopulator<Notification>> cellItem,
					String componentId, IModel<Notification> rowModel) {
				PullRequest request = rowModel.getObject().getRequest();
				if (request.getSource() != null) {
					cellItem.add(new BranchLink(componentId, request.getSource()) {
	
						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("a");
						}
						
					});
				} else {
					cellItem.add(new Label(componentId, request.getSource().getFQN()));
				}
			}
			
		});
		columns.add(new AbstractColumn<Notification, String>(Model.of("Task"), "task") {

			@Override
			public void populateItem(
					Item<ICellPopulator<Notification>> cellItem,
					String componentId, IModel<Notification> rowModel) {
				Notification notification = rowModel.getObject();
				cellItem.add(new Label(componentId, notification.getTask().toString()));
			}
			
		});
		columns.add(new AbstractColumn<Notification, String>(Model.of("When"), "date") {

			@Override
			public void populateItem(
					Item<ICellPopulator<Notification>> cellItem,
					String componentId, IModel<Notification> rowModel) {
				Notification notification = rowModel.getObject();
				cellItem.add(new Label(componentId, DateUtils.formatAge(notification.getDate())));
			}
			
		});
		
		add(new DefaultDataTable<Notification, String>("notifications", 
				columns, dataProvider, Constants.DEFAULT_PAGE_SIZE));
		
		add(new Link<Void>("deleteSelected") {

			@Override
			public void onClick() {
				if (selectionColumn.getSelections().isEmpty()) {
					getSession().warn("Please select notifications to delete");
				} else {
					for (IModel<Notification> model: selectionColumn.getSelections())
						GitPlex.getInstance(Dao.class).remove(model.getObject());
				}
				setResponsePage(AccountNotificationsPage.class, AccountNotificationsPage.paramsOf(getAccount()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getAccount().equals(getCurrentUser()));
			}
			
		});
	}

}