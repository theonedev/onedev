package io.onedev.server.model.support.widget;

import io.onedev.server.annotation.Editable;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Editable
public class WidgetTabWrapper implements Serializable {
	
	private WidgetTab tab;

	@Editable
	@NotNull
	public WidgetTab getTab() {
		return tab;
	}

	public void setTab(WidgetTab tab) {
		this.tab = tab;
	}
	
}
