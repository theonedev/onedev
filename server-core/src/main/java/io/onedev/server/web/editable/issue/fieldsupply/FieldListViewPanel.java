package io.onedev.server.web.editable.issue.fieldsupply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import io.onedev.server.model.support.issue.fieldsupply.FieldSupply;
import io.onedev.server.model.support.issue.fieldsupply.ScriptingValue;
import io.onedev.server.model.support.issue.fieldsupply.SpecifiedValue;
import io.onedev.server.web.editable.PropertyContext;

@SuppressWarnings("serial")
class FieldListViewPanel extends Panel {

	private final List<FieldSupply> fields = new ArrayList<>();
	
	public FieldListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			fields.add((FieldSupply) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepeatingView fieldsView = new RepeatingView("fields");
		for (FieldSupply field: fields) {
			WebMarkupContainer container = new WebMarkupContainer(fieldsView.newChildId());
			container.add(new Label("name", field.getName()));
			
			if (field.getValueProvider() instanceof SpecifiedValue) {
				if (field.isSecret())
					container.add(new Label("valueProvider", SpecifiedValue.SECRET_DISPLAY_NAME));
				else
					container.add(new Label("valueProvider", SpecifiedValue.DISPLAY_NAME));
				List<String> value = field.getValueProvider().getValue();
				if (value.size() == 0) 
					container.add(new Label("value", "<i>Empty</i>").setEscapeModelStrings(false));
				else if (value.size() == 1)
					container.add(new Label("value", value.iterator().next()));
				else 
					container.add(new Label("value", value.toString()));
			} else if (field.getValueProvider() instanceof ScriptingValue) {
				if (field.isSecret())
					container.add(new Label("valueProvider", ScriptingValue.SECRET_DISPLAY_NAME));
				else
					container.add(new Label("valueProvider", ScriptingValue.DISPLAY_NAME));
				container.add(PropertyContext.view("value", field.getValueProvider(), "scriptName"));
			} else {
				container.add(new WebMarkupContainer("value"));
			}
			fieldsView.add(container);
		}
		add(fieldsView);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new FieldListCssResourceReference()));
	}
	
}
