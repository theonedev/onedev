package io.onedev.server.web.component.tabbable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import org.jspecify.annotations.Nullable;

public abstract class ActionTab extends Tab {

	private static final long serialVersionUID = 1L;

	private final IModel<String> titleModel;
	
	private final IModel<String> iconModel;
	
	private boolean selected;
	
	public ActionTab(IModel<String> titleModel, @Nullable IModel<String> iconModel) {
		this.titleModel = titleModel;
		this.iconModel = iconModel;
	}

	public ActionTab(IModel<String> titleModel) {
		this(titleModel, null);
	}
	
	protected final IModel<String> getTitleModel() {
		return titleModel;
	}
	
	@Nullable
	protected final IModel<String> getIconModel() {
		return iconModel;
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
