package io.onedev.server.ee.dashboard.widgets;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.Widget;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Editable
public class WidgetWrapper implements Serializable {
	
	private Widget widget;

	@Editable
	@NotNull
	public Widget getWidget() {
		return widget;
	}

	public void setWidget(Widget widget) {
		this.widget = widget;
	}
}
