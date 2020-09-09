package io.onedev.server.web.page.admin.serverinformation;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.util.DateUtils;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class ServerInformationPage extends AdministrationPage {
	
	public ServerInformationPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("systemDateTime", DateUtils.formatDateTime(new Date())));
		add(new Label("os", System.getProperty("os.name") + " " 
				+ System.getProperty("os.version") + ", " 
				+ System.getProperty("os.arch")));
		add(new Label("jvm", System.getProperty("java.vm.name") + " " 
				+ System.getProperty("java.version") + ", " 
				+ System.getProperty("java.vm.vendor")));
		add(new Label("totalMemory", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return String.valueOf(Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB";
			}
			
		}));
		add(new Label("usedMemory", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return String.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + " MB";
			}
			
		}));
		
		add(new Label("memoryUsage", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())*1.0 
						/ Runtime.getRuntime().maxMemory() * 100) + "%";
			}
			
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("style", "width: " + getDefaultModelObject());
			}
			
		});
		
		add(new Label("osUserName", System.getProperty("user.name")));
		
		add(new Link<Void>("gc") {

			@Override
			public void onClick() {
				System.gc();
			}
			
		});
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Server Information");
	}
	
}
