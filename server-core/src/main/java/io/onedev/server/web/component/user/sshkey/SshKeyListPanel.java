package io.onedev.server.web.component.user.sshkey;

import java.util.ArrayList;
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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.util.LoadableDetachableDataProvider;

@SuppressWarnings("serial")
public class SshKeyListPanel extends GenericPanel<List<SshKey>> {

	private DataTable<SshKey, Void> sshKeysTable;
	
    public SshKeyListPanel(String id, IModel<List<SshKey>> model) {
        super(id, model);
    }
    
    private List<SshKey> getKeys() {
    	return getModelObject();
    }
    
    @Override
    public void onInitialize() {
    	super.onInitialize();
        
		List<IColumn<SshKey, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<SshKey, Void>(Model.of("Digest")) {

			@Override
			public void populateItem(Item<ICellPopulator<SshKey>> cellItem, String componentId,
					IModel<SshKey> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getDigest()));
			}
			
		});
		
		columns.add(new AbstractColumn<SshKey, Void>(Model.of("Comment")) {

			@Override
			public void populateItem(Item<ICellPopulator<SshKey>> cellItem, String componentId,
					IModel<SshKey> rowModel) {
				String comment = rowModel.getObject().getComment();
				if (comment != null)
					cellItem.add(new Label(componentId, comment));
				else
					cellItem.add(new Label(componentId, "<i>No comment</i>").setEscapeModelStrings(false));
			}
			
		});
		
		columns.add(new AbstractColumn<SshKey, Void>(Model.of("Created At")) {

			@Override
			public String getCssClass() {
				return "expanded";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<SshKey>> cellItem, String componentId,
					IModel<SshKey> rowModel) {
				cellItem.add(new Label(componentId, DateUtils.formatDateTime(rowModel.getObject().getCreatedAt())));
			}
			
		});
		
		columns.add(new AbstractColumn<SshKey, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<SshKey>> cellItem, String componentId, 
					IModel<SshKey> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", SshKeyListPanel.this);
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						SshKey sshKey = rowModel.getObject();
						OneDev.getInstance(SshKeyManager.class).delete(sshKey);
						Session.get().success("SSH key deleted");
						target.add(sshKeysTable);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = "Do you really want to delete this SSH key?";
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

				}.setVisible(!rowModel.getObject().getOwner().isSshKeyExternalManaged()));
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});
		
		SortableDataProvider<SshKey, Void> dataProvider = new LoadableDetachableDataProvider<SshKey, Void>() {

			@Override
			public Iterator<? extends SshKey> iterator(long first, long count) {
				return getKeys().iterator();
			}

			@Override
			public long calcSize() {
				return getKeys().size();
			}

			@Override
			public IModel<SshKey> model(SshKey sshKey) {
				Long id = sshKey.getId();
				return new LoadableDetachableModel<SshKey>() {

					@Override
					protected SshKey load() {
						return OneDev.getInstance(SshKeyManager.class).load(id);
					}
					
				};
			}
		};
		
		add(sshKeysTable = new OneDataTable<SshKey, Void>("keys", columns, dataProvider, 
				Integer.MAX_VALUE, null));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SshKeyCssResourceReference()));
	}
    
}
