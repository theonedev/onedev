package io.onedev.server.web.component.entity.reference;

import io.onedev.server.util.Referenceable;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class ReferencePanel extends Panel {
	
	private Referenceable entity;
	
	public ReferencePanel(String id, Referenceable entity) {
		super(id);
		this.entity = entity;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String displayReference = String.format("%s#%d", entity.getProject(), entity.getNumber());
		String reference = entity.getPrefix() + " " + displayReference;
		
		add(new Label("reference", displayReference));
		add(new CopyToClipboardLink("copy", Model.of(reference)));
	}
}
