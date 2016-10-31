package com.gitplex.web.page.admin;

import java.io.File;
import java.io.IOException;

import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.resource.AbstractResource;

import com.gitplex.core.GitPlex;
import com.gitplex.core.manager.ConfigManager;
import com.gitplex.commons.bootstrap.BootstrapUtils;
import com.gitplex.commons.hibernate.PersistManager;
import com.gitplex.commons.util.FileUtils;
import com.gitplex.commons.wicket.editable.BeanContext;

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
