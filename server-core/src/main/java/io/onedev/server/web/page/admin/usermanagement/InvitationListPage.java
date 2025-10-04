package io.onedev.server.web.page.admin.usermanagement;

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
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserInvitationService;
import io.onedev.server.model.UserInvitation;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ShowGlobalAjaxIndicatorListener;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;

public class InvitationListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private TextField<String> searchField;
	
	private DataTable<UserInvitation, Void> invitationsTable;
	
	private String query;
	
	private boolean typing;

	public InvitationListPage(PageParameters params) {
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
		target.add(invitationsTable);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(searchField = new TextField<String>("filterInvitations", new IModel<String>() {

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
				
				String url = RequestCycle.get().urlFor(InvitationListPage.class, params).toString();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (typing)
					replaceState(target, url, query);
				else
					pushState(target, url, query);
				
				invitationsTable.setCurrentPage(0);
				target.add(invitationsTable);
				
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
				setResponsePage(NewInvitationPage.class);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		List<IColumn<UserInvitation, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<>(Model.of(_T("Email Address"))) {

			@Override
			public void populateItem(Item<ICellPopulator<UserInvitation>> cellItem,
									 String componentId, IModel<UserInvitation> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getEmailAddress()));
			}

		});
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<UserInvitation>> cellItem,
									 String componentId, IModel<UserInvitation> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", InvitationListPage.this);

				fragment.add(new AjaxLink<Void>("resend") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ShowGlobalAjaxIndicatorListener());
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (OneDev.getInstance(SettingService.class).getMailConnector() != null) {
							UserInvitation invitation = rowModel.getObject();
							getInvitationService().sendInvitationEmail(invitation);
							Session.get().success(MessageFormat.format(_T("Invitation sent to \"{0}\""), invitation.getEmailAddress()));
						} else {
							Session.get().error(_T("Mail service not configured"));
						}
					}

				});

				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = MessageFormat.format(_T("Do you really want to cancel invitation to \"{0}\"?"), rowModel.getObject().getEmailAddress());
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						UserInvitation invitation = rowModel.getObject();
						getInvitationService().delete(invitation);
						Session.get().success(MessageFormat.format(_T("Invitation to \"{0}\" deleted"), invitation.getEmailAddress()));
						target.add(invitationsTable);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.isAdministrator());
					}

				});

				cellItem.add(fragment);
			}

		});
		
		SortableDataProvider<UserInvitation, Void> dataProvider = new SortableDataProvider<UserInvitation, Void>() {

			@Override
			public Iterator<? extends UserInvitation> iterator(long first, long count) {
				return getInvitationService().query(query, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return getInvitationService().count(query);
			}

			@Override
			public IModel<UserInvitation> model(UserInvitation object) {
				Long id = object.getId();
				return new LoadableDetachableModel<UserInvitation>() {

					@Override
					protected UserInvitation load() {
						return getInvitationService().load(id);
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
		
		add(invitationsTable = new DefaultDataTable<>("invitations", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}
	
	private UserInvitationService getInvitationService() {
		return OneDev.getInstance(UserInvitationService.class);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Invitations"));
	}

}
