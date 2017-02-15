package com.gitplex.server.web.page.admin;

import java.io.File;
import java.io.IOException;

import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.resource.AbstractResource;

import com.gitplex.launcher.bootstrap.BootstrapUtils;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.persistence.PersistManager;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.web.editable.BeanContext;

@SuppressWarnings("serial")
public class DatabaseBackupPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BackupSettingHolder backupSettingHolder = new BackupSettingHolder();
		backupSettingHolder.setBackupSetting(GitPlex.getInstance(ConfigManager.class).getBackupSetting());
		Form<?> form = new Form<Void>("backupSetting") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				GitPlex.getInstance(ConfigManager.class).saveBackupSetting(backupSettingHolder.getBackupSetting());
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
						File tempDir = BootstrapUtils.createTempDir("backup");
						try {
							PersistManager persistManager = GitPlex.getInstance(PersistManager.class);
							persistManager.exportData(tempDir);
							BootstrapUtils.zip(tempDir, attributes.getResponse().getOutputStream());
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
