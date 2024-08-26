package io.onedev.server.web.page.admin.serverinformation;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.page.admin.ServerDetailPage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
public class ServerInformationPage extends ServerDetailPage {
	
	private final IModel<ServerInformation> serverInformationModel = new LoadableDetachableModel<>() {
		@Override
		protected ServerInformation load() {
			return getServerInformation(server);
		}

	};
	
	public ServerInformationPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ListView<Map.Entry<String, String>>("properties", new LoadableDetachableModel<>() {

			@Override
			protected List<Map.Entry<String, String>> load() {
				return new ArrayList<>(serverInformationModel.getObject().properties.entrySet());
			}
		}) {

			@Override
			protected void populateItem(ListItem<Map.Entry<String, String>> item) {
				item.add(new Label("name", item.getModelObject().getKey()));
				item.add(new Label("value", item.getModelObject().getValue()));
			}
		});
		
		add(new Label("memoryUsage", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				return serverInformationModel.getObject().memoryUsage;
			}
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("style", "width: " + serverInformationModel.getObject().memoryUsage);
			}
			
		});
		
		add(new Link<Void>("gc") {

			@Override
			public void onClick() {
				if (server != null) {
					getClusterManager().runOnServer(server, () -> {
						System.gc();
						return null;
					});
				} else {
					System.gc();
				}
			}
			
		});
	}

	@Override
	protected void onDetach() {
		serverInformationModel.detach();
		super.onDetach();
	}

	private static ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
	}

	private static ServerInformation getServerInformation() {
		var serverInformation = new ServerInformation();
		serverInformation.properties.put("System Date", DateUtils.formatDateTime(new Date()));
		serverInformation.properties.put("OS", System.getProperty("os.name") + " " + System.getProperty("os.version") + ", " + System.getProperty("os.arch"));
		serverInformation.properties.put("OS User Name", System.getProperty("user.name"));
		serverInformation.properties.put("JVM", System.getProperty("java.vm.name") + " " + System.getProperty("java.version") + ", " + System.getProperty("java.vm.vendor"));
		serverInformation.properties.put("Total Heap Memory", String.valueOf(Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
		serverInformation.properties.put("Used Heap Memory", String.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + " MB");
		serverInformation.memoryUsage = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())*1.0
				/ Runtime.getRuntime().maxMemory() * 100) + "%";
		return serverInformation;
	}

	private static ServerInformation getServerInformation(@Nullable String server) {
		ServerInformation serverInformation;
		if (server != null)
			serverInformation = getClusterManager().runOnServer(server, () -> getServerInformation());
		else
			serverInformation = getServerInformation();
		return serverInformation;
	}

	@Override
	protected String newTopbarTitle() {
		return "Server Information";
	}

	private static class ServerInformation implements Serializable {
		Map<String, String> properties = new LinkedHashMap<>();
		
		String memoryUsage;
		
	}
}
