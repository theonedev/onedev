package io.onedev.server.web.component.modal;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

@SuppressWarnings("serial")
public abstract class ModalLink extends AjaxLink<Void> {

	private ModalPanel modal;
	
	private final ModalPanel.Size size;
	
	public ModalLink(String id, ModalPanel.Size size) {
		super(id);
		this.size = size;
	}

	public ModalLink(String id) {
		this(id, ModalPanel.Size.MEDIUM);
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		// if modal has not been created, or has been removed from page 
		// when the same page instance is refreshed 
		if (modal == null || modal.getParent() == null) {
			modal = new ModalPanel(target, size) {
	
				@Override
				protected Component newContent(String id) {
					return ModalLink.this.newContent(id, this);
				}

				@Override
				protected void onClosed() {
					super.onClosed();
					modal = null;
				}
				
			};
		}
	}

	protected abstract Component newContent(String id, ModalPanel modal);
}
