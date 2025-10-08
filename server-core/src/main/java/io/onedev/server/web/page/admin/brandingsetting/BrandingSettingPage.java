package io.onedev.server.web.page.admin.brandingsetting;

import static io.onedev.server.web.translation.Translation._T;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jspecify.annotations.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.io.Resources;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.img.ImageScope;
import io.onedev.server.web.page.admin.AdministrationPage;

public class BrandingSettingPage extends AdministrationPage {

	private static final String DATA_PREFIX = "data:image/png;base64,";
	
	public BrandingSettingPage(PageParameters params) {
		super(params);
	}
	
	private static File getCustomLogoFile(boolean darkMode) {
		if (darkMode)
			return new File(OneDev.getAssetsDir(), "logo-dark.png");
		else
			return new File(OneDev.getAssetsDir(), "logo.png");			
	}
	
	private String getLogoData(boolean darkMode) {
		var file = getCustomLogoFile(darkMode);
		if (file.exists()) {
			try {
				return DATA_PREFIX + Base64.encodeBase64String(FileUtils.readFileToByteArray(file));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return getDefaultLogoData(darkMode);
		}
	}

	private String getDefaultLogoData(boolean darkMode) {
		try {
			var fileName = darkMode?"logo-dark.png":"logo.png";
			URL url = Resources.getResource(ImageScope.class, fileName);
			return DATA_PREFIX + Base64.encodeBase64String(Resources.toByteArray(url));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private byte[] getLogoBytes(String logoData) {
		return Base64.decodeBase64(logoData.substring(DATA_PREFIX.length()));
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var setting = getSettingService().getBrandingSetting();
		var bean = new BrandSettingEditBean();
		bean.setName(setting.getName());
		bean.setLogoData(getLogoData(false));
		bean.setDarkLogoData(getLogoData(true));
		
		var form = new Form<Void>("settings") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				setting.setName(bean.getName());
				getSettingService().saveBrandingSetting(setting);
				auditService.audit(null, "changed branding settings", null, null);
				if (!bean.getLogoData().equals(getDefaultLogoData(false))) {
					var bytes = getLogoBytes(bean.getLogoData());
					getClusterService().runOnAllServers(new UpdateLogoTask(bytes, false));
				}
				if (!bean.getDarkLogoData().equals(getDefaultLogoData(true))) {
					var bytes = getLogoBytes(bean.getDarkLogoData());
					getClusterService().runOnAllServers(new UpdateLogoTask(bytes, true));
				}
				Session.get().success(_T("Branding settings updated"));
			}
		};
		add(form);
		form.add(BeanContext.edit("editor", bean));
		form.add(new Link<Void>("useDefault") {

			@Override
			public void onClick() {
				setting.setName("OneDev");
				getSettingService().saveBrandingSetting(setting);
				auditService.audit(null, "changed branding settings", null, null);
				getClusterService().runOnAllServers(new UpdateLogoTask(null, false));
				getClusterService().runOnAllServers(new UpdateLogoTask(null, true));
				setResponsePage(BrandingSettingPage.class);
				Session.get().success(_T("Default branding settings restored"));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!setting.getName().equals("OneDev") 
						|| getCustomLogoFile(false).exists() 
						|| getCustomLogoFile(true).exists());
			}
		});
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Branding"));
	}
	
	private SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}

	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}
	
	private static class UpdateLogoTask implements ClusterTask<Void> {

		private final byte[] logoBytes;
		
		private final boolean darkMode;
		
		UpdateLogoTask(@Nullable byte[] logoBytes, boolean darkMode) {
			this.logoBytes = logoBytes;
			this.darkMode = darkMode;
		}
		
		@Override
		public Void call() throws Exception {
			try {
				if (logoBytes != null)
					FileUtils.writeByteArrayToFile(getCustomLogoFile(darkMode), logoBytes);
				else 
					FileUtils.deleteFile(getCustomLogoFile(darkMode));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return null;
		}
		
	}
	
}