package io.onedev.server.web.page.admin.gpgtrustedkeys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.bouncycastle.openpgp.PGPPublicKey;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.BaseGpgKey;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.util.GpgUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;

@SuppressWarnings("serial")
public class GpgTrustedKeysPage extends AdministrationPage {

	public GpgTrustedKeysPage(PageParameters params) {
		super(params);
	}

	private DataTable<Long, Void> trustedKeysTable;
	
    @Override
    public void onInitialize() {
    	super.onInitialize();
    	
    	add(new ModalLink("newKey") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "addKeyFrag", GpgTrustedKeysPage.this);
				
		        fragment.add(new AjaxLink<Void>("close") {

		            @Override
		            public void onClick(AjaxRequestTarget target) {
		            	modal.close();
		            }
		            
		        });
		        
		        Form<?> form = new Form<Void>("form");
		        
		        BeanEditor editor = BeanContext.edit("editor", new BaseGpgKey());
		        form.add(editor);
		        
		        form.add(new AjaxButton("add") {
		        	
		            @Override
		            protected void onSubmit(AjaxRequestTarget target, Form<?> myform) {
		                super.onSubmit(target, myform);
		                
		                BaseGpgKey bean = (BaseGpgKey) editor.getModelObject();
		                
		                GpgSetting setting = getSettingManager().getGpgSetting();
		                if (bean.getKeyIds().stream().anyMatch(it-> setting.getTrustedSignatureVerificationKey(it) != null)) {
							editor.error(
									new Path(new PathNode.Named(BaseGpgKey.PROP_CONTENT)), 
									"This key or one of its sub key is already added");
							target.add(form);
		                } else {
			                setting.getEncodedTrustedKeys().put(bean.getKeyIds().get(0), bean.getContent());
							setting.encodedTrustedKeysUpdated();
		                	getSettingManager().saveGpgSetting(setting);
		                	target.add(trustedKeysTable);
		                	modal.close();
		                }
		            }
		            
		            @Override
		            protected void onError(AjaxRequestTarget target, Form<?> form) {
		                super.onError(target, form);
		                target.add(form);
		            }
		            
		        });
		        
		        form.add(new AjaxLink<Void>("cancel") {
		            
		        	@Override
		            public void onClick(AjaxRequestTarget target) {
		        		modal.close();
		            }
		            
		        });
		        
		        fragment.add(form.setOutputMarkupId(true));
		        
		        return fragment;
			}
    		
    	});
        
		List<IColumn<Long, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Long, Void>(Model.of("Key ID")) {

			@Override
			public void populateItem(Item<ICellPopulator<Long>> cellItem, String componentId,
					IModel<Long> rowModel) {
				List<PGPPublicKey> trustedKey = getTrustedKey(rowModel.getObject());
				cellItem.add(new Label(componentId, GpgUtils.getKeyIDString(trustedKey.get(0).getKeyID())));
			}
			
		});
		
		columns.add(new AbstractColumn<Long, Void>(Model.of("Email Addresses")) {

			@Override
			public void populateItem(Item<ICellPopulator<Long>> cellItem, String componentId,
					IModel<Long> rowModel) {
				List<PGPPublicKey> trustedKey = getTrustedKey(rowModel.getObject());
				String joined = StringUtils.join(GpgUtils.getEmailAddresses(trustedKey.get(0)), "\n");
				cellItem.add(new MultilineLabel(componentId, joined));
			}
			
		});
		
		columns.add(new AbstractColumn<Long, Void>(Model.of("Sub Keys")) {

			@Override
			public void populateItem(Item<ICellPopulator<Long>> cellItem, String componentId,
					IModel<Long> rowModel) {
				List<PGPPublicKey> trustedKey = getTrustedKey(rowModel.getObject());
				long keyId = trustedKey.get(0).getKeyID();
				String subKeyIds = trustedKey.stream()
						.map(it->it.getKeyID())
						.filter(it->it!=keyId)
						.map(it->GpgUtils.getKeyIDString(it))
						.collect(Collectors.joining("\n"));
				if (subKeyIds.length() != 0)
					cellItem.add(new MultilineLabel(componentId, subKeyIds));
				else
					cellItem.add(new Label(componentId, "<i>None</i>").setEscapeModelStrings(false));
			}

			@Override
			public String getCssClass() {
				return "expanded";
			}
			
		});
		
		columns.add(new AbstractColumn<Long, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Long>> cellItem, String componentId, 
					IModel<Long> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", GpgTrustedKeysPage.this);
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						GpgSetting setting = getSettingManager().getGpgSetting();
						setting.getEncodedTrustedKeys().remove(rowModel.getObject());
						setting.encodedTrustedKeysUpdated();
						getSettingManager().saveGpgSetting(setting);
						Session.get().success("GPG key deleted");
						target.add(trustedKeysTable);
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
		
		SortableDataProvider<Long, Void> dataProvider = new LoadableDetachableDataProvider<Long, Void>() {

			@Override
			public Iterator<? extends Long> iterator(long first, long count) {
				return getSettingManager().getGpgSetting().getEncodedTrustedKeys().keySet().iterator();
			}

			@Override
			public long calcSize() {
				return getSettingManager().getGpgSetting().getEncodedTrustedKeys().size();
			}

			@Override
			public IModel<Long> model(Long keyId) {
				return Model.of(keyId);
			}
		};
		
		add(trustedKeysTable = new DefaultDataTable<Long, Void>("keys", columns, dataProvider, 
				Integer.MAX_VALUE, null));
	}
    
    private List<PGPPublicKey> getTrustedKey(long keyId) {
    	return getSettingManager().getGpgSetting().getTrustedKeys().get(keyId);
    }
    
    private SettingManager getSettingManager() {
    	return OneDev.getInstance(SettingManager.class);
    }

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GpgTrustedKeysCssResourceReference()));
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "GPG Trusted Keys");
	}

}
