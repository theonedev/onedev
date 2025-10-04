package io.onedev.server.web.page.admin.groupmanagement;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.GroupService;
import io.onedev.server.model.Group;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.groupmanagement.create.NewGroupPage;
import io.onedev.server.web.page.admin.groupmanagement.profile.GroupProfilePage;
import io.onedev.server.web.util.TextUtils;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;

public class GroupListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private TextField<String> searchField;
	
	private DataTable<Group, Void> groupsTable;
	
	private String query;
	
	private boolean typing;

	public GroupListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}
	
	@Override
	protected void onBeforeRender() {
		typing = false;
		super.onBeforeRender();
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(searchField);
		target.add(groupsTable);
	}
	
	private WebMarkupContainer newProfileLink(String componentId, Group group) {
		return new ActionablePageLink(componentId, GroupProfilePage.class, GroupProfilePage.paramsOf(group)) {

			@Override
			protected void doBeforeNav(AjaxRequestTarget target) {
				String redirectUrlAfterDelete = RequestCycle.get().urlFor(
						GroupListPage.class, getPageParameters()).toString();
				WebSession.get().setRedirectUrlAfterDelete(Group.class, redirectUrlAfterDelete);
			}

		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(searchField = new TextField<String>("filterGroups", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
				PageParameters params = getPageParameters();
				params.set(PARAM_QUERY, query);
				params.remove(PARAM_PAGE);
				
				String url = RequestCycle.get().urlFor(GroupListPage.class, params).toString();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (typing)
					replaceState(target, url, query);
				else
					pushState(target, url, query);
				
				groupsTable.setCurrentPage(0);
				target.add(groupsTable);
				
				typing = true;
			}
			
		}));
		
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
			}

		});
		
		add(new Link<Void>("addNew") {

			@Override
			public void onClick() {
				setResponsePage(NewGroupPage.class);
			}
			
		});
		
		List<IColumn<Group, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<>(Model.of(_T("Name"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId, IModel<Group> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", GroupListPage.this);
				Group group = rowModel.getObject();
				var link = newProfileLink("link", group);
				link.add(new Label("label", group.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<>(Model.of(_T("Is Site Admin"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
									 IModel<Group> rowModel) {
				cellItem.add(new Label(componentId, _T(TextUtils.getDisplayValue(rowModel.getObject().isAdministrator()))));
			}

		});
		
		columns.add(new AbstractColumn<Group, Void>(Model.of(_T("Can Create Root Projects"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
					IModel<Group> rowModel) {
				Group group = rowModel.getObject();
				cellItem.add(new Label(componentId, _T(TextUtils.getDisplayValue(group.isAdministrator() || group.isCreateRootProjects()))));
			}
			
		});
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId, IModel<Group> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", GroupListPage.this);

				fragment.add(newProfileLink("edit", rowModel.getObject()));				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = MessageFormat.format(_T("Do you really want to delete group \"{0}\"?"), rowModel.getObject().getName());
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						Group group = rowModel.getObject();
						var oldAuditContent = VersionedXmlDoc.fromBean(group).toXML();
						OneDev.getInstance(GroupService.class).delete(group);
						OneDev.getInstance(AuditService.class).audit(null, "deleted group \"" + group.getName() + "\"", oldAuditContent, null);
						Session.get().success(MessageFormat.format(_T("Group \"{0}\" deleted"), group.getName()));
						target.add(groupsTable);
					}

				});

				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}

		});
		
		SortableDataProvider<Group, Void> dataProvider = new SortableDataProvider<>() {

			@Override
			public Iterator<? extends Group> iterator(long first, long count) {
				return OneDev.getInstance(GroupService.class).query(query, (int) first, (int) count).iterator();
			}

			@Override
			public long size() {
				return OneDev.getInstance(GroupService.class).count(query);
			}

			@Override
			public IModel<Group> model(Group object) {
				Long id = object.getId();
				return new LoadableDetachableModel<>() {

					@Override
					protected Group load() {
						return OneDev.getInstance(GroupService.class).load(id);
					}

				};
			}
		};

		PagingHistorySupport pagingHistorySupport = new ParamPagingHistorySupport() {
			
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

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Groups"));
	}

}
