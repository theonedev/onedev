package io.onedev.server.web.page.admin.databasebackup;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.data.DataManager;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class DatabaseBackupPage extends AdministrationPage {

	public DatabaseBackupPage(PageParameters params) {
		super(params);
	}

	private ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("leadServer", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return getClusterManager().getLeaderServerAddress();
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getClusterManager().getServerAddresses().size() > 1);
			}
		});
		BackupSettingHolder backupSettingHolder = new BackupSettingHolder();
		backupSettingHolder.setBackupSetting(OneDev.getInstance(SettingManager.class).getBackupSetting());
		Form<?> form = new Form<Void>("backupSetting") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).saveBackupSetting(backupSettingHolder.getBackupSetting());
				getSession().success("Backup settings updated");
				
				setResponsePage(DatabaseBackupPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", backupSettingHolder));
		form.add(new ResourceLink<Void>("backupNow", new AbstractResource() {

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes) {
				ResourceResponse response = new ResourceResponse();
				response.setContentType(MimeTypes.OCTET_STREAM);
				response.disableCaching();
				response.setFileName("backup.zip");
				response.setWriteCallback(new WriteCallback() {

					@Override
					public void writeData(Attributes attributes) throws IOException {
						File tempDir = FileUtils.createTempDir("backup");
						try {
							DataManager databaseManager = OneDev.getInstance(DataManager.class);
							databaseManager.exportData(tempDir);
							FileUtils.zip(tempDir, attributes.getResponse().getOutputStream());
						} finally {
							FileUtils.deleteDir(tempDir);
						}
					}				
				});

				return response;

			}
			
		}));

		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Database Backup");
	}

}
