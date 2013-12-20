package com.pmease.gitop.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.bean.validation.BeanValidationConfiguration;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.devutils.stateless.StatelessChecker;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.version.LastModifiedResourceVersion;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.pmease.commons.util.ExceptionUtils;
import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.validation.ProjectNameValidator;
import com.pmease.gitop.model.validation.UserNameValidator;
import com.pmease.gitop.web.assets.AssetLocator;
import com.pmease.gitop.web.common.wicket.mapper.PageParameterAwareMountedMapper;
import com.pmease.gitop.web.component.avatar.AvatarImageResource;
import com.pmease.gitop.web.component.avatar.AvatarImageResourceReference;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.page.account.RegisterPage;
import com.pmease.gitop.web.page.account.home.AccountHomePage;
import com.pmease.gitop.web.page.account.setting.members.AccountMembersSettingPage;
import com.pmease.gitop.web.page.account.setting.password.AccountPasswordPage;
import com.pmease.gitop.web.page.account.setting.profile.AccountProfilePage;
import com.pmease.gitop.web.page.account.setting.projects.AccountProjectsPage;
import com.pmease.gitop.web.page.account.setting.teams.AccountTeamsPage;
import com.pmease.gitop.web.page.account.setting.teams.AddTeamPage;
import com.pmease.gitop.web.page.account.setting.teams.EditTeamPage;
import com.pmease.gitop.web.page.admin.MailSettingEdit;
import com.pmease.gitop.web.page.admin.SystemSettingEdit;
import com.pmease.gitop.web.page.error.AccessDeniedPage;
import com.pmease.gitop.web.page.error.ErrorPage;
import com.pmease.gitop.web.page.error.InternalErrorPage;
import com.pmease.gitop.web.page.error.PageExpiredPage;
import com.pmease.gitop.web.page.error.PageNotFoundPage;
import com.pmease.gitop.web.page.home.HomePage;
import com.pmease.gitop.web.page.init.ServerInitPage;
import com.pmease.gitop.web.page.project.issue.ProjectPullRequestsPage;
import com.pmease.gitop.web.page.project.settings.CreateProjectPage;
import com.pmease.gitop.web.page.project.settings.GateKeeperSettingPage;
import com.pmease.gitop.web.page.project.settings.ProjectAuditLogPage;
import com.pmease.gitop.web.page.project.settings.ProjectHooksPage;
import com.pmease.gitop.web.page.project.settings.ProjectOptionsPage;
import com.pmease.gitop.web.page.project.settings.ProjectPermissionsPage;
import com.pmease.gitop.web.page.project.settings.PullRequestSettingsPage;
import com.pmease.gitop.web.page.project.source.ProjectHomePage;
import com.pmease.gitop.web.page.project.source.blob.SourceBlobPage;
import com.pmease.gitop.web.page.project.source.blob.renderer.ImageBlobResourceReference;
import com.pmease.gitop.web.page.project.source.blob.renderer.RawBlobResourceReference;
import com.pmease.gitop.web.page.project.source.branches.BranchesPage;
import com.pmease.gitop.web.page.project.source.commit.SourceCommitPage;
import com.pmease.gitop.web.page.project.source.commits.CommitsPage;
import com.pmease.gitop.web.page.project.source.contributors.ContributorsPage;
import com.pmease.gitop.web.page.project.source.tags.TagsPage;
import com.pmease.gitop.web.page.project.source.tree.SourceTreePage;
import com.pmease.gitop.web.page.project.stats.ProjectForksPage;
import com.pmease.gitop.web.page.project.stats.ProjectGraphsPage;
import com.pmease.gitop.web.page.project.wiki.ProjectWikiPage;
import com.pmease.gitop.web.page.test.ProjectPage;
import com.pmease.gitop.web.page.test.PullRequestsPage;
import com.pmease.gitop.web.page.test.TestPage;
import com.pmease.gitop.web.shiro.LoginPage;
import com.pmease.gitop.web.shiro.LogoutPage;
import com.pmease.gitop.web.shiro.ShiroWicketPlugin;

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
					if (requestHandler.getPage() instanceof ErrorPage)
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

		getRequestCycleListeners().add(new AbstractRequestCycleListener() {
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception e) {
				if (ExceptionUtils.find(e, EntityNotFoundException.class) != null) {
					return new RenderPageRequestHandler(new PageProvider(PageNotFoundPage.class));
				} else if (ExceptionUtils.find(e, AccessDeniedException.class) != null) {
					if (Gitop.getInstance(UserManager.class).getCurrent() == null) {
						return new RenderPageRequestHandler(new PageProvider(LoginPage.class));
					} else {
						return new RenderPageRequestHandler(new PageProvider(AccessDeniedPage.class));
					}
				} else {
					return null;
				}
			}
		});
		
		getApplicationSettings().setPageExpiredErrorPage(PageExpiredPage.class);
		
		// wicket bean validation
		new BeanValidationConfiguration().configure(this);

		loadDefaultUserAvatarData();
		
		new ShiroWicketPlugin()
				.mountLoginPage("login", LoginPage.class)
				.mountLogoutPage("logout", LogoutPage.class)
				.install(this);

		mountPages();
		configureResources();
		
		if (getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT) {
			getComponentPreOnBeforeRenderListeners().add(new StatelessChecker());
		}
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
		
		// error pages
		mountPage("404", PageNotFoundPage.class);
		mountPage("403", AccessDeniedPage.class);
		mountPage("501", InternalErrorPage.class);
		
		// account related pages
		// --------------------------------------------------------
		
		// project dashboard
		mount(new MountedMapper("/${user}/${project}", ProjectHomePage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> normalizedSegments = normalizeUrlSegments(url.getSegments());
				if (normalizedSegments.size() < 2)
					return false;
				String userName = normalizedSegments.get(0);
				if (UserNameValidator.getReservedNames().contains(userName))
					return false;
				
				String projectName = normalizedSegments.get(1);
				return !ProjectNameValidator.getReservedNames().contains(projectName);
			}

		});
		
		mount(new PageParameterAwareMountedMapper("${user}/${project}/tree/${objectId}", SourceTreePage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/blob/${objectId}", SourceBlobPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/commit/${objectId}", SourceCommitPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/commits", CommitsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/branches", BranchesPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/tags", TagsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/contributors", ContributorsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/wiki", ProjectWikiPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/merges", ProjectPullRequestsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/graphs", ProjectGraphsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/forks", ProjectForksPage.class));

		mount(new PageParameterAwareMountedMapper("${user}/${project}/settings", ProjectOptionsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/settings/gate-keepers", GateKeeperSettingPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/settings/hooks", ProjectHooksPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/settings/pull-requests", PullRequestSettingsPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/settings/audits", ProjectAuditLogPage.class));
		mount(new PageParameterAwareMountedMapper("${user}/${project}/settings/permissions", ProjectPermissionsPage.class));
		
		// account dashboard
		mount(new MountedMapper("/${user}", AccountHomePage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> normalizedSegments = normalizeUrlSegments(url.getSegments());
				if (normalizedSegments.size() < 1)
					return false;
				String userName = normalizedSegments.get(0);
				return !UserNameValidator.getReservedNames().contains(userName);
			}

		});
		
		// account settings
		mountPage("settings/profile", AccountProfilePage.class);
		mountPage("settings/password", AccountPasswordPage.class);
		mountPage("settings/projects", AccountProjectsPage.class);
		mountPage("settings/members", AccountMembersSettingPage.class);
		mountPage("settings/teams", AccountTeamsPage.class);
		mountPage("settings/teams/new", AddTeamPage.class);
		mountPage("settings/teams/edit/${teamId}", EditTeamPage.class);
		
		// project related
		mountPage("new", CreateProjectPage.class);
		
		// administration related
		mountPage("administration/system", SystemSettingEdit.class);
		mountPage("administration/mail", MailSettingEdit.class);

		mountPage("/test", TestPage.class);
		mountPage("/test/project", ProjectPage.class);
		mountPage("/test/merge_requests", PullRequestsPage.class);
		
		// repository pages
		// --------------------------------------------------------
	}
	
	private List<String> normalizeUrlSegments(List<String> segments) {
		List<String> normalized = new ArrayList<String>();
		for (String each: segments) {
			each = StringUtils.remove(each, '/');
			if (each.length() != 0)
				normalized.add(each);
		}
		return normalized;
	}

	private void configureResources() {
		final IPackageResourceGuard packageResourceGuard = getResourceSettings().getPackageResourceGuard();

        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
            guard.addPattern("+*.woff");
            guard.addPattern("+*.eot");
            guard.addPattern("+*.svg");
            guard.addPattern("+*.ttf");
        }
        
        mountResources();
	}
	
	private void mountResources() {
		getSharedResources().add(AvatarImageResourceReference.AVATAR_RESOURCE, new AvatarImageResource());
		mountResource("avatars/${type}/${id}", new AvatarImageResourceReference());
		
//		getSharedResources().add(ImageBlobResourceReference.IMAGE_BLOB_RESOURCE, new ImageBlobResource());
		mountResource("imageblob/${user}/${project}/${objectId}", new ImageBlobResourceReference());
		
//		getSharedResources().add(RawBlobResourceReference.RAW_BLOB_RESOURCE, new RawBlobResource());
		mountResource("rawblob/${user}/${project}/${objectId}", new RawBlobResourceReference());
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
