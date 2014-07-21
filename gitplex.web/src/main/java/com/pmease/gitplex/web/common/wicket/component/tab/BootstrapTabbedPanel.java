package com.pmease.gitplex.web.common.wicket.component.tab;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

import com.google.common.base.Strings;

@SuppressWarnings("serial")
public class BootstrapTabbedPanel<T extends ITab> extends Panel {

	private final List<T> tabs;
	private final Loop tabContent;
	private final IModel<Integer> tabCount;
	
	public BootstrapTabbedPanel(String id, List<T> tabs) {
		this(id, tabs, null);
	}
	
	public BootstrapTabbedPanel(String id, List<T> tabs, IModel<Integer> activeTab) {
		super(id, activeTab);
		
		this.tabs = Args.notNull(tabs, "tabs");
		tabCount = new AbstractReadOnlyModel<Integer>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Integer getObject() {
				return BootstrapTabbedPanel.this.tabs.size();
			}
		};

		add(newTabLinks("tab-links", tabCount));
		
		add(tabContent = newTabContent("tab-contents", tabCount));
	}
	
	protected Component newTabLinks(String id, IModel<Integer> tabCount) {
		return new Loop(id, tabCount) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				configureTabLinks(this);
			}
			
			@Override
			protected void populateItem(final LoopItem item) {
				final int index = item.getIndex();
				item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return isTabSelected(index) ? "active" : "";
					}
					
				}));
				
				item.add(newTabLink("tab-link", item.getIndex()));
			}
		};
	}
	
	protected void configureTabLinks(Component loop) {
		
	}
	
	protected Loop newTabContent(String id, IModel<Integer> tabCount) {
		return new Loop(id, tabCount) {

			@Override
			protected void populateItem(LoopItem item) {
				final int index = item.getIndex();
				item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return isTabSelected(index) ? "active" : "";
					}
					
				}));
				
				item.add(newTabContent("panel", item.getIndex()));
			}
		};
	}
	
	protected Component newTabLink(String id, final int index) {
		T tab = tabs.get(index);
		Fragment frag = new Fragment(id, "linkfrag", BootstrapTabbedPanel.this);
		WebMarkupContainer link = new WebMarkupContainer("link");
		frag.add(link);
		link.add(new Label("title", tab.getTitle()));
		link.add(AttributeModifier.replace("href", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "#" + tabContent.get(index).getMarkupId(true);
			}
			
		}));
		
		return frag;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		String onShownScript = getOnShownScript();
		if (!Strings.isNullOrEmpty(onShownScript)) {
			StringBuffer sb = new StringBuffer();
			sb.append("$('#" + getMarkupId(true) + " a[data-toggle=\"tab\"]')")
				.append(".on('shown.bs.tab', ")
				.append(onShownScript)
				.append(")");
			
			response.render(OnDomReadyHeaderItem.forScript(sb.toString()));
		}
		
	}
	
	protected String getOnShownScript() {
		return null;
	}
	
	protected final boolean isTabSelected(int index) {
		if (getActiveTab() == null) {
			return index == 0;
		} else {
			return index == getActiveTab();
		}
	}
	
	protected Component newTabContent(String id, int index) {
		T tab = tabs.get(index);
		return tab.getPanel(id);
	}
	
	public Integer getActiveTab() {
		return (Integer) getDefaultModelObject();
	}
}
