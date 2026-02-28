package io.onedev.server.web.editable.userconfig;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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

import io.onedev.server.model.support.workspace.spec.UserConfig;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.draw.DrawCardPanel;
import io.onedev.server.web.component.draw.DrawPanel;
import io.onedev.server.web.editable.BeanContext;

class UserConfigListViewPanel extends Panel {

	private final List<UserConfig> configs = new ArrayList<>();
	
	public UserConfigListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			configs.add((UserConfig) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<UserConfig, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<UserConfig, Void>(Model.of(_T("Path"))) {

			@Override
			public void populateItem(Item<ICellPopulator<UserConfig>> cellItem, String componentId, IModel<UserConfig> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getPath()));
			}
			
		});		
		
		columns.add(new AbstractColumn<UserConfig, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<UserConfig>> cellItem, String componentId, IModel<UserConfig> rowModel) {
				int configIndex = cellItem.findParent(Item.class).getIndex();
				Fragment fragment = new Fragment(componentId, "showDetailFrag", UserConfigListViewPanel.this);
				fragment.add(new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new DrawCardPanel(target, DrawPanel.Placement.RIGHT) {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(AttributeAppender.append("class", "user-config"));
							}

							@Override
							protected Component newTitle(String componentId) {
								return new Label(componentId, configs.get(configIndex).getPath());
							}

							@Override
							protected Component newBody(String id) {
								return BeanContext.view(id, configs.get(configIndex));
							}

						};
					}
					
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "ellipsis text-right";
			}
			
		});		
		
		IDataProvider<UserConfig> dataProvider = new ListDataProvider<UserConfig>() {

			@Override
			protected List<UserConfig> getData() {
				return configs;
			}

		};
		
		add(new DataTable<UserConfig, Void>("configs", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this, Model.of(_T("Unspecified"))));
				add(new NoRecordsBehavior());
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserConfigCssResourceReference()));
	}

}
