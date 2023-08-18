package io.onedev.server.ee.clustering;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

import static org.unbescape.html.HtmlEscape.escapeHtml5;

@SuppressWarnings("serial")
public class ClusterManagementPage extends AdministrationPage {

	public ClusterManagementPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<String>("servers", new LoadableDetachableModel<>() {
			@Override
			protected List<String> load() {
				return getClusterManager().getServerAddresses();
			}

		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				var address = item.getModelObject();
				var escaped = escapeHtml5(address + " (" + getClusterManager().getServerName(address) + ")");
				if (item.getIndex() == 0) 
					escaped += " <span class='badge badge-info badge-sm ml-1'>lead</span>";
				item.add(new Label("server", escaped).setEscapeModelStrings(false));
			}
			
		});
		
		var clusterSetting = getSettingManager().getClusterSetting();
		Form<?> form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSettingManager().saveClusterSetting(clusterSetting);
				getProjectManager().redistributeReplicas();
				Session.get().success("Settings saved and project redistribution scheduled");
			}
			
		};
		form.add(BeanContext.edit("editor", clusterSetting));
		add(form);
	}

	private ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
	}
	
	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "High Availability & Scalability");
	}

}
