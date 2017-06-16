package com.gitplex.server.web.page.admin.serverinformation;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.util.FileUtils;
import com.gitplex.server.web.page.admin.AdministrationPage;
import com.gitplex.server.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.progress.ProgressBar;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.progress.ProgressBar.Type;

@SuppressWarnings("serial")
public class ServerInformationPage extends AdministrationPage {
	
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
				return FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory());
			}
			
		}));
		add(new Label("usedMemory", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return FileUtils.byteCountToDisplaySize(
						Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
			}
			
		}));
		add(new ProgressBar("memoryUsage", new LoadableDetachableModel<Integer>() {

			@Override
			protected Integer load() {
				return (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())*1.0 
						/ Runtime.getRuntime().maxMemory() * 100);
			}
			
		}, Type.SUCCESS, true));
		
		add(new Label("osUserName", System.getProperty("user.name")));
		
		add(new Link<Void>("gc") {

			@Override
			public void onClick() {
				System.gc();
			}
			
		});
	}
}
