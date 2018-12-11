package io.onedev.server.web.component.tabbable;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
public class Tabbable extends Panel {
	
	private static final String OPTIONS_ID = "options";
	
	private final List<Tab> tabs;
	
	public Tabbable(String id, List<? extends Tab> tabs) {
		super(id);
		
		this.tabs = new ArrayList<>();
		for (Tab tab: tabs) {
			this.tabs.add(tab);
		}
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		checkComponentTag(openTag, "ul");
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		List<ActionTab> actionTabs = new ArrayList<>();
		for (Tab tab: tabs) {
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
		
		add(new ListView<Tab>("tabs", tabs){

			@Override
			protected void populateItem(ListItem<Tab> item) {
				Tab tab = item.getModelObject();
				if (tab.isSelected())
					item.add(AttributeModifier.append("class", "active"));

				item.add(tab.render("tab"));
			}
			
		});

		if (tabs.size() > 1) {
			Fragment fragment = new Fragment("collapsed", "multiTabCollapsedFrag", this);
			fragment.add(new DropdownLink("link") {

				@Override
				protected void onInitialize(FloatingPanel dropdown) {
					super.onInitialize(dropdown);
					dropdown.add(AttributeAppender.append("class", "menu tabs"));
				}

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new TabsFragment(id, dropdown);
				}

				@Override
				protected void onBeforeRender() {
					boolean found = false;
					for (Tab tab: tabs) {
						if (tab.isSelected()) {
							addOrReplace(new Label("label", tab.getTitle()));
							found = true;
							break;
						}
					}
					if (!found)
						addOrReplace(new Label("label", "Please select..."));
					
					super.onBeforeRender();
				}
				
			});
			add(fragment);
		} else {
			Fragment fragment = new Fragment("collapsed", "singleTabCollapsedFrag", this);
			fragment.add(new Label("label", tabs.iterator().next().getTitle()));
			add(fragment);
		}
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onBeforeRender() {
		boolean found = false;
		for (Tab tab: tabs) {
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

	public class TabsFragment extends Fragment {

		private final FloatingPanel dropdown;
		
		public TabsFragment(String id, FloatingPanel dropdown) {
			super(id, "tabsFrag", Tabbable.this);
			this.dropdown = dropdown;
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			add(new ListView<Tab>("tabs", tabs){

				@Override
				protected void populateItem(ListItem<Tab> item) {
					Tab tab = item.getModelObject();
					if (tab.isSelected())
						item.add(AttributeModifier.append("class", "active"));

					item.add(tab.render("tab"));
				}
				
			});
		}

		public Tabbable getTabbable() {
			return Tabbable.this;
		}

		public FloatingPanel getDropdown() {
			return dropdown;
		}
		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TabbableCssResourceReference()));
	}
	
}
