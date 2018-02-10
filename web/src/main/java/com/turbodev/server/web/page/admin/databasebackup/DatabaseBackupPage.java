package com.turbodev.server.web.page.admin.databasebackup;

import java.io.File;
import java.io.IOException;

import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.resource.AbstractResource;

import com.turbodev.utils.FileUtils;
import com.turbodev.utils.ZipUtils;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.manager.StorageManager;
import com.turbodev.server.persistence.PersistManager;
import com.turbodev.server.web.editable.BeanContext;
import com.turbodev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class DatabaseBackupPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("storageDir", TurboDev.getInstance(StorageManager.class).getStorageDir()));
		
		BackupSettingHolder backupSettingHolder = new BackupSettingHolder();
		backupSettingHolder.setBackupSetting(TurboDev.getInstance(ConfigManager.class).getBackupSetting());
		Form<?> form = new Form<Void>("backupSetting") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				TurboDev.getInstance(ConfigManager.class).saveBackupSetting(backupSettingHolder.getBackupSetting());
				getSession().success("Backup setting has been updated");
				
				setResponsePage(DatabaseBackupPage.class);
			}
			
		};
		form.add(BeanContext.editBean("editor", backupSettingHolder));
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
							PersistManager persistManager = TurboDev.getInstance(PersistManager.class);
							persistManager.exportData(tempDir);
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

}
