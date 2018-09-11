package io.onedev.server.web.page.project.issues.issuedetail.changedfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.IssueInfoManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.build.BuildStatusPanel;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;

@SuppressWarnings("serial")
public class FixedInBuildsPage extends IssueDetailPage {

	private final IModel<List<Build>> buildsModel = new LoadableDetachableModel<List<Build>>() {

		@Override
		protected List<Build> load() {
			Map<Configuration, Build> buildMap = new HashMap<>();
			for (String buildUUID: OneDev.getInstance(IssueInfoManager.class).getFixedInBuildUUIDs(getProject(), getIssue().getUUID())) {
				Build build = OneDev.getInstance(BuildManager.class).find(buildUUID);
				Build existingBuild = buildMap.get(build.getConfiguration());
				if (existingBuild == null || existingBuild.getDate().before(build.getDate()))
					buildMap.put(build.getConfiguration(), build);
			}
			List<Build> buildList = new ArrayList<>(buildMap.values());
			Collections.sort(buildList);
			return buildList;
		}
		
	};
	
	public FixedInBuildsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		buildsModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<Build, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Build, Void>(Model.of("Configuration")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId,
					IModel<Build> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getConfiguration().getName()));
			}
		});
		
		columns.add(new AbstractColumn<Build, Void>(Model.of("Build")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId,
					IModel<Build> rowModel) {
				cellItem.add(new Label(componentId, "#" + rowModel.getObject().getNumber()));
			}
		});
		
		columns.add(new AbstractColumn<Build, Void>(Model.of("Description")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId,
					IModel<Build> rowModel) {
				Fragment fragment = new Fragment(componentId, "descriptionFrag", FixedInBuildsPage.this);

				fragment.add(new BuildStatusPanel("status", rowModel));
				ExternalLink link = new ExternalLink("description", rowModel.getObject().getUrl());
				link.add(new Label("label", rowModel.getObject().getDescription()));
				fragment.add(link);
				
				cellItem.add(fragment);
			}
			
		});
		
		columns.add(new AbstractColumn<Build, Void>(Model.of("Date")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId,
					IModel<Build> rowModel) {
				cellItem.add(new Label(componentId, DateUtils.formatDateTime(rowModel.getObject().getDate())));
			}
			
		});
		
		SortableDataProvider<Build, Void> dataProvider = new SortableDataProvider<Build, Void>() {

			@Override
			public Iterator<? extends Build> iterator(long first, long count) {
				return buildsModel.getObject().iterator();
			}

			@Override
			public long size() {
				return buildsModel.getObject().size();
			}

			@Override
			public IModel<Build> model(Build object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Build>() {

					@Override
					protected Build load() {
						return OneDev.getInstance(BuildManager.class).load(id);
					}
					
				};
			}
		};
		
		DataTable<Build, Void> buildsTable = new DataTable<Build, Void>("builds", columns, dataProvider, Integer.MAX_VALUE);
		buildsTable.addTopToolbar(new AjaxFallbackHeadersToolbar<Void>(buildsTable, dataProvider));
		buildsTable.addBottomToolbar(new NoRecordsToolbar(buildsTable));
		add(buildsTable);
	}

}
