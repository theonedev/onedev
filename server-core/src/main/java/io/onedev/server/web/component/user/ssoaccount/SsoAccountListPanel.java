package io.onedev.server.web.component.user.ssoaccount;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.SsoAccountService;
import io.onedev.server.model.SsoAccount;
import io.onedev.server.model.User;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.page.user.UserPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;

public class SsoAccountListPanel extends GenericPanel<User> {

    private DataTable<SsoAccount, Void> ssoAccountsTable;

    public SsoAccountListPanel(String id, IModel<User> model) {
        super(id, model);
    }

    private User getUser() {
        return getModelObject();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

		List<IColumn<SsoAccount, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<SsoAccount, Void>(Model.of("SSO Provider")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoAccount>> cellItem, String componentId,
					IModel<SsoAccount> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getProvider().getName()));
			}
			
		});
		
		columns.add(new AbstractColumn<SsoAccount, Void>(Model.of("Subject")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoAccount>> cellItem, String componentId, IModel<SsoAccount> rowModel) {
                cellItem.add(new Label(componentId, rowModel.getObject().getSubject()));
			}

		});
		
		columns.add(new AbstractColumn<SsoAccount, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoAccount>> cellItem, String componentId, 
					IModel<SsoAccount> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", SsoAccountListPanel.this);
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						var ssoAccount = rowModel.getObject();
						OneDev.getInstance(SsoAccountService.class).delete(ssoAccount);
						if (getPage() instanceof UserPage)
							OneDev.getInstance(AuditService.class).audit(null, "deleted SSO account \"" + ssoAccount.getProvider() + "/" + ssoAccount.getSubject() + "\" from account \"" + ssoAccount.getUser().getName() + "\"", null, null);
						Session.get().success(_T("SSO account deleted"));
						target.add(ssoAccountsTable);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = _T("Do you really want to delete this SSO account?");
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});
		
		SortableDataProvider<SsoAccount, Void> dataProvider = new LoadableDetachableDataProvider<SsoAccount, Void>() {

			@Override
			public Iterator<? extends SsoAccount> iterator(long first, long count) {
				var ssoAccounts = new ArrayList<>(getUser().getSsoAccounts());
                Collections.sort(ssoAccounts, new Comparator<SsoAccount>() {    
                    @Override
                    public int compare(SsoAccount o1, SsoAccount o2) {
                        return (o1.getProvider().getName() + "/" + o1.getSubject()).compareTo(o2.getProvider().getName() + "/" + o2.getSubject());
                    }
                });
                return ssoAccounts.iterator();
			}

			@Override
			public long calcSize() {
				return getUser().getSsoAccounts().size();
			}

			@Override
			public IModel<SsoAccount> model(SsoAccount ssoAccount) {
				Long id = ssoAccount.getId();
				return new LoadableDetachableModel<SsoAccount>() {

					@Override
					protected SsoAccount load() {
						return OneDev.getInstance(SsoAccountService.class).load(id);
					}
					
				};
			}
		};
		
		add(ssoAccountsTable = new DefaultDataTable<SsoAccount, Void>("ssoAccounts", columns, dataProvider, 
				Integer.MAX_VALUE, null));
    }
}
