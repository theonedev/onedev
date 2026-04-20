package io.onedev.server.web.editable.servicelocator;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.model.support.administration.jobexecutor.ServiceLocator;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;

class ServiceLocatorListEditPanel extends DrawCardBeanListEditPanel<ServiceLocator> {

	private static final long serialVersionUID = 1L;

	ServiceLocatorListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
	}

	private BuildSpec getBuildSpec() {
		BuildSpecAware buildSpecAware = findParent(BuildSpecAware.class);
		return buildSpecAware != null ? buildSpecAware.getBuildSpec() : null;
	}

	private Job getJob() {
		JobAware jobAware = findParent(JobAware.class);
		return jobAware != null ? jobAware.getJob() : null;
	}

	@Override
	protected String getAddTooltip() {
		return _T("Add new service locator");
	}

	@Override
	protected DrawCardBeanItemEditPanel<ServiceLocator> newEditPanel(String id, List<ServiceLocator> items, int index,
			DrawCardBeanItemEditPanel.EditCallback callback) {
		return new ServiceLocatorEditPanel(id, items, index, callback) {

			private static final long serialVersionUID = 1L;

			@Override
			public BuildSpec getBuildSpec() {
				return ServiceLocatorListEditPanel.this.getBuildSpec();
			}

			@Override
			public Job getJob() {
				return ServiceLocatorListEditPanel.this.getJob();
			}

			@Override
			public List<ParamSpec> getParamSpecs() {
				return getJob() != null ? getJob().getParamSpecs() : null;
			}

		};
	}

	@Override
	protected List<IColumn<ServiceLocator, Void>> getDataColumns() {
		List<IColumn<ServiceLocator, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of(_T("Applicable Names"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				if (rowModel.getObject().getServiceNames() != null)
					cellItem.add(new Label(componentId, rowModel.getObject().getServiceNames()));
				else
					cellItem.add(new Label(componentId, "<i>" + _T("All") + "</i>").setEscapeModelStrings(false));
			}

		});

		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of(_T("Applicable Images"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				if (rowModel.getObject().getServiceImages() != null)
					cellItem.add(new Label(componentId, rowModel.getObject().getServiceImages()));
				else
					cellItem.add(new Label(componentId, "<i>" + _T("All") + "</i>").setEscapeModelStrings(false));
			}

		});

		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of(_T("#Node Selector Entries"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getNodeSelector().size()));
			}

		});

		return columns;
	}

}
