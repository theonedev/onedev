package io.onedev.server.web.component.markdown;

import org.apache.wicket.Component;
import org.jspecify.annotations.Nullable;

import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;

public abstract class MarkdownOutlineDropdownLink extends DropdownLink {

	public MarkdownOutlineDropdownLink(String id) {
		super(id, AlignPlacement.bottom(0), false, true);
	}

	@Nullable
	protected abstract MarkdownViewer getViewer();

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new MarkdownOutlinePanel(id, getViewer(), false);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getViewer() != null);
	}

}
