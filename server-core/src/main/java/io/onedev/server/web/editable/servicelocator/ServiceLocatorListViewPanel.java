package io.onedev.server.web.editable.servicelocator;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Sets;

import io.onedev.server.model.support.administration.jobexecutor.ServiceLocator;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

class ServiceLocatorListViewPanel extends DrawCardBeanListViewPanel<ServiceLocator> {

	private static final long serialVersionUID = 1L;

	ServiceLocatorListViewPanel(String id, List<Serializable> elements) {
		super(id, elements);
	}

	@Override
	protected String getDetailTitle(ServiceLocator item) {
		return _T("Service Locator");
	}

	@Override
	protected Component newDetailBody(String id, ServiceLocator item) {
		return BeanContext.view(id, item, Sets.newHashSet("job"), true);
	}

	@Override
	protected List<IColumn<ServiceLocator, Void>> getDataColumns() {
		List<IColumn<ServiceLocator, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of(_T("Applicable Services"))) {

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
