package io.onedev.server.web.page.admin.buildsetting.agent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.web.component.AgentStatusBadge;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.util.ConfirmClickModifier;

@SuppressWarnings("serial")
public class AgentOverviewPage extends AgentDetailPage {

	public AgentOverviewPage(PageParameters params) {
		super(params);
	}

	private AgentManager getAgentManager() {
		return OneDev.getInstance(AgentManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("restart") {

			@Override
			public void onClick() {
				getAgentManager().restart(getAgent());
				setResponsePage(AgentOverviewPage.class, AgentOverviewPage.paramsOf(getAgent()));
				Session.get().success("Restart command issued");
			}
			
		}.add(new ConfirmClickModifier("Do you really want to restart this agent?")));
		
		add(new Link<Void>("remove") {

			@Override
			public void onClick() {
				getAgentManager().delete(getAgent());
				setResponsePage(AgentListPage.class);
				Session.get().success("Agent removed");
			}

		}.add(new ConfirmClickModifier("Do you really want to remove this agent?")));
		
		add(new Link<Void>("pauseOrResume") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return getAgent().isPaused()?"Resume":"Pause";
					}
					
				}));
			}

			@Override
			public void onClick() {
				if (getAgent().isPaused())
					getAgentManager().resume(getAgent());
				else
					getAgentManager().pause(getAgent());
				setResponsePage(AgentOverviewPage.class, AgentOverviewPage.paramsOf(getAgent()));
			}
			
		});
		
		add(new AgentIcon("icon", agentModel));
		
		add(new Label("name", getAgent().getName()));
		add(new Label("ipAddress", getAgent().getIpAddress()));
		add(new Label("osVersion", getAgent().getOsVersion()));
		add(new Label("osArch", getAgent().getOsArch()));
		add(new Label("cpu", getAgent().getCpu()));
		add(new Label("memory", getAgent().getMemory()));
		add(new Label("temporal", getAgent().isTemporal()));
		add(new AgentStatusBadge("status", agentModel));

		if (getAgent().isOnline()) {
			Fragment fragment = new Fragment("attributes", "onlineAttributesFrag", this);
			AgentEditBean bean = new AgentEditBean();
			bean.getAttributes().addAll(getAgent().getAttributes());
			bean.getAttributes().sort(Comparator.comparing(AgentAttribute::getName));
			
			Form<?> form = new Form<Void>("form") {

				@Override
				protected void onSubmit() {
					super.onSubmit();
					Map<String, String> attributeMap = new HashMap<>();
					for (AgentAttribute attribute: bean.getAttributes())
						attributeMap.put(attribute.getName(), attribute.getValue());
					OneDev.getInstance(AgentAttributeManager.class).syncAttributes(getAgent(), attributeMap);
					getAgentManager().attributesUpdated(getAgent());
					Session.get().success("Attributes saved");
				}
				
			};
			form.add(BeanContext.edit("attributes", bean));
			fragment.add(form);
			add(fragment);
		} else if (!getAgent().getAttributes().isEmpty()) {
			Fragment fragment = new Fragment("attributes", "offlineHasAttributesFrag", this);
			fragment.add(new ListView<AgentAttribute>("attributes", new LoadableDetachableModel<List<AgentAttribute>>() {

				@Override
				protected List<AgentAttribute> load() {
					List<AgentAttribute> attributes = new ArrayList<>(getAgent().getAttributes());
					attributes.sort(Comparator.comparing(AgentAttribute::getName));
					return attributes;
				}
				
			}) {

				@Override
				protected void populateItem(ListItem<AgentAttribute> item) {
					item.add(new Label("name", item.getModelObject().getName()));
					item.add(new Label("value", item.getModelObject().getValue()));
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getModelObject().isEmpty());
				}
				
			});
			add(fragment);
		} else {
			add(new Fragment("attributes", "offlineNoAttributesFrag", this));
		}
	}

}
