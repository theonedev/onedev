package io.onedev.server.codequality;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class ContainerTargetKeyPanel extends Panel {
	
	private final String name;
	
	private final String platform;
	
	public ContainerTargetKeyPanel(String id, String name, String platform) {
		super(id);
		this.name = name;
		this.platform = platform;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("name", name));
		add(new Label("platform", platform));
	}
	
}
