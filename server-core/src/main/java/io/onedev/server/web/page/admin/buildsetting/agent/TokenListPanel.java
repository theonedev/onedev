package io.onedev.server.web.page.admin.buildsetting.agent;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.service.AgentTokenService;
import io.onedev.server.service.AuditService;
import io.onedev.server.model.AgentToken;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.util.LoadableDetachableDataProvider;

public class TokenListPanel extends GenericPanel<List<AgentToken>> {
	
	public TokenListPanel(String id) {
		super(id);
		
		setModel(new LoadableDetachableModel<List<AgentToken>>() {

			@Override
			protected List<AgentToken> load() {
				List<AgentToken> tokens = getTokenService().queryUnused();
				Collections.sort(tokens);
				return tokens;
			}
			
		});
	}

	private AgentTokenService getTokenService() {
		return OneDev.getInstance(AgentTokenService.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("addNew") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				AgentToken token = new AgentToken();
				getTokenService().createOrUpdate(token);
				getAuditService().audit(null, "created agent token", null, null);
				target.add(TokenListPanel.this);
			}
			
		});
		
		add(new AjaxLink<Void>("deleteAll") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete unused tokens?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getTokenService().deleteUnused();
				getAuditService().audit(null, "deleted unused agent tokens", null, null);
				target.add(TokenListPanel.this);
				target.add(this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getUnusedTokens().isEmpty());
			}
			
		});
		
		List<IColumn<AgentToken, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<AgentToken, Void>(Model.of(_T("Value"))) {

			@Override
			public void populateItem(Item<ICellPopulator<AgentToken>> cellItem, String componentId,
					IModel<AgentToken> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getValue()));
			}

			@Override
			public String getCssClass() {
				return "text-nowrap align-top text-monospace";
			}
			
		});
		
		columns.add(new AbstractColumn<AgentToken, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<AgentToken>> cellItem, String componentId,
					IModel<AgentToken> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionsFrag", TokenListPanel.this);

				AgentToken token = rowModel.getObject();
				fragment.add(new CopyToClipboardLink("copy", Model.of(token.getValue())));
				cellItem.add(fragment);
			}
			
			@Override
			public String getCssClass() {
				return "text-nowrap align-top";
			}
			
		});
		
		SortableDataProvider<AgentToken, Void> dataProvider = new LoadableDetachableDataProvider<AgentToken, Void>() {

			@Override
			public Iterator<? extends AgentToken> iterator(long first, long count) {
				return getUnusedTokens().iterator();
			}

			@Override
			public long calcSize() {
				return getUnusedTokens().size();
			}

			@Override
			public IModel<AgentToken> model(AgentToken token) {
				Long tokenId = token.getId();
				return new LoadableDetachableModel<AgentToken>() {

					@Override
					protected AgentToken load() {
						return getTokenService().load(tokenId);
					}
					
				};
			}
			
		};
		
		DataTable<AgentToken, Void> table;
		add(table = new DataTable<AgentToken, Void>("tokens", columns, dataProvider, Integer.MAX_VALUE) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getUnusedTokens().isEmpty());
			}
			
		});
		table.addBottomToolbar(new NoRecordsToolbar(table));
	}

	private List<AgentToken> getUnusedTokens() {
		return getModelObject();
	}
	
	private AuditService getAuditService() {
		return OneDev.getInstance(AuditService.class);
	}
	
}
