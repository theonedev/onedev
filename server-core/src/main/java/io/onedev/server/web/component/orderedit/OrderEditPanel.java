package io.onedev.server.web.component.orderedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;

@SuppressWarnings("serial")
public class OrderEditPanel extends GenericPanel<List<EntitySort>> {

	private final List<String> available;
	
	public OrderEditPanel(String id, List<String> available, IModel<List<EntitySort>> selectedModel) {
		super(id, selectedModel);
		this.available = available;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getParent().add(AttributeAppender.append("class", "order-edit"));
		
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
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(selected, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(selected, fromIndex-i, fromIndex-i-1);
				}
				OrderEditPanel.this.setModelObject(selected);
				target.add(selectedContainer);
			}
			
		}.sortable("tbody"));
		
		add(selectedContainer.setOutputMarkupId(true));
		
		selectedContainer.add(new ListView<EntitySort>("selectedItems", getModel()) {

			@Override
			protected void populateItem(ListItem<EntitySort> item) {
				item.add(new Label("index", String.valueOf(item.getIndex()+1) + "."));
				
				EntitySort sort = item.getModelObject();
				item.add(new Label("name", sort.getField()));
				
				boolean descending = sort.getDirection() == EntitySort.Direction.DESCENDING;
				item.add(new AjaxCheckBox("descending", Model.of(descending)) {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						List<EntitySort> selected = new ArrayList<>(getSelected());
						if (getModelObject())
							sort.setDirection(EntitySort.Direction.DESCENDING);
						else
							sort.setDirection(EntitySort.Direction.ASCENDING);
						selected.set(item.getIndex(), sort);
						OrderEditPanel.this.setModelObject(selected);
					}
					
				});
				
				item.add(new AjaxLink<Void>("remove") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						List<EntitySort> selected = new ArrayList<>(getSelected());
						selected.remove(item.getIndex());
						OrderEditPanel.this.setModelObject(selected);
						target.add(OrderEditPanel.this);
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
		
		availableContainer.add(new ListView<String>("availableItems", 
				new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				List<String> available = new ArrayList<>(OrderEditPanel.this.available);
				available.removeAll(getSelected().stream().map(it->it.getField()).collect(Collectors.toSet()));
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
						sort.setDirection(EntitySort.Direction.ASCENDING);
						List<EntitySort> selected = new ArrayList<>(getSelected());
						selected.add(sort);
						OrderEditPanel.this.setModelObject(selected);
						target.add(OrderEditPanel.this);
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
		response.render(CssHeaderItem.forReference(new OrderEditCssResourceReference()));
	}

}
