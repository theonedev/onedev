package com.pmease.gitop.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.bean.validation.BeanValidationConfiguration;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.devutils.stateless.StatelessChecker;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.version.LastModifiedResourceVersion;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.eclipse.jgit.storage.file.WindowCacheConfig;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.gitop.web.assets.AssetLocator;
import com.pmease.gitop.web.common.quantity.Data;
import com.pmease.gitop.web.page.error.BaseErrorPage;
import com.pmease.gitop.web.page.error.PageExpiredPage;
import com.pmease.gitop.web.page.home.HomePage;
import com.pmease.gitop.web.shiro.LoginPage;
import com.pmease.gitop.web.shiro.LogoutPage;
import com.pmease.gitop.web.shiro.ShiroWicketPlugin;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;

@Singleton
public class GitopWebApp extends AbstractWicketConfig {
	private static final Duration DEFAULT_TIMEOUT = Duration.minutes(10);
	
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
	public WebRequest newWebRequest(HttpServletRequest servletRequest, String filterPath) {
		return new ServletWebRequest(servletRequest, filterPath) {

			@Override
			public boolean shouldPreserveClientUrl() {
				if (RequestCycle.get().getActiveRequestHandler() instanceof RenderPageRequestHandler) {
					RenderPageRequestHandler requestHandler = 
							(RenderPageRequestHandler) RequestCycle.get().getActiveRequestHandler();
					
					/*
					 *  Add this to make sure that the page url does not change upon errors, so that 
					 *  user can know which page is actually causing the error. This behavior is common
					 *  for main stream applications.   
					 */
					if (requestHandler.getPage() instanceof BaseErrorPage)
						return true;
				}
				return super.shouldPreserveClientUrl();
			}
			
		};
	}

	@Override
	protected void init() {
		this.startupDate = new Date();

		super.init();

		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		getRequestCycleSettings().setTimeout(DEFAULT_TIMEOUT);
		
		getResourceSettings().setCachingStrategy(new FilenameWithVersionResourceCachingStrategy(new LastModifiedResourceVersion()));

		getRequestCycleListeners().add(new WicketRequestCycleListener());
		
		getApplicationSettings().setPageExpiredErrorPage(PageExpiredPage.class);
		
		// wicket bean validation
		new BeanValidationConfiguration().configure(this);

		loadDefaultUserAvatarData();

		new ShiroWicketPlugin()
				.mountLoginPage("login", LoginPage.class)
				.mountLogoutPage("logout", LogoutPage.class)
				.install(this);
		
		Bootstrap.install(this, new BootstrapSettings());

		configureResources();
		
		// mount all pages and resources
		mount(new GitopMappings(this));
		
		if (usesDevelopmentConfig()) {
			getComponentPreOnBeforeRenderListeners().add(new StatelessChecker());
		}
		
		initGitConfig();
	}

