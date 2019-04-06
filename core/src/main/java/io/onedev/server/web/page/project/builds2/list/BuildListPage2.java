package io.onedev.server.web.page.project.builds2.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ci.job.JobScheduler;
import io.onedev.server.entitymanager.Build2Manager;
import io.onedev.server.model.Build2;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.builds2.detail.BuildDetailPage;

@SuppressWarnings("serial")
public class BuildListPage2 extends ProjectPage {

	public BuildListPage2(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<Build2, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Build2, Void>(Model.of("#")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build2>> cellItem, String componentId,
					IModel<Build2> rowModel) {
				Build2 build = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "linkFrag", BuildListPage2.this);
				Link<Void> link = new BookmarkablePageLink<Void>("link", BuildDetailPage.class, 
						BuildDetailPage.paramsOf(build));
				link.add(new Label("label", build.getNumber()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Build2, Void>(Model.of("Status")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build2>> cellItem, String componentId,
					IModel<Build2> rowModel) {
				Build2 build = rowModel.getObject();
				cellItem.add(new Label(componentId, build.getStatus()));
			}
		});
		
		columns.add(new AbstractColumn<Build2, Void>(Model.of("Error Message")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build2>> cellItem, String componentId,
					IModel<Build2> rowModel) {
				Build2 build = rowModel.getObject();
				cellItem.add(new Label(componentId, build.getStatusMessage()));
			}
		});
		
		columns.add(new AbstractColumn<Build2, Void>(Model.of("Commit")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build2>> cellItem, String componentId,
					IModel<Build2> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getCommitHash()));
			}
		});
		
		columns.add(new AbstractColumn<Build2, Void>(Model.of("Job")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build2>> cellItem, String componentId,
					IModel<Build2> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getJobName()));
			}
		});
		
		columns.add(new AbstractColumn<Build2, Void>(Model.of("Params")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build2>> cellItem, String componentId,
					IModel<Build2> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getParamMap().toString()));
			}
		});
		
		columns.add(new AbstractColumn<Build2, Void>(Model.of("Dependencies")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build2>> cellItem, String componentId,
					IModel<Build2> rowModel) {
				List<String> dependencyIds = new ArrayList<>();
				for (BuildDependence dependence: rowModel.getObject().getDependencies())
					dependencyIds.add(dependence.getDependency().getNumberStr());
				cellItem.add(new Label(componentId, StringUtils.join(dependencyIds, ", ")));
			}
		});
		
		columns.add(new AbstractColumn<Build2, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<Build2>> cellItem, String componentId,
					IModel<Build2> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionsFrag", BuildListPage2.this);
				fragment.add(new Link<Void>("rebuild") {

					@Override
					public void onClick() {
						OneDev.getInstance(JobScheduler.class).resubmit(rowModel.getObject());
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(rowModel.getObject().isFinished());
					}
					
				});
				fragment.add(new Link<Void>("cancel") {

					@Override
					public void onClick() {
						OneDev.getInstance(JobScheduler.class).cancel(rowModel.getObject());
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!rowModel.getObject().isFinished());
					}
					
				});
				cellItem.add(fragment);
			}
		});
		
		SortableDataProvider<Build2, Void> dataProvider = new LoadableDetachableDataProvider<Build2, Void>() {

			@Override
			public Iterator<? extends Build2> iterator(long first, long count) {
				EntityCriteria<Build2> criteria = getCriteria();
				criteria.addOrder(Order.desc("number"));
				return OneDev.getInstance(Build2Manager.class).query(criteria, (int)first, (int)count).iterator();
			}

			private EntityCriteria<Build2> getCriteria() {
				EntityCriteria<Build2> criteria = EntityCriteria.of(Build2.class);
				criteria.add(Restrictions.eq("project", getProject()));
				return criteria;
			}
			
			@Override
			public long calcSize() {
				return OneDev.getInstance(Build2Manager.class).count(getCriteria());
			}

			@Override
			public IModel<Build2> model(Build2 object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Build2>() {

					@Override
					protected Build2 load() {
						return OneDev.getInstance(Build2Manager.class).load(id);
					}
					
				};
			}
		};
		
		add(new DefaultDataTable<Build2, Void>("builds", columns, dataProvider, WebConstants.PAGE_SIZE));
	}
	
}
