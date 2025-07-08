package io.onedev.server.rest.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.entitymanager.AuditManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.GlobalProjectSetting;
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.ServiceDeskSetting;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.mailservice.MailService;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.rest.InvalidParamException;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;

@Path("/settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SettingResource {

	private final SettingManager settingManager;

	private final AuditManager auditManager;
	
	@Inject
	public SettingResource(SettingManager settingManager, AuditManager auditManager) {
		this.settingManager = settingManager;
		this.auditManager = auditManager;
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
    public List<ContributedAdministrationSetting> getContributedSettings() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return new ArrayList<>(settingManager.getContributedSettings().values());
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
    	var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getSystemSetting()).toXML();
    	settingManager.saveSystemSetting(systemSetting);
		auditManager.audit(null, "changed system setting via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(systemSetting).toXML());
    	return Response.ok().build();
    }

	@Api(order=1600)
	@Path("/authenticator")
    @POST
    public Response setAuthenticator(Authenticator authenticator) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getAuthenticator()).toXML();
    	settingManager.saveAuthenticator(authenticator);
		auditManager.audit(null, "changed authenticator via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(authenticator).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=1700)
	@Path("/backup")
    @POST
    public Response setBackupSetting(BackupSetting backupSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getBackupSetting()).toXML();
    	settingManager.saveBackupSetting(backupSetting);
		auditManager.audit(null, "changed backup settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(backupSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=1800)
	@Path("/build")
    @POST
    public Response setBuildSetting(@NotNull GlobalBuildSetting buildSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getBuildSetting()).toXML();
    	settingManager.saveBuildSetting(buildSetting);
		auditManager.audit(null, "changed build settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(buildSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=1900)
	@Path("/groovy-scripts")
	@POST
    public Response setGroovyScripts(@NotNull List<GroovyScript> groovyScripts) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getGroovyScripts()).toXML();
    	settingManager.saveGroovyScripts(groovyScripts);
		auditManager.audit(null, "changed groovy scripts via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(groovyScripts).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2000)
	@Path("/issue")
	@POST
    public Response setIssueSetting(@NotNull GlobalIssueSetting issueSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getIssueSetting()).toXML();
    	issueSetting.setReconciled(false);
    	settingManager.saveIssueSetting(issueSetting);
		auditManager.audit(null, "changed issue settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(issueSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2100)
	@Path("/job-executors")
	@POST
    public Response setJobExecutors(@NotNull List<JobExecutor> jobExecutors) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getJobExecutors()).toXML();
    	settingManager.saveJobExecutors(jobExecutors);
		auditManager.audit(null, "changed job executors via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(jobExecutors).toXML());
    	return Response.ok().build();
    }

	@Api(order=2200)
	@Path("/mail-service")
	@POST
    public Response setMailService(MailService mailService) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getMailService()).toXML();
    	settingManager.saveMailService(mailService);
		auditManager.audit(null, "changed mail service via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(mailService).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2210)
	@Path("/service-desk")
	@POST
    public Response setServiceDeskSetting(ServiceDeskSetting serviceDeskSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getServiceDeskSetting()).toXML();
    	settingManager.saveServiceDeskSetting(serviceDeskSetting);
		auditManager.audit(null, "changed service desk settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(serviceDeskSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2220)
	@Path("/notification-template")
	@POST
    public Response setNotificationTemplateSetting(EmailTemplates emailTemplates) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getEmailTemplates()).toXML();
    	settingManager.saveEmailTemplates(emailTemplates);
		auditManager.audit(null, "changed notification template via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(emailTemplates).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2300)
	@Path("/project")
	@POST
    public Response setProjectSetting(@NotNull GlobalProjectSetting projectSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getProjectSetting()).toXML();
    	settingManager.saveProjectSetting(projectSetting);
		auditManager.audit(null, "changed project settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(projectSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2400)
	@Path("/pull-request")
	@POST
    public Response setPullRequestSetting(@NotNull GlobalPullRequestSetting pullRequestSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getPullRequestSetting()).toXML();
    	settingManager.savePullRequestSetting(pullRequestSetting);
		auditManager.audit(null, "changed pull request settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(pullRequestSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2500)
	@Path("/security")
	@POST
    public Response setSecuritySetting(@NotNull SecuritySetting securitySetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getSecuritySetting()).toXML();
    	settingManager.saveSecuritySetting(securitySetting);
		auditManager.audit(null, "changed security settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(securitySetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2600)
	@Path("/ssh")
	@POST
    public Response setSshSetting(@NotNull SshSetting sshSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getSshSetting()).toXML();
    	settingManager.saveSshSetting(sshSetting);
		auditManager.audit(null, "changed ssh settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(sshSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2700)
	@Path("/sso-connectors")
	@POST
    public Response setSsoConnectors(@NotNull List<SsoConnector> ssoConnectors) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingManager.getSsoConnectors()).toXML();
    	settingManager.saveSsoConnectors(ssoConnectors);
		auditManager.audit(null, "changed sso connectors via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(ssoConnectors).toXML());
    	return Response.ok().build();
    }
	
	private String getAuditContent(Map<String, ContributedAdministrationSetting> contributedSettings) {
		var list = new ArrayList<ContributedAdministrationSetting>();
		for (var entry: contributedSettings.entrySet())
			list.add(entry.getValue());
		Collections.sort(list, (a, b) -> {return a.getClass().getName().compareTo(b.getClass().getName());});
		return VersionedXmlDoc.fromBean(list).toXML();
	}
	
	@Api(order=2800)
	@Path("/contributed-settings")
	@POST
    public Response setContributedSettings(@NotNull List<ContributedAdministrationSetting> contributedSettings) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = getAuditContent(settingManager.getContributedSettings());
		var settingMap = new HashMap<String, ContributedAdministrationSetting>();
		for (var setting: contributedSettings)
			settingMap.put(setting.getClass().getName(), setting);
    	settingManager.saveContributedSettings(settingMap);
		auditManager.audit(null, "changed contributed settings via RESTful API", 
				oldAuditContent, getAuditContent(settingMap));
    	return Response.ok().build();
    }
	
}
