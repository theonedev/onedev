package io.onedev.server.web.page.admin.ssosetting;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.SsoProviderService;
import io.onedev.server.model.SsoProvider;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.page.admin.AdministrationPage;

public class SsoProviderListPage extends AdministrationPage {
	
	private DataTable<SsoProvider, Void> providersTable;

	public SsoProviderListPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("addNew", NewSsoProviderPage.class));
		
		List<IColumn<SsoProvider, Void>> columns = new ArrayList<>();
				
		columns.add(new AbstractColumn<SsoProvider, Void>(Model.of(_T("Name"))) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoProvider>> cellItem, String componentId, IModel<SsoProvider> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", SsoProviderListPage.this);
				var link = new BookmarkablePageLink<Void>("link", SsoProviderDetailPage.class, 
						SsoProviderDetailPage.paramsOf(rowModel.getObject()));
				link.add(new Label("label", rowModel.getObject().getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});		
		
		columns.add(new AbstractColumn<SsoProvider, Void>(Model.of(_T("Callback URL"))) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoProvider>> cellItem, String componentId, IModel<SsoProvider> rowModel) {
				SsoProvider provider = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "callbackUriFrag", SsoProviderListPage.this);
				fragment.add(new Label("value", provider.getConnector().getCallbackUri(provider.getName()).toString()));
				fragment.add(new CopyToClipboardLink("copy", Model.of(provider.getConnector().getCallbackUri(provider.getName()).toString())));
				cellItem.add(fragment);
			}
			
		});		
		
		columns.add(new AbstractColumn<SsoProvider, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoProvider>> cellItem, String componentId, IModel<SsoProvider> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", SsoProviderListPage.this);
				fragment.add(AttributeAppender.append("class", "text-nowrap"));
				fragment.add(new BookmarkablePageLink<>("edit", SsoProviderDetailPage.class, SsoProviderDetailPage.paramsOf(rowModel.getObject())));
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = MessageFormat.format(_T("Do you really want to delete SSO provider \"{0}\"?"), rowModel.getObject().getName());
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						SsoProvider provider = rowModel.getObject();
						var oldAuditContent = VersionedXmlDoc.fromBean(provider).toXML();
						getSsoProviderService().delete(provider);
						OneDev.getInstance(AuditService.class).audit(null, "deleted SSO provider \"" + provider.getName() + "\"", oldAuditContent, null);
						Session.get().success(MessageFormat.format(_T("SSO provider \"{0}\" deleted"), provider.getName()));
						target.add(providersTable);
					}

				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});		
		
		SortableDataProvider<SsoProvider, Void> dataProvider = new SortableDataProvider<>() {

			@Override
			public Iterator<? extends SsoProvider> iterator(long first, long count) {
				return getSsoProviderService().query().iterator();
			}

			@Override
			public long size() {
				return getSsoProviderService().count();
			}

			@Override
			public IModel<SsoProvider> model(SsoProvider object) {
				Long id = object.getId();
				return new LoadableDetachableModel<>() {

					@Override
					protected SsoProvider load() {
						return getSsoProviderService().load(id);
					}

				};
			}
		};
		
		add(providersTable = new DefaultDataTable<>("providers", columns, dataProvider, Integer.MAX_VALUE, null));			
		providersTable.setOutputMarkupId(true);
	}

	private SsoProviderService getSsoProviderService() {
		return OneDev.getInstance(SsoProviderService.class);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>" + _T("SSO Providers") + "</span>").setEscapeModelStrings(false);
	}
	
}
