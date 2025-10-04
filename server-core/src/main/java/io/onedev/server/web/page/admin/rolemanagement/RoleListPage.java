package io.onedev.server.web.page.admin.rolemanagement;

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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
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
import io.onedev.server.service.RoleService;
import io.onedev.server.model.Role;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;

public class RoleListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private DataTable<Role, Void> rolesTable;
	
	private TextField<String> searchField;
	
	private boolean typing;
	
	private String query;

	public RoleListPage(PageParameters params) {
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
		target.add(rolesTable);
	}
	
	private WebMarkupContainer newDetailLink(String componentId, Role role) {
		return new ActionablePageLink(componentId, 
				RoleDetailPage.class, RoleDetailPage.paramsOf(role)) {

			@Override
			public void doBeforeNav(AjaxRequestTarget target) {
				String redirectUrlAfterDelete = RequestCycle.get().urlFor(
						RoleListPage.class, getPageParameters()).toString();
				WebSession.get().setRedirectUrlAfterDelete(Role.class, redirectUrlAfterDelete);
			}
		
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(searchField = new TextField<String>("filterRoles", new IModel<String>() {

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
				
				String url = RequestCycle.get().urlFor(RoleListPage.class, params).toString();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (typing)
					replaceState(target, url, query);
				else
					pushState(target, url, query);
				
				rolesTable.setCurrentPage(0);
				target.add(rolesTable);
				
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
				setResponsePage(NewRolePage.class);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		List<IColumn<Role, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Role, Void>(Model.of(_T("Name"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Role>> cellItem, String componentId, IModel<Role> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", RoleListPage.this);
				
				Role role = rowModel.getObject();
				
				WebMarkupContainer link = newDetailLink("link", role);
				link.add(new Label("label", role.getName()));
				fragment.add(link);
				
				cellItem.add(fragment);
			}

			
		});

		columns.add(new AbstractColumn<Role, Void>(Model.of(_T("Description"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Role>> cellItem, String componentId, IModel<Role> rowModel) {
				cellItem.add(new MultiLineLabel(componentId, rowModel.getObject().getDescription()));
			}
			
		});
		
		columns.add(new AbstractColumn<Role, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Role>> cellItem, String componentId, IModel<Role> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", RoleListPage.this);

				fragment.add(newDetailLink("edit", rowModel.getObject()));

				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = MessageFormat.format(_T("Do you really want to delete role \"{0}\"?"), rowModel.getObject().getName());						
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						Role role = rowModel.getObject();
						OneDev.getInstance(RoleService.class).delete(role);
						var oldAuditContent = VersionedXmlDoc.fromBean(role).toXML();
						OneDev.getInstance(AuditService.class).audit(null, "deleted role \"" + role.getName() + "\"", oldAuditContent, null);
						Session.get().success(MessageFormat.format(_T("Role \"{0}\" deleted"), role.getName()));
						target.add(rolesTable);
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (!isEnabled()) {
							tag.put("disabled", "disabled");
							tag.put("data-tippy-content", _T("This is a built-in role and can not be deleted"));
						}
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setEnabled(!rowModel.getObject().isOwner());
					}
					
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "text-nowrap";
			}
			
		});
		
		SortableDataProvider<Role, Void> dataProvider = new SortableDataProvider<Role, Void>() {

			@Override
			public Iterator<? extends Role> iterator(long first, long count) {
				return OneDev.getInstance(RoleService.class).query(query, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return OneDev.getInstance(RoleService.class).count(query);
			}

			@Override
			public IModel<Role> model(Role object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Role>() {

					@Override
					protected Role load() {
						return OneDev.getInstance(RoleService.class).load(id);
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
		
		add(rolesTable = new DefaultDataTable<>("roles", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Roles"));
	}

}
