package io.onedev.server.web.page.admin.databasebackup;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.ZipUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.SettingService;
import io.onedev.server.data.DataService;
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

import static io.onedev.server.web.translation.Translation._T;

import java.io.File;
import java.io.IOException;

public class DatabaseBackupPage extends AdministrationPage {

	public DatabaseBackupPage(PageParameters params) {
		super(params);
	}

	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("leadServer", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return getClusterService().getLeaderServerAddress();
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getClusterService().getServerAddresses().size() > 1);
			}
		});
		BackupSettingHolder backupSettingHolder = new BackupSettingHolder();
		backupSettingHolder.setBackupSetting(OneDev.getInstance(SettingService.class).getBackupSetting());
		Form<?> form = new Form<Void>("backupSetting") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingService.class).saveBackupSetting(backupSettingHolder.getBackupSetting());
				getSession().success(_T("Backup settings updated"));
				
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
							DataService databaseManager = OneDev.getInstance(DataService.class);
							databaseManager.exportData(tempDir);
							ZipUtils.zip(tempDir, attributes.getResponse().getOutputStream());
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
		return new Label(componentId, _T("Database Backup"));
	}

}
