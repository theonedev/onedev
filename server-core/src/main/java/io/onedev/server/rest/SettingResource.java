package io.onedev.server.rest;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.mailservice.MailService;
import io.onedev.server.model.support.administration.*;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Api(order=100000)
@Path("/settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SettingResource {

	private final SettingManager settingManager;
	
	@Inject
	public SettingResource(SettingManager settingManager) {
		this.settingManager = settingManager;
	}

	@Api(order=100)
	@Path("/system")
    @GET
    public SystemSetting getSystemSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getSystemSetting();
    }

	@Api(order=200)
	@Path("/authenticator")
    @GET
    public Authenticator getAuthenticator() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getAuthenticator();
    }
	
	@Api(order=300)
	@Path("/backup")
    @GET
    public BackupSetting getBackupSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getBackupSetting();
    }
	
	@Api(order=400)
	@Path("/build")
    @GET
    public GlobalBuildSetting getBuildSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getBuildSetting();
    }
	
	@Api(order=500)
	@Path("/groovy-scripts")
    @GET
    public List<GroovyScript> getGroovyScripts() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getGroovyScripts();
    }
	
	@Api(order=600)
	@Path("/issue")
    @GET
    public GlobalIssueSetting getIssueSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getIssueSetting();
    }
	
	@Api(order=700)
	@Path("/job-executors")
    @GET
    public List<JobExecutor> getJobExecutors() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getJobExecutors();
    }

	@Api(order=800)
	@Path("/mail-service")
    @GET
    public MailService getMailService() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getMailService();
    }
	
	@Api(order=850)
	@Path("/service-desk")
    @GET
    public ServiceDeskSetting getServiceDeskSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getServiceDeskSetting();
    }
	
	@Api(order=900)
	@Path("/notification-template")
    @GET
    public EmailTemplates getNotificiationTemplateSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getEmailTemplates();
    }
	
	@Api(order=1000)
	@Path("/project")
    @GET
    public GlobalProjectSetting getProjectSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getProjectSetting();
    }
	
	@Api(order=1100)
	@Path("/pull-request")
    @GET
    public GlobalPullRequestSetting getPullRequestSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getPullRequestSetting();
    }
	
	@Api(order=1200)
	@Path("/security")
    @GET
    public SecuritySetting getSecuritySetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getSecuritySetting();
    }
	
	@Api(order=1300)
	@Path("/ssh")
    @GET
    public SshSetting getSshSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getSshSetting();
    }
	
	@Api(order=1400)
	@Path("/sso-connectors")
    @GET
    public List<SsoConnector> getSsoConnectors() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getSsoConnectors();
    }
	
	@Api(order=1450)
	@Path("/contributed-settings")
    @GET
    public Map<String, ContributedAdministrationSetting> getContributedSettings() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingManager.getContributedSettings();
    }
	
	@Api(order=1500)
	@Path("/system")
    @POST
    public Response setSystemSetting(@NotNull SystemSetting systemSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	String ingressUrl = OneDev.getInstance().getIngressUrl();
    	if (ingressUrl != null && !ingressUrl.equals(systemSetting.getServerUrl()))
    		throw new InvalidParamException("Server URL can only be \"" + ingressUrl + "\"");
    	
    	settingManager.saveSystemSetting(systemSetting);
    	return Response.ok().build();
    }

	@Api(order=1600)
	@Path("/authenticator")
    @POST
    public Response setAuthenticator(Authenticator authenticator) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveAuthenticator(authenticator);
    	return Response.ok().build();
    }
	
	@Api(order=1700)
	@Path("/backup")
    @POST
    public Response setBackupSetting(BackupSetting backupSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveBackupSetting(backupSetting);
    	return Response.ok().build();
    }
	
	@Api(order=1800)
	@Path("/build")
    @POST
    public Response setBuildSetting(@NotNull GlobalBuildSetting buildSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveBuildSetting(buildSetting);
    	return Response.ok().build();
    }
	
	@Api(order=1900)
	@Path("/groovy-scripts")
	@POST
    public Response setGroovyScripts(@NotNull List<GroovyScript> groovyScripts) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveGroovyScripts(groovyScripts);
    	return Response.ok().build();
    }
	
	@Api(order=2000)
	@Path("/issue")
	@POST
    public Response setIssueSetting(@NotNull GlobalIssueSetting issueSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	issueSetting.setReconciled(false);
    	settingManager.saveIssueSetting(issueSetting);
    	return Response.ok().build();
    }
	
	@Api(order=2100)
	@Path("/job-executors")
	@POST
    public Response setJobExecutors(@NotNull List<JobExecutor> jobExecutors) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveJobExecutors(jobExecutors);
    	return Response.ok().build();
    }

	@Api(order=2200)
	@Path("/mail-service")
	@POST
    public Response setMailService(MailService mailService) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveMailService(mailService);
    	return Response.ok().build();
    }
	
	@Api(order=2210)
	@Path("/service-desk")
	@POST
    public Response setServiceDeskSetting(ServiceDeskSetting serviceDeskSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveServiceDeskSetting(serviceDeskSetting);
    	return Response.ok().build();
    }
	
	@Api(order=2220)
	@Path("/notification-template")
	@POST
    public Response setNotificationTemplateSetting(EmailTemplates emailTemplates) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveEmailTemplates(emailTemplates);
    	return Response.ok().build();
    }
	
	@Api(order=2300)
	@Path("/project")
	@POST
    public Response setProjectSetting(@NotNull GlobalProjectSetting projectSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveProjectSetting(projectSetting);
    	return Response.ok().build();
    }
	
	@Api(order=2400)
	@Path("/pull-request")
	@POST
    public Response setPullRequestSetting(@NotNull GlobalPullRequestSetting pullRequestSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.savePullRequestSetting(pullRequestSetting);
    	return Response.ok().build();
    }
	
	@Api(order=2500)
	@Path("/security")
	@POST
    public Response setSecuritySetting(@NotNull SecuritySetting securitySetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveSecuritySetting(securitySetting);
    	return Response.ok().build();
    }
	
	@Api(order=2600)
	@Path("/ssh")
	@POST
    public Response setSshSetting(@NotNull SshSetting sshSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveSshSetting(sshSetting);
    	return Response.ok().build();
    }
	
	@Api(order=2700)
	@Path("/sso-connectors")
	@POST
    public Response setSsoConnectors(@NotNull List<SsoConnector> ssoConnectors) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveSsoConnectors(ssoConnectors);
    	return Response.ok().build();
    }
	
	@Api(order=2800)
	@Path("/contributed-settings")
	@POST
    public Response setContributedSettings(@NotNull Map<String, ContributedAdministrationSetting> contributedSettings) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	settingManager.saveContributedSettings(contributedSettings);
    	return Response.ok().build();
    }
	
}
