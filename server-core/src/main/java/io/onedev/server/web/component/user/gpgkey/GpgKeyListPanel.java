package io.onedev.server.web.component.user.gpgkey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.GpgKey;
import io.onedev.server.util.GpgUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.util.LoadableDetachableDataProvider;

@SuppressWarnings("serial")
public class GpgKeyListPanel extends GenericPanel<List<GpgKey>> {

	private DataTable<GpgKey, Void> gpgKeysTable;
	
    public GpgKeyListPanel(String id, IModel<List<GpgKey>> model) {
        super(id, model);
    }
    
    private List<GpgKey> getKeys() {
    	return getModelObject();
    }
    
    @Override
    public void onInitialize() {
    	super.onInitialize();
        
		List<IColumn<GpgKey, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<GpgKey, Void>(Model.of("Key ID")) {

			@Override
			public void populateItem(Item<ICellPopulator<GpgKey>> cellItem, String componentId,
					IModel<GpgKey> rowModel) {
				cellItem.add(new Label(componentId, 
						GpgUtils.getKeyIDString(rowModel.getObject().getKeyId())));
			}
			
		});
		
		columns.add(new AbstractColumn<GpgKey, Void>(Model.of("Email Addresses")) {

			@Override
			public void populateItem(Item<ICellPopulator<GpgKey>> cellItem, String componentId,
					IModel<GpgKey> rowModel) {
				GpgKey key = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "emailAddressesFrag", GpgKeyListPanel.this);
				RepeatingView valuesView = new RepeatingView("values");
				EmailAddressManager emailAddressManager = OneDev.getInstance(EmailAddressManager.class);
				for (String emailAddressValue: GpgUtils.getEmailAddresses(key.getPublicKeys().get(0))) {
					WebMarkupContainer container = new WebMarkupContainer(valuesView.newChildId());
					valuesView.add(container);
					container.add(new Label("value", emailAddressValue));
					EmailAddress emailAddress = emailAddressManager.findByValue(emailAddressValue);
					boolean unverified = emailAddress == null 
							|| !emailAddress.isVerified() 
							|| !emailAddress.getOwner().equals(key.getOwner());
					container.add(new WebMarkupContainer("ineffective").setVisible(unverified));
				}
				fragment.add(valuesView);

				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "email-addresses";
			}
			
		});
		
		columns.add(new AbstractColumn<GpgKey, Void>(Model.of("Sub Keys")) {

			@Override
			public String getCssClass() {
				return "expanded";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<GpgKey>> cellItem, String componentId,
					IModel<GpgKey> rowModel) {
				GpgKey key = rowModel.getObject();
				String subKeyIds = key.getKeyIds().stream()
						.filter(it->it!=key.getKeyId())
						.map(it->GpgUtils.getKeyIDString(it))
						.collect(Collectors.joining("\n"));
				if (subKeyIds.length() != 0)
					cellItem.add(new MultilineLabel(componentId, subKeyIds));
				else
					cellItem.add(new Label(componentId, "<i>None</i>").setEscapeModelStrings(false));
			}
			
		});
		
		columns.add(new AbstractColumn<GpgKey, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<GpgKey>> cellItem, String componentId, 
					IModel<GpgKey> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", GpgKeyListPanel.this);
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						GpgKey GpgKey = rowModel.getObject();
						OneDev.getInstance(GpgKeyManager.class).delete(GpgKey);
						Session.get().success("GPG key deleted");
						target.add(gpgKeysTable);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = "Do you really want to delete this GPG key?";
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
		
		SortableDataProvider<GpgKey, Void> dataProvider = new LoadableDetachableDataProvider<GpgKey, Void>() {

			@Override
			public Iterator<? extends GpgKey> iterator(long first, long count) {
				return getKeys().iterator();
			}

			@Override
			public long calcSize() {
				return getKeys().size();
			}

			@Override
			public IModel<GpgKey> model(GpgKey gpgKey) {
				Long id = gpgKey.getId();
				return new LoadableDetachableModel<GpgKey>() {

					@Override
					protected GpgKey load() {
						return OneDev.getInstance(GpgKeyManager.class).load(id);
					}
					
				};
			}
		};
		
		add(gpgKeysTable = new DefaultDataTable<GpgKey, Void>("keys", columns, dataProvider, 
				Integer.MAX_VALUE, null));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GpgKeyCssResourceReference()));
	}
    
}
