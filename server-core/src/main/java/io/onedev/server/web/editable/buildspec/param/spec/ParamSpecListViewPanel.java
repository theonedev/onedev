package io.onedev.server.web.editable.buildspec.param.spec;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Sets;

import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.web.asset.inputspec.InputSpecCssResourceReference;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

class ParamSpecListViewPanel extends DrawCardBeanListViewPanel<ParamSpec> {

	private static final long serialVersionUID = 1L;

	ParamSpecListViewPanel(String id, List<Serializable> elements) {
		super(id, elements);
	}

	@Override
	protected String getDetailTitle(ParamSpec item) {
		String paramType = EditableUtils.getDisplayName(item.getClass());
		return _T("Parameter Spec") + " (" + _T("type") + ": " + paramType + ")";
	}

	@Override
	protected Component newDetailBody(String id, ParamSpec item) {
		Set<String> excludedProperties = Sets.newHashSet("canBeChangedBy", "nameOfEmptyValue");
		return BeanContext.view(id, item, excludedProperties, true);
	}

	@Override
	protected String additionalDetailCssClass() {
		return "param-spec input-spec";
	}

	@Override
	protected List<IColumn<ParamSpec, Void>> getDataColumns() {
		List<IColumn<ParamSpec, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of(_T("Name"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}

		});

		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of(_T("Type"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				cellItem.add(new Label(componentId, EditableUtils.getDisplayName(rowModel.getObject().getClass())));
			}

		});

		return columns;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new InputSpecCssResourceReference()));
		response.render(CssHeaderItem.forReference(new ParamSpecCssResourceReference()));
	}

}
