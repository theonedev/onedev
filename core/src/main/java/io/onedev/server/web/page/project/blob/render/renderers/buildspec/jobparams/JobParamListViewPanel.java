package io.onedev.server.web.page.project.blob.render.renderers.buildspec.jobparams;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Preconditions;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.layout.SideFloating;
import io.onedev.utils.StringUtils;
import jersey.repackaged.com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class JobParamListViewPanel extends Panel {

	private final List<InputSpec> params = new ArrayList<>();
	
	public JobParamListViewPanel(String id, Class<?> elementClass, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			params.add((InputSpec) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<InputSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel, rowModel.getObject().getName()));
			}
		});		
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				InputSpec param = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, rowModel, EditableUtils.getDisplayName(param.getClass())));
			}
		});		
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("Allow Empty")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				InputSpec param = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, rowModel, StringUtils.describe(param.isAllowEmpty())));
			}
		});		
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				cellItem.add(new Label(componentId, "<a title='Click the row for more info'><i class='fa fa-ellipsis-h'></i></a>").setEscapeModelStrings(false));
			}

			@Override
			public String getCssClass() {
				return "ellipsis";
			}
			
		});		
		
		IDataProvider<InputSpec> dataProvider = new ListDataProvider<InputSpec>() {

			@Override
			protected List<InputSpec> getData() {
				return params;
			}

		};
		
		add(new DataTable<InputSpec, Void>("params", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this));
			}
			
		});
	}

	private int getParamIndex(String paramName) {
		for (int i=0; i<params.size(); i++) {
			if (params.get(i).getName().equals(paramName))
				return i;
		}
		return -1;
	}
	
	private class ColumnFragment extends Fragment {

		private final int index;
		
		private final String label;
		
		public ColumnFragment(String id, IModel<InputSpec> model, String label) {
			super(id, "columnFrag", JobParamListViewPanel.this, model);
			this.index = getParamIndex(getParam().getName());
			Preconditions.checkState(this.index != -1);
			this.label = label;
		}
		
		private InputSpec getParam() {
			return (InputSpec) getDefaultModelObject();
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new SideFloating(target, SideFloating.Placement.RIGHT) {

						@Override
						protected String getTitle() {
							return getParam().getName() + " (type: " + EditableUtils.getDisplayName(getParam().getClass()) + ")";
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "job-param def-detail"));
						}

						@Override
						protected Component newBody(String id) {
							return BeanContext.viewBean(id, getParam(), Sets.newHashSet("name"), true);
						}

					};
				}
				
			};
			link.add(new Label("label", label));
			add(link);
		}
		
	}
}
