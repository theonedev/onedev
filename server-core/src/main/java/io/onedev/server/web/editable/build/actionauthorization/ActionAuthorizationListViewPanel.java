package io.onedev.server.web.editable.build.actionauthorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;

@SuppressWarnings("serial")
class ActionAuthorizationListViewPanel extends Panel {

	private final List<ActionAuthorization> authorizations = new ArrayList<>();
	
	public ActionAuthorizationListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			authorizations.add((ActionAuthorization) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<ActionAuthorization, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ActionAuthorization, Void>(Model.of("Action")) {

			@Override
			public void populateItem(Item<ICellPopulator<ActionAuthorization>> cellItem, String componentId, IModel<ActionAuthorization> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getActionDescription()));
			}
			
		});		
		
		columns.add(new AbstractColumn<ActionAuthorization, Void>(Model.of("Authorized Branches")) {

			@Override
			public void populateItem(Item<ICellPopulator<ActionAuthorization>> cellItem, String componentId, IModel<ActionAuthorization> rowModel) {
				String authorizedBranches = rowModel.getObject().getAuthorizedBranches();
				if (authorizedBranches != null)
					cellItem.add(new Label(componentId, authorizedBranches));
				else
					cellItem.add(new Label(componentId, "<i>All</i>").setEscapeModelStrings(false));
			}
			
		});		
		
		IDataProvider<ActionAuthorization> dataProvider = new ListDataProvider<ActionAuthorization>() {

			@Override
			protected List<ActionAuthorization> getData() {
				return authorizations;
			}

		};
		
		add(new DataTable<ActionAuthorization, Void>("authorizations", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this));
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ActionAuthorizationCssResourceReference()));
	}

}
