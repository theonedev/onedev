package io.onedev.server.web.editable.interpolativestringlist;

import io.onedev.server.web.editable.PropertyDescriptor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;

public class InterpolativeStringListPropertyViewer extends Panel {
	
	private final List<String> elements;
	
	public InterpolativeStringListPropertyViewer(String id, PropertyDescriptor descriptor, List<String> elements) {
		super(id);
		this.elements = elements;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<>("elements", elements) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("value", item.getModelObject()));
			}
		});
	}

}
