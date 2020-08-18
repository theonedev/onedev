package io.onedev.server.web.component.tabbable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

public abstract class ActionTab extends Tab {

	private static final long serialVersionUID = 1L;

	private IModel<String> titleModel;
	
	private boolean selected;
	
	public ActionTab(IModel<String> titleModel) {
		this.titleModel = titleModel;
	}
	
	protected final IModel<String> getTitleModel() {
		return titleModel;
	}
	
	@Override
	public Component render(String componentId) {
		return new ActionTabHead(componentId, this);
	}

	public ActionTab setSelected(boolean selected) {
		this.selected = selected;
		return this;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}
	
	@SuppressWarnings("unchecked")
	public void selectTab(Component tabLink) {
		ListView<ActionTab> tabItems = tabLink.findParent(ListView.class);
		for (ActionTab each: tabItems.getModelObject())
			each.setSelected(false);
		
		setSelected(true);
		onSelect(tabLink);
	}

	@Override
	public String getTitle() {
		return titleModel.getObject();
	}

	protected abstract void onSelect(Component tabLink);
}
