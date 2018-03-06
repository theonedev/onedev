/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.onedev.server.web.page.error;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ConfigManager;
import io.onedev.server.model.User;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.util.DateUtils;
import io.onedev.utils.FileUtils;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public class UnexpectedExceptionPage extends BaseErrorPage {
	
	private static final long serialVersionUID = 1L;

	private Exception exception;
	
	public UnexpectedExceptionPage(Exception exception) {
		this.exception = exception;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String description = "Error Details: \n\n" + StringUtils.replace(Strings.toString(exception).trim(), "    ", "\t"); 
		add(new TextArea<String>("description", Model.of(description)));
		add(new WebMarkupContainer("contactName") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				User user = getLoginUser();
				if (user != null) 
					tag.put("value", user.getDisplayName());
			}
			
		});
		add(new WebMarkupContainer("contactEmail") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				User user = getLoginUser();
				if (user != null)
					tag.put("value", user.getEmail());
			}
			
		});
		
		List<Property> serverProps = new ArrayList<>();
		try {
			serverProps.add(new Property("Host Name", InetAddress.getLocalHost().getHostName()));
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		serverProps.add(new Property("System Date and Time", DateUtils.formatDateTime(new Date())));
		serverProps.add(new Property("Operating System", 
				System.getProperty("os.name") + " " 
				+ System.getProperty("os.version") + ", " 
				+ System.getProperty("os.arch")));
		serverProps.add(new Property("OS User", 
				System.getProperty("user.name")));
		serverProps.add(new Property("JVM", 
				System.getProperty("java.vm.name") + " " 
				+ System.getProperty("java.version") + ", " 
				+ System.getProperty("java.vm.vendor")));
		serverProps.add(new Property("OneDev Version", "EAP"));
		User user = getLoginUser();
		if (user != null) 
			serverProps.add(new Property("OneDev User", user.getName()));
		else 
			serverProps.add(new Property("OneDev User", "<anonymous>"));
		serverProps.add(new Property("Total Memory", 
				FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory())));
		serverProps.add(new Property("Used Memory", 
				FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory() - 
						Runtime.getRuntime().freeMemory())));
		add(new ListView<Property>("props", serverProps) {

			@Override
			protected void populateItem(ListItem<Property> item) {
				Property property = item.getModelObject();
				item.add(new Label("name", property.getName()));
				item.add(new Label("value", property.getValue()));
			}
			
		});
		add(new BookmarkablePageLink<Void>("homeTop", ProjectListPage.class));
		add(new BookmarkablePageLink<Void>("homeBottom", ProjectListPage.class));
		
		add(new WebMarkupContainer("backUrl") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("value", OneDev.getInstance(ConfigManager.class).getSystemSetting().getServerUrl());
			}
			
		});
		add(new WebMarkupContainer("additionalInfo") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				String serverInfo = "";
				for (Property property: serverProps) 
					serverInfo += property.getName() + ": " + property.getValue() + "\n";
				tag.put("value", serverInfo);
			}
			
		});
	}

	private static class Property implements Serializable {
		
		private final String name;
		
		private final String value;
		
		public Property(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
		
	}
	
}