	private void initGitConfig() {
		WindowCacheConfig cfg = new WindowCacheConfig();
        cfg.setStreamFileThreshold((int) Data.ONE_MB * 10);
        cfg.install();
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

	/*
	private void mountPages() {
		mountPage("init", ServerInitPage.class);
		mountPage("register", RegisterPage.class);
		
		// error pages
		mountPage("404", PageNotFoundPage.class);
		mountPage("403", AccessDeniedPage.class);
		mountPage("501", InternalErrorPage.class);
		
		// account related pages
		// --------------------------------------------------------
		
		// project dashboard
		mount(new MountedMapper("${user}/${repo}", RepositoryHomePage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> normalizedSegments = UrlUtils.normalizeUrlSegments(url.getSegments());
				if (normalizedSegments.size() < 2)
					return false;
				String userName = normalizedSegments.get(0);
				if (UserNameValidator.getReservedNames().contains(userName))
					return false;
				
				String projectName = normalizedSegments.get(1);
				return !ProjectNameValidator.getReservedNames().contains(projectName);
			}

		});
		
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/tree/${objectId}", SourceTreePage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/blob/${objectId}", SourceBlobPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/blame/#{objectId}", BlobBlamePage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/commit/${objectId}", SourceCommitPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/commits/#{objectId}", CommitsPage.class));
		
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/branches", BranchesPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/tags", TagsPage.class));
		
//		mount(new PageParameterAwareMountedMapper("${user}/${project}/wiki", ProjectWikiPage.class));
		
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/pull-requests/open", OpenRequestsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/pull-requests/closed", ClosedRequestsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/pull-requests/new", NewRequestPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/pull-requests", RequestDetailPage.class));
		
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/contributors", ContributorsPage.class));
//		mount(new PageParameterAwareMountedMapper("${user}/${project}/graphs", ProjectGraphsPage.class));
//		mount(new PageParameterAwareMountedMapper("${user}/${project}/forks", ProjectForksPage.class));

		mount(new PageParameterAwareMountedMapper("${user}/${repo}/settings", RepositoryOptionsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/settings/gate-keepers", GateKeeperSettingPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/settings/hooks", RepositoryHooksPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/settings/pull-requests", PullRequestSettingsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/settings/audits", RepositoryAuditLogPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${repo}/settings/permissions", RepositoryPermissionsPage.class));
		
		// account dashboard
		mount(new MountedMapper("${user}", AccountHomePage.class)
		{

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> normalizedSegments = UrlUtils.normalizeUrlSegments(url.getSegments());
				if (normalizedSegments.size() < 1)
					return false;
				String userName = normalizedSegments.get(0);
				return !UserNameValidator.getReservedNames().contains(userName);
			}

		});
		
		// account settings
		mountPage("${user}/settings", AccountProfilePage.class);
		mountPage("${user}/settings/password", AccountPasswordPage.class);
		mountPage("${user}/settings/projects", RepositoriesPage.class);
		mountPage("${user}/settings/members", AccountMembersSettingPage.class);
		mountPage("${user}/settings/teams", AccountTeamsPage.class);
		mountPage("${user}/settings/teams/new", AddTeamPage.class);
		mountPage("${user}/settings/teams/${teamId}", EditTeamPage.class);
		
		mountPage("new", CreateRepositoryPage.class);
		
		// system administration related
		mountPage("administration", AdministrationOverviewPage.class);
		mountPage("administration/users", UserAdministrationPage.class);
		mountPage("administration/mail-settings", MailSettingEdit.class);
		mountPage("administration/system-settings", SystemSettingEdit.class);
		mountPage("administration/support", SupportPage.class);
		mountPage("administration/licensing", LicensingPage.class);
	}
	
	private void mountResources() {
//		getSharedResources().add(AvatarImageResourceReference.AVATAR_RESOURCE, new AvatarImageResource());
//		getSharedResources().add(ImageBlobResourceReference.IMAGE_BLOB_RESOURCE, new ImageBlobResource());
//		mountResource("imageblob/${user}/${project}/${objectId}", new ImageBlobResourceReference());
//		getSharedResources().add(RawBlobResourceReference.RAW_BLOB_RESOURCE, new RawBlobResource());
		
		mountResource("avatar/${type}/${id}", new AvatarImageResourceReference());
		mountResource("raw/${user}/${repo}/${objectId}", new RawBlobResourceReference());
		mountResource("archive/${user}/${repo}/${file}", new GitArchiveResourceReference());
	}
	*/
	

	private void configureResources() {
		final IPackageResourceGuard packageResourceGuard = getResourceSettings().getPackageResourceGuard();

        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
            guard.addPattern("+*.woff");
            guard.addPattern("+*.eot");
            guard.addPattern("+*.svg");
            guard.addPattern("+*.ttf");
        }
        
//        mountResources();
	}
	
	public boolean isGravatarEnabled() {
		return true;
	}

	public boolean isPublicSignupEnabled() {
		return true;
	}

	public Iterable<IRequestMapper> getRequestMappers() {
		return getRootRequestMapperAsCompound();
	}
}
