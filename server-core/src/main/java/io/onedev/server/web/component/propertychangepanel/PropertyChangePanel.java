package io.onedev.server.web.component.propertychangepanel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

@SuppressWarnings("serial")
public class PropertyChangePanel extends Panel {

	private final List<PropertyChange> changes = new ArrayList<>();
	
	private final boolean showName;
	
	public PropertyChangePanel(String id, Map<String, String> oldProperties, Map<String, String> newProperties, 
			boolean hideNameIfOnlyOneChange) {
		super(id);

		MapDifference<String, String> diff = Maps.difference(oldProperties, newProperties);
		for (Map.Entry<String, ValueDifference<String>> entry: diff.entriesDiffering().entrySet()) { 
			changes.add(new PropertyChange(entry.getKey(), 
					entry.getValue().leftValue(), entry.getValue().rightValue()));
		}
		for (Map.Entry<String, String> entry: diff.entriesOnlyOnLeft().entrySet())  
			changes.add(new PropertyChange(entry.getKey(), entry.getValue(), null));
		for (Map.Entry<String, String> entry: diff.entriesOnlyOnRight().entrySet())  
			changes.add(new PropertyChange(entry.getKey(), null, entry.getValue()));

		showName = !hideNameIfOnlyOneChange || changes.size() > 1;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("nameHeader").setVisible(showName));
		
		add(new ListView<PropertyChange>("properties", changes) {

			@Override
			protected void populateItem(ListItem<PropertyChange> item) {
				PropertyChange change = item.getModelObject();
				item.add(new Label("name", change.name).setVisible(showName));
				if (change.oldValue != null)
					item.add(new Label("oldValue", change.oldValue));
				else
					item.add(new Label("oldValue", "<i>empty</i>").setEscapeModelStrings(false));
				if (change.newValue != null)
					item.add(new Label("newValue", change.newValue));
				else
					item.add(new Label("newValue", "<i>empty</i>").setEscapeModelStrings(false));
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PropertyChangeCssResourceReference()));
	}

	private static class PropertyChange implements Serializable {
		
		private String name;
		
		private String oldValue;
		
		private String newValue;
		
		PropertyChange(String name, String oldValue, String newValue) {
			this.name = name;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		
	}
}
