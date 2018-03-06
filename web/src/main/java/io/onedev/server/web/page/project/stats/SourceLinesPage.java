package io.onedev.server.web.page.project.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.git.LineStats;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.util.Day;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.util.DateUtils;

@SuppressWarnings("serial")
public class SourceLinesPage extends ProjectStatsPage {

	private IModel<LineStats> lineStatsModel = new LoadableDetachableModel<LineStats>() {

		@Override
		protected LineStats load() {
			return OneDev.getInstance(CommitInfoManager.class).getLineStats(getProject());
		}
		
	};
	
	public SourceLinesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		lineStatsModel.detach();
		super.onDetach();
	}

	private LineStats getLineStats() {
		return lineStatsModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<String>("languages", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				Day lastDay = getLineStats().getLastDay();
				if (lastDay != null) {
					Map<String, Integer> linesOnDay = getLineStats().getLinesByDay().get(lastDay);
					List<String> languages = new ArrayList<>(linesOnDay.keySet());
					Collections.sort(languages, new Comparator<String>() {

						@Override
						public int compare(String o1, String o2) {
							return linesOnDay.get(o2) - linesOnDay.get(o1);
						}
						
					});
					return languages;
				} else {
					return new ArrayList<>();
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("language", item.getModelObject()));
				
				List<IColumn<Day, Void>> columns = new ArrayList<>();
				
				columns.add(new AbstractColumn<Day, Void>(Model.of("Date")) {

					@Override
					public void populateItem(Item<ICellPopulator<Day>> cellItem, String componentId, 
							IModel<Day> rowModel) {
						cellItem.add(new Label(componentId, DateUtils.formatDate(rowModel.getObject().getDate())));
					}
				});

				columns.add(new AbstractColumn<Day, Void>(Model.of("Lines")) {

					@Override
					public void populateItem(Item<ICellPopulator<Day>> cellItem, String componentId, 
							IModel<Day> rowModel) {
						String language = item.getModelObject();
						Day day = rowModel.getObject();
						int lines = getLineStats().getLinesByLanguage().get(language).get(day);
						cellItem.add(new Label(componentId, lines));
					}
					
				});
				
				SortableDataProvider<Day, Void> dataProvider = new SortableDataProvider<Day, Void>() {

					private List<Day> getDays() {
						List<Day> days = new ArrayList<>();
						days = new ArrayList<>(getLineStats().getLinesByLanguage().get(item.getModelObject()).keySet());
						Collections.sort(days);
						return days;
					}
					
					@Override
					public Iterator<? extends Day> iterator(long first, long count) {
						List<Day> days = getDays();
						if (first + count <= days.size())
							return days.subList((int)first, (int)(first+count)).iterator();
						else
							return days.subList((int)first, days.size()).iterator();
					}

					@Override
					public long size() {
						return getDays().size();
					}

					@Override
					public IModel<Day> model(Day object) {
						return Model.of(object);
					}
				};
				
				DataTable<Day, Void> linesTable = 
						new DataTable<Day, Void>("lines", columns, dataProvider, WebConstants.PAGE_SIZE);		
				
				linesTable.addTopToolbar(new HeadersToolbar<>(linesTable, dataProvider));
				linesTable.addBottomToolbar(new AjaxNavigationToolbar(linesTable));
				linesTable.setOutputMarkupId(true);
				item.add(linesTable);				
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.stats.lineStats.onDomReady();"));
	}

}
