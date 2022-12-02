package io.onedev.server.web.page.admin.brandingsetting;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.BrandingSetting;
import io.onedev.server.web.component.brandlogo.BrandLogoPanel;
import io.onedev.server.web.component.fileupload.FileUploadField;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.ConfirmClickModifier;

@SuppressWarnings("serial")
public class BrandingSettingPage extends AdministrationPage {

	private List<FileUpload> uploads;
	
	public BrandingSettingPage(PageParameters params) {
		super(params);
	}
	
	private File getLogoFile() {
		return new File(Bootstrap.getSiteDir(), "assets/logo.png");
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BrandingSetting setting = OneDev.getInstance(SettingManager.class).getBrandingSetting();
		
		BeanEditor editor = BeanContext.edit("editor", setting);
		
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();

				if (uploads != null && !uploads.isEmpty()) {
					FileUpload upload = uploads.iterator().next();
					try {
						FileUtils.writeByteArrayToFile(getLogoFile(), upload.getBytes());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				OneDev.getInstance(SettingManager.class).saveBrandingSetting(setting);
				getSession().success("Branding setting has been saved");
			}
			
		};
		
		Form<?> form = new Form<Void>("brandingSetting");
		form.setMultiPart(true);
		form.add(editor);
		
		form.add(new BrandLogoPanel("logoPreview"));
		
		form.add(new FileUploadField("logoUpload", new IModel<List<FileUpload>>() {

			@Override
			public void detach() {
			}

			@Override
			public List<FileUpload> getObject() {
				return uploads;
			}

			@Override
			public void setObject(List<FileUpload> object) {
				uploads = object;
			}
			
		}) {

			@Override
			protected String getHint() {
				return "Select file...";
			}

			@Override
			protected String getIcon() {
				return "image";
			}
			
		});
		
		form.add(saveButton);
		
		form.add(new Link<Void>("useDefaultLogo") {

			@Override
			public void onClick() {
				getLogoFile().delete();
				getSession().success("Using default logo now");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLogoFile().exists());
			}
			
		}.add(new ConfirmClickModifier("Do you really want to use the default logo?")));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Branding");
	}

}