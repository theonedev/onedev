package com.gitplex.server.web.page.project.stats;

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

import com.gitplex.server.git.DayAndCommits;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.util.DateUtils;

@SuppressWarnings("serial")
public class ContributionPanel extends Panel {

	public ContributionPanel(String id, IModel<List<DayAndCommits>> contributionModel) {
		super(id, contributionModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<DayAndCommits, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<DayAndCommits, Void>(Model.of("Date")) {

			@Override
			public void populateItem(Item<ICellPopulator<DayAndCommits>> cellItem, String componentId, 
					IModel<DayAndCommits> rowModel) {
				cellItem.add(new Label(componentId, DateUtils.formatDate(rowModel.getObject().getDay().getDate())));
			}
		});

		columns.add(new AbstractColumn<DayAndCommits, Void>(Model.of("Commits")) {

			@Override
			public void populateItem(Item<ICellPopulator<DayAndCommits>> cellItem, String componentId, 
					IModel<DayAndCommits> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getCommits()));
			}
		});
		
		SortableDataProvider<DayAndCommits, Void> dataProvider = new SortableDataProvider<DayAndCommits, Void>() {

			@Override
			public Iterator<? extends DayAndCommits> iterator(long first, long count) {
				if (first + count <= getContribution().size())
					return getContribution().subList((int)first, (int)(first+count)).iterator();
				else
					return getContribution().subList((int)first, getContribution().size()).iterator();
			}

			@Override
			public long size() {
				return getContribution().size();
			}

			@Override
			public IModel<DayAndCommits> model(DayAndCommits object) {
				return Model.of(object);
			}
		};
		
		DataTable<DayAndCommits, Void> contributionTable = 
				new DataTable<DayAndCommits, Void>("table", columns, dataProvider, WebConstants.PAGE_SIZE);		
		
		contributionTable.addTopToolbar(new HeadersToolbar<>(contributionTable, dataProvider));
		contributionTable.addBottomToolbar(new AjaxNavigationToolbar(contributionTable));
		contributionTable.setOutputMarkupId(true);
		add(contributionTable);
	}

	@SuppressWarnings("unchecked")
	private List<DayAndCommits> getContribution() {
		return (List<DayAndCommits>) getDefaultModelObject();
	}
	
}
