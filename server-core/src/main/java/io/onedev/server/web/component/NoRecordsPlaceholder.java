package io.onedev.server.web.component;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;

@SuppressWarnings("serial")
public class NoRecordsPlaceholder extends WebMarkupContainer {

	private final IPageableItems pageable;
	
	public NoRecordsPlaceholder(String id, IPageableItems pageable) {
		super(id);
		this.pageable = pageable;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", "norecords"));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(pageable.getItemCount() == 0);
	}

}
