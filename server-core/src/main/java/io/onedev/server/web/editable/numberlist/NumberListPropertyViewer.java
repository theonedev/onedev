package io.onedev.server.web.editable.numberlist;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;

public class NumberListPropertyViewer extends Panel {

	private final List<Number> elements;

	public NumberListPropertyViewer(String id, List<Number> elements) {
		super(id);
		this.elements = elements;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ListView<>("elements", elements) {

			@Override
			protected void populateItem(ListItem<Number> item) {
				item.add(new Label("value", item.getModelObject().toString()));
			}
		});
	}

}
