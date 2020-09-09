package io.onedev.server.web.page.admin.databasebackup;

import java.io.File;
import java.io.IOException;

import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.ZipUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.persistence.PersistManager;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class DatabaseBackupPage extends AdministrationPage {

	public DatabaseBackupPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BackupSettingHolder backupSettingHolder = new BackupSettingHolder();
		backupSettingHolder.setBackupSetting(OneDev.getInstance(SettingManager.class).getBackupSetting());
		Form<?> form = new Form<Void>("backupSetting") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).saveBackupSetting(backupSettingHolder.getBackupSetting());
				getSession().success("Backup setting has been updated");
				
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
							PersistManager persistManager = OneDev.getInstance(PersistManager.class);
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

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Database Backup");
	}

}
