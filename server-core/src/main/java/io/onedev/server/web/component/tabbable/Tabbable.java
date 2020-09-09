package io.onedev.server.web.component.tabbable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class Tabbable extends GenericPanel<List<? extends Tab>> {
	
	private static final String OPTIONS_ID = "options";
	
	public Tabbable(String id, IModel<List<? extends Tab>> tabsModel) {
		super(id, tabsModel);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Tabbable(String id, List<? extends Tab> tabs) {
		this(id, new Model((Serializable) tabs));
	}
	
	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		checkComponentTag(openTag, "ul");
	}
	
	private List<? extends Tab> getTabs() {
		return getModelObject();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		List<ActionTab> actionTabs = new ArrayList<>();
		for (Tab tab: getTabs()) {
			if (tab instanceof ActionTab)
				actionTabs.add((ActionTab) tab);
		}
		if (!actionTabs.isEmpty()) {
			boolean hasSelection = false;
			for (Tab tab: actionTabs) {
				if (tab.isSelected()) {
					hasSelection = true;
					break;
				}
			}
			if (!hasSelection)
				actionTabs.get(0).setSelected(true);
		}
		
		add(new ListView<Tab>("tabs", new LoadableDetachableModel<List<Tab>>() {

			@Override
			protected List<Tab> load() {
				return getTabs().stream().collect(Collectors.toList());
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Tab> item) {
				Tab tab = item.getModelObject();
				if (tab.isSelected())
					item.add(AttributeModifier.append("class", "active"));
				if (item.getIndex() == 0)
					item.add(AttributeModifier.append("class", "first"));
				if (item.getIndex() == getTabs().size()-1)
					item.add(AttributeModifier.append("class", "last"));

				item.add(tab.render("tab"));
			}
			
		});

		setOutputMarkupId(true);
	}
	
	@Override
	protected void onBeforeRender() {
		boolean found = false;
		for (Tab tab: getTabs()) {
			if (tab.isSelected()) {
				Component options = tab.renderOptions(OPTIONS_ID);
				if (options != null) {
					addOrReplace(options);
					found = true;
				}
				break;
			}
		}
		if (!found)
			addOrReplace(new WebMarkupContainer(OPTIONS_ID).setVisible(false));
		
		super.onBeforeRender();
	}

}
