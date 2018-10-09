package io.onedev.server.web.page.project.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;

@SuppressWarnings("serial")
class ContributionListPanel extends Panel {

	public ContributionListPanel(String id, IModel<List<DayContribution>> contributionsModel) {
		super(id, contributionsModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<DayContribution, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<DayContribution, Void>(Model.of("Date")) {

			@Override
			public void populateItem(Item<ICellPopulator<DayContribution>> cellItem, String componentId, 
					IModel<DayContribution> rowModel) {
				cellItem.add(new Label(componentId, DateUtils.formatDate(rowModel.getObject().getDay().getDate())));
			}
		});

		columns.add(new AbstractColumn<DayContribution, Void>(Model.of("Commits")) {

			@Override
			public void populateItem(Item<ICellPopulator<DayContribution>> cellItem, String componentId, 
					IModel<DayContribution> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getContribution().getCommits()));
			}
		});
		
		columns.add(new AbstractColumn<DayContribution, Void>(Model.of("Additions")) {

			@Override
			public void populateItem(Item<ICellPopulator<DayContribution>> cellItem, String componentId, 
					IModel<DayContribution> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getContribution().getAdditions()));
			}
		});
		
		columns.add(new AbstractColumn<DayContribution, Void>(Model.of("Deletions")) {

			@Override
			public void populateItem(Item<ICellPopulator<DayContribution>> cellItem, String componentId, 
					IModel<DayContribution> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getContribution().getDeletions()));
			}
		});
		
		SortableDataProvider<DayContribution, Void> dataProvider = new LoadableDetachableDataProvider<DayContribution, Void>() {

			@Override
			public Iterator<? extends DayContribution> iterator(long first, long count) {
				if (first + count <= getContribution().size())
					return getContribution().subList((int)first, (int)(first+count)).iterator();
				else
					return getContribution().subList((int)first, getContribution().size()).iterator();
			}

			@Override
			public long calcSize() {
				return getContribution().size();
			}

			@Override
			public IModel<DayContribution> model(DayContribution object) {
				return Model.of(object);
			}
		};
		
		DataTable<DayContribution, Void> contributionTable = 
				new DataTable<DayContribution, Void>("table", columns, dataProvider, WebConstants.PAGE_SIZE);		
		
		contributionTable.addTopToolbar(new HeadersToolbar<>(contributionTable, dataProvider));
		contributionTable.addBottomToolbar(new AjaxNavigationToolbar(contributionTable));
		contributionTable.setOutputMarkupId(true);
		add(contributionTable);
	}

	@SuppressWarnings("unchecked")
	private List<DayContribution> getContribution() {
		return (List<DayContribution>) getDefaultModelObject();
	}
	
}
