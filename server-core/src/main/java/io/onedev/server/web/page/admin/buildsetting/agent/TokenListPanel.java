package io.onedev.server.web.page.admin.buildsetting.agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.model.AgentToken;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.util.LoadableDetachableDataProvider;

@SuppressWarnings("serial")
public class TokenListPanel extends GenericPanel<List<AgentToken>> {

	private String note;
	
	public TokenListPanel(String id) {
		super(id);
		
		setModel(new LoadableDetachableModel<List<AgentToken>>() {

			@Override
			protected List<AgentToken> load() {
				List<AgentToken> tokens = getTokenManager().queryUnused();
				Collections.sort(tokens);
				return tokens;
			}
			
		});
	}

	private AgentTokenManager getTokenManager() {
		return OneDev.getInstance(AgentTokenManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.add(new TextField<String>("note", new PropertyModel<String>(this, "note")));
		form.add(new AjaxButton("add") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				AgentToken token = new AgentToken();
				token.setValue(UUID.randomUUID().toString());
				token.setNote(note);
				getTokenManager().save(token);
				target.add(TokenListPanel.this);
				
				note = null;
			}
			
		});
		add(form);
		
		List<IColumn<AgentToken, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<AgentToken, Void>(Model.of("Value")) {

			@Override
			public void populateItem(Item<ICellPopulator<AgentToken>> cellItem, String componentId,
					IModel<AgentToken> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getValue()));
			}

			@Override
			public String getCssClass() {
				return "text-nowrap align-top font-size-sm text-monospace";
			}
			
		});
		
		columns.add(new AbstractColumn<AgentToken, Void>(Model.of("Note")) {

			@Override
			public void populateItem(Item<ICellPopulator<AgentToken>> cellItem, String componentId,
					IModel<AgentToken> rowModel) {
				AgentToken token = rowModel.getObject();
				if (token.getNote() != null)
					cellItem.add(new Label(componentId, token.getNote()));
				else
					cellItem.add(new Label(componentId, "<i>No note</i>").setEscapeModelStrings(false));
			}
			
			@Override
			public String getCssClass() {
				return "align-top font-size-sm";
			}
			
		});
		
		columns.add(new AbstractColumn<AgentToken, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<AgentToken>> cellItem, String componentId,
					IModel<AgentToken> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionsFrag", TokenListPanel.this);

				AgentToken token = rowModel.getObject();

				fragment.add(new CopyToClipboardLink("copy", Model.of(token.getValue())));
				
				Long tokenId = token.getId();
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this token?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						getTokenManager().delete(getTokenManager().load(tokenId));
						target.add(TokenListPanel.this);
					}

				});
				
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
				return getModelObject().iterator();
			}

			@Override
			public long calcSize() {
				return getModelObject().size();
			}

			@Override
			public IModel<AgentToken> model(AgentToken token) {
				Long tokenId = token.getId();
				return new LoadableDetachableModel<AgentToken>() {

					@Override
					protected AgentToken load() {
						return getTokenManager().load(tokenId);
					}
					
				};
			}
			
		};
		
		DataTable<AgentToken, Void> table;
		add(table = new DataTable<AgentToken, Void>("tokens", columns, dataProvider, Integer.MAX_VALUE));
		table.addBottomToolbar(new NoRecordsToolbar(table));
		table.add(new NoRecordsBehavior());
	}

}
