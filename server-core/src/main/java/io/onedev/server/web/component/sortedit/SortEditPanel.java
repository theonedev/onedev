package io.onedev.server.web.component.sortedit;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;

public class SortEditPanel<T extends AbstractEntity> extends GenericPanel<List<EntitySort>> {

	private final Map<String, Direction> available;
	
	public SortEditPanel(String id, Map<String, Direction> available, IModel<List<EntitySort>> selectedModel) {
		super(id, selectedModel);
		this.available = available;
	}

	public SortEditPanel(String id, List<String> available, IModel<List<EntitySort>> selectedModel) {
		super(id, selectedModel);
		this.available = new LinkedHashMap<>();
		for (var each: available)
			this.available.put(each, ASCENDING);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getParent().add(AttributeAppender.append("class", "sort-edit"));
		
		WebMarkupContainer selectedContainer = new WebMarkupContainer("selected") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getSelected().isEmpty());
			}
			
		};
		selectedContainer.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				List<EntitySort> selected = new ArrayList<>(getSelected());
				CollectionUtils.move(selected, from.getItemIndex(), to.getItemIndex());
				SortEditPanel.this.setModelObject(selected);
				target.add(selectedContainer);
			}
			
		}.sortable("tbody"));
		
		add(selectedContainer.setOutputMarkupId(true));
		
		selectedContainer.add(new ListView<>("selectedItems", getModel()) {

			@Override
			protected void populateItem(ListItem<EntitySort> item) {
				item.add(new Label("index", item.getIndex() + 1 + "."));

				EntitySort sort = item.getModelObject();
				item.add(new Label("name", sort.getField()));

				boolean descending = sort.getDirection() == Direction.DESCENDING;
				item.add(new AjaxCheckBox("descending", Model.of(descending)) {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						List<EntitySort> selected = new ArrayList<>(getSelected());
						if (getModelObject())
							sort.setDirection(Direction.DESCENDING);
						else
							sort.setDirection(ASCENDING);
						selected.set(item.getIndex(), sort);
						SortEditPanel.this.setModelObject(selected);
					}

				});

				item.add(new AjaxLink<Void>("remove") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						List<EntitySort> selected = new ArrayList<>(getSelected());
						selected.remove(item.getIndex());
						SortEditPanel.this.setModelObject(selected);
						target.add(SortEditPanel.this);
					}

				});
			}

		});
		
		WebMarkupContainer availableContainer = new WebMarkupContainer("available") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getSelected().size() != available.size());
			}
			
		};
		add(availableContainer);
		
		availableContainer.add(new ListView<>("availableItems",
				new LoadableDetachableModel<List<String>>() {

					@Override
					protected List<String> load() {
						List<String> available = new ArrayList<>(SortEditPanel.this.available.keySet());
						available.removeAll(getSelected().stream().map(it -> it.getField()).collect(Collectors.toSet()));
						return available;
					}

				}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String field = item.getModelObject();

				item.add(new AjaxLink<Void>("add") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("name", field));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						EntitySort sort = new EntitySort();
						sort.setField(field);
						sort.setDirection(available.get(field));
						List<EntitySort> selected = new ArrayList<>(getSelected());
						selected.add(sort);
						SortEditPanel.this.setModelObject(selected);
						target.add(SortEditPanel.this);
					}

				});
			}

		});
		
		setOutputMarkupId(true);
	}
	
	private List<EntitySort> getSelected() {
		return getModelObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SortEditCssResourceReference()));
	}

}
