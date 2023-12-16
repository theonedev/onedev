package io.onedev.server.ee.dashboard.widgets.composite;

import io.onedev.server.annotation.Editable;
import io.onedev.server.ee.dashboard.widgets.WidgetWrapper;
import io.onedev.server.model.support.Widget;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Editable(name="Composite", order=100000)
public class CompositeWidget extends Widget {

	private static final long serialVersionUID = 1L;

	private List<WidgetWrapper> widgetWrappers;

	@Editable(order=100, name="Widgets", description = "Each added widget will be displayed as a tab in this composite widget")
	@Size(min=2, message = "At least two widgets should be added")
	public List<WidgetWrapper> getWidgetWrappers() {
		return widgetWrappers;
	}

	public void setWidgetWrappers(List<WidgetWrapper> widgetWrappers) {
		this.widgetWrappers = widgetWrappers;
	}

	@Override
	protected Component doRender(String componentId) {
		var widgets = widgetWrappers.stream()
				.map(WidgetWrapper::getWidget).collect(toList());
		return new CompositeWidgetPanel(componentId, widgets);
	}

}
