package io.onedev.server.web.page.admin.usermanagement;

import java.io.Serializable;
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
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserInvitationManager;
import io.onedev.server.model.UserInvitation;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ShowGlobalAjaxIndicatorListener;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
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
		
		columns.add(new AbstractColumn<UserInvitation, Void>(Model.of("Email Address")) {

			@Override
			public void populateItem(Item<ICellPopulator<UserInvitation>> cellItem, 
					String componentId, IModel<UserInvitation> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getEmailAddress()));
			}
			
		});
		
		columns.add(new AbstractColumn<UserInvitation, Void>(Model.of("")) {

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
						if (OneDev.getInstance(SettingManager.class).getMailSetting() != null) {
							UserInvitation invitation = rowModel.getObject();
							getInvitationManager().sendInvitationEmail(invitation);
							Session.get().success("Invitation sent to '" + invitation.getEmailAddress() + "'");
						} else {
							Session.get().error("Mail settings not specified");
						}
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = "Do you really want to cancel invitation to '" 
								+ rowModel.getObject().getEmailAddress() + "'?";
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						UserInvitation invitation = rowModel.getObject();
						getInvitationManager().delete(invitation);
						Session.get().success("Invitation to '" + invitation.getEmailAddress() + "' deleted");
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
				return getInvitationManager().query(query, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return getInvitationManager().count(query);
			}

			@Override
			public IModel<UserInvitation> model(UserInvitation object) {
				Long id = object.getId();
				return new LoadableDetachableModel<UserInvitation>() {

					@Override
					protected UserInvitation load() {
						return getInvitationManager().load(id);
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
		
		add(invitationsTable = new DefaultDataTable<>("invitations", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}
	
	private UserInvitationManager getInvitationManager() {
		return OneDev.getInstance(UserInvitationManager.class);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Invitations");
	}

}
