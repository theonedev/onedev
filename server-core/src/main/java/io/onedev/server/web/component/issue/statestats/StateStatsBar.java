package io.onedev.server.web.component.issue.statestats;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.issue.StateSpec;

@SuppressWarnings("serial")
public abstract class StateStatsBar extends GenericPanel<Map<String, Integer>> {

	public StateStatsBar(String id, IModel<Map<String, Integer>> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		int totalCount = getModelObject().values().stream().collect(Collectors.summingInt(it->it));
		if (totalCount != 0) {
			RepeatingView statesView = new RepeatingView("states");
			for (StateSpec state: OneDev.getInstance(SettingManager.class).getIssueSetting().getStateSpecs()) {
				Integer count = getModelObject().get(state.getName());
				if (count != null) { 
					Link<Void> link = newStateLink(statesView.newChildId(), state.getName());
					link.add(AttributeAppender.append("title", count + " " + state.getName().toLowerCase() + " issues"));
					link.add(AttributeAppender.append("data-percent", count*1.0/totalCount));
					link.add(AttributeAppender.append("style", "background-color: " + state.getColor()));
					statesView.add(link);
				}
			}
			add(statesView);
		} else {
			add(new Label("states", "&nbsp;") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			}.setEscapeModelStrings(false));
			add(AttributeAppender.append("title", "No issues in milestone"));
		}
		
		setOutputMarkupId(true);
	}
	
	protected abstract Link<Void> newStateLink(String componentId, String state);

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new StateStatsResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("onedev.server.stateStats.onDomReady('%s');", getMarkupId())));
	}

}
