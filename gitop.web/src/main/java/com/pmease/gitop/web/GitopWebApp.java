package com.pmease.gitop.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.bean.validation.BeanValidationConfiguration;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.assets.AssetLocator;
import com.pmease.gitop.web.common.component.avatar.AvatarImageResource;
import com.pmease.gitop.web.common.component.avatar.AvatarImageResourceReference;
import com.pmease.gitop.web.common.component.fileupload.FileManagerResourceReference;
import com.pmease.gitop.web.common.component.fileupload.FileUploadResourceReference;
import com.pmease.gitop.web.page.account.AccountHomePage;
import com.pmease.gitop.web.page.account.RegisterPage;
import com.pmease.gitop.web.page.account.setting.password.AccountPasswordPage;
import com.pmease.gitop.web.page.account.setting.permission.AccountPermissionPage;
import com.pmease.gitop.web.page.account.setting.profile.AccountProfilePage;
import com.pmease.gitop.web.page.account.setting.repos.AccountReposPage;
import com.pmease.gitop.web.page.home.HomePage;
import com.pmease.gitop.web.page.init.ServerInitPage;
import com.pmease.gitop.web.shiro.LoginPage;
import com.pmease.gitop.web.shiro.LogoutPage;
import com.pmease.gitop.web.shiro.ShiroWicketPlugin;

@Singleton
public class GitopWebApp extends AbstractWicketConfig {
	private Date startupDate;
	private byte[] defaultUserAvatar;

	public static GitopWebApp get() {
		return (GitopWebApp) Application.get();
	}

	public Date getStartupDate() {
		return startupDate;
	}

	public Duration getUptime() {
		Date start = getStartupDate();
		if (start == null) {
			return Duration.milliseconds(0);
		}

		return Duration.elapsed(Time.valueOf(start));
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

	@Override
	public Session newSession(Request request, Response response) {
		return new GitopSession(request);
	}

	@Override
	protected void init() {
		this.startupDate = new Date();

		super.init();

		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		// wicket bean validation
		new BeanValidationConfiguration().configure(this);

		loadDefaultUserAvatarData();
		
		new ShiroWicketPlugin().mountLoginPage("login", LoginPage.class)
				.mountLogoutPage("logout", LogoutPage.class).install(this);

		mountPages();
		
		mountResources();
	}

	public byte[] getDefaultUserAvatar() {
		return defaultUserAvatar;
	}
	
	private void loadDefaultUserAvatarData() {
		InputStream in = null;
		try {
			in = AssetLocator.class.getResourceAsStream("img/empty-avatar.jpg");
			defaultUserAvatar = ByteStreams.toByteArray(in);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private void mountPages() {
		mountPage("init", ServerInitPage.class);
		mountPage("register", RegisterPage.class);
		
		// account related pages
		// --------------------------------------------------------
		
		// account dashboard
		mountPage("account/${user}", AccountHomePage.class);
		
		// account settings
		mountPage("settings/profile", AccountProfilePage.class);
		mountPage("settings/password", AccountPasswordPage.class);
		mountPage("settings/permission", AccountPermissionPage.class);
		mountPage("settings/repos", AccountReposPage.class);
		
		// repository pages
		// --------------------------------------------------------
		
	}

	private void mountResources() {
		getSharedResources().add(AvatarImageResourceReference.AVATAR_RESOURCE, new AvatarImageResource());
		mountResource("avatars/${type}/${id}", new AvatarImageResourceReference());
		
		mountResource("fileManager", new FileManagerResourceReference(getUploadsDir().getAbsolutePath()));
		mountResource("fileUpload", new FileUploadResourceReference(getUploadsDir().getAbsolutePath()));
	}
	
	public boolean isGravatarEnabled() {
		return true;
	}

	public boolean isPublicSignupEnabled() {
		return true;
	}

	public File getDataDir() {
		String str = AppLoader.getInstance(ConfigManager.class).getStorageSetting().getRepoStorageDir();
		return new File(str);
	}

	public File getSystemAvatarDir() {
		return new File(getDataDir(), "avatars");
	}

	public File getUserAvatarDir(Long id) {
		Preconditions.checkNotNull(id, "user id");
		return new File(getSystemAvatarDir(), "users/" + id.toString());
	}

	public File getUserAvatarDir(User user) {
		return getUserAvatarDir(user.getId());
	}

	public File getUploadsDir() {
		return new File(getDataDir(), "uploads");
	}

}
