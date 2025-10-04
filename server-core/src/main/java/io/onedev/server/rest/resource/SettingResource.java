package io.onedev.server.rest.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.SettingService;
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
import io.onedev.server.model.support.administration.mailservice.MailConnector;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;

@Path("/settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SettingResource {

	private final SettingService settingService;

	private final AuditService auditService;
	
	@Inject
	public SettingResource(SettingService settingService, AuditService auditService) {
		this.settingService = settingService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/system")
    @GET
    public SystemSetting getSystemSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getSystemSetting();
    }

	@Api(order=200)
	@Path("/authenticator")
    @GET
    public Authenticator getAuthenticator() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getAuthenticator();
    }
	
	@Api(order=300)
	@Path("/backup")
    @GET
    public BackupSetting getBackupSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getBackupSetting();
    }
	
	@Api(order=400)
	@Path("/build")
    @GET
    public GlobalBuildSetting getBuildSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getBuildSetting();
    }
	
	@Api(order=500)
	@Path("/groovy-scripts")
    @GET
    public List<GroovyScript> getGroovyScripts() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getGroovyScripts();
    }
	
	@Api(order=600)
	@Path("/issue")
    @GET
    public GlobalIssueSetting getIssueSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getIssueSetting();
    }
	
	@Api(order=700)
	@Path("/job-executors")
    @GET
    public List<JobExecutor> getJobExecutors() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getJobExecutors();
    }

	@Api(order=800)
	@Path("/mail-service")
    @GET
    public MailConnector getMailConnector() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getMailConnector();
    }
	
	@Api(order=850)
	@Path("/service-desk")
    @GET
    public ServiceDeskSetting getServiceDeskSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getServiceDeskSetting();
    }
	
	@Api(order=900)
	@Path("/notification-template")
    @GET
    public EmailTemplates getNotificiationTemplateSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getEmailTemplates();
    }
	
	@Api(order=1000)
	@Path("/project")
    @GET
    public GlobalProjectSetting getProjectSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getProjectSetting();
    }
	
	@Api(order=1100)
	@Path("/pull-request")
    @GET
    public GlobalPullRequestSetting getPullRequestSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getPullRequestSetting();
    }
	
	@Api(order=1200)
	@Path("/security")
    @GET
    public SecuritySetting getSecuritySetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getSecuritySetting();
    }
	
	@Api(order=1300)
	@Path("/ssh")
    @GET
    public SshSetting getSshSetting() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return settingService.getSshSetting();
    }
		
	@Api(order=1450)
	@Path("/contributed-settings")
    @GET
    public List<ContributedAdministrationSetting> getContributedSettings() {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	return new ArrayList<>(settingService.getContributedSettings().values());
    }
	
	@Api(order=1500)
	@Path("/system")
    @POST
    public Response setSystemSetting(@NotNull @Valid SystemSetting systemSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
    	String ingressUrl = OneDev.getInstance().getIngressUrl();
    	if (ingressUrl != null && !ingressUrl.equals(systemSetting.getServerUrl()))
    		throw new NotAcceptableException("Server URL can only be \"" + ingressUrl + "\"");
    	var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getSystemSetting()).toXML();
    	settingService.saveSystemSetting(systemSetting);
		auditService.audit(null, "changed system setting via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(systemSetting).toXML());
    	return Response.ok().build();
    }

	@Api(order=1600)
	@Path("/authenticator")
    @POST
    public Response setAuthenticator(@Valid Authenticator authenticator) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getAuthenticator()).toXML();
    	settingService.saveAuthenticator(authenticator);
		auditService.audit(null, "changed authenticator via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(authenticator).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=1700)
	@Path("/backup")
    @POST
    public Response setBackupSetting(@Valid BackupSetting backupSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getBackupSetting()).toXML();
    	settingService.saveBackupSetting(backupSetting);
		auditService.audit(null, "changed backup settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(backupSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=1800)
	@Path("/build")
    @POST
    public Response setBuildSetting(@NotNull @Valid GlobalBuildSetting buildSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getBuildSetting()).toXML();
    	settingService.saveBuildSetting(buildSetting);
		auditService.audit(null, "changed build settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(buildSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=1900)
	@Path("/groovy-scripts")
	@POST
    public Response setGroovyScripts(@NotNull @Valid List<GroovyScript> groovyScripts) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getGroovyScripts()).toXML();
    	settingService.saveGroovyScripts(groovyScripts);
		auditService.audit(null, "changed groovy scripts via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(groovyScripts).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2000)
	@Path("/issue")
	@POST
    public Response setIssueSetting(@NotNull @Valid GlobalIssueSetting issueSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getIssueSetting()).toXML();
    	issueSetting.setReconciled(false);
    	settingService.saveIssueSetting(issueSetting);
		auditService.audit(null, "changed issue settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(issueSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2100)
	@Path("/job-executors")
	@POST
    public Response setJobExecutors(@NotNull @Valid List<JobExecutor> jobExecutors) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getJobExecutors()).toXML();
    	settingService.saveJobExecutors(jobExecutors);
		auditService.audit(null, "changed job executors via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(jobExecutors).toXML());
    	return Response.ok().build();
    }

	@Api(order=2200)
	@Path("/mail-service")
	@POST
    public Response setMailService(@Valid MailConnector mailConnector) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getMailConnector()).toXML();
    	settingService.saveMailConnector(mailConnector);
		auditService.audit(null, "changed mail service via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(mailConnector).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2210)
	@Path("/service-desk")
	@POST
    public Response setServiceDeskSetting(@Valid ServiceDeskSetting serviceDeskSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getServiceDeskSetting()).toXML();
    	settingService.saveServiceDeskSetting(serviceDeskSetting);
		auditService.audit(null, "changed service desk settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(serviceDeskSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2220)
	@Path("/notification-template")
	@POST
    public Response setNotificationTemplateSetting(@Valid EmailTemplates emailTemplates) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getEmailTemplates()).toXML();
    	settingService.saveEmailTemplates(emailTemplates);
		auditService.audit(null, "changed notification template via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(emailTemplates).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2300)
	@Path("/project")
	@POST
    public Response setProjectSetting(@NotNull @Valid GlobalProjectSetting projectSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getProjectSetting()).toXML();
    	settingService.saveProjectSetting(projectSetting);
		auditService.audit(null, "changed project settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(projectSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2400)
	@Path("/pull-request")
	@POST
    public Response setPullRequestSetting(@NotNull @Valid GlobalPullRequestSetting pullRequestSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getPullRequestSetting()).toXML();
    	settingService.savePullRequestSetting(pullRequestSetting);
		auditService.audit(null, "changed pull request settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(pullRequestSetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2500)
	@Path("/security")
	@POST
    public Response setSecuritySetting(@NotNull @Valid SecuritySetting securitySetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getSecuritySetting()).toXML();
    	settingService.saveSecuritySetting(securitySetting);
		auditService.audit(null, "changed security settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(securitySetting).toXML());
    	return Response.ok().build();
    }
	
	@Api(order=2600)
	@Path("/ssh")
	@POST
    public Response setSshSetting(@NotNull @Valid SshSetting sshSetting) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = VersionedXmlDoc.fromBean(settingService.getSshSetting()).toXML();
    	settingService.saveSshSetting(sshSetting);
		auditService.audit(null, "changed ssh settings via RESTful API", 
				oldAuditContent, VersionedXmlDoc.fromBean(sshSetting).toXML());
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
    public Response setContributedSettings(@NotNull @Valid List<ContributedAdministrationSetting> contributedSettings) {
    	if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		var oldAuditContent = getAuditContent(settingService.getContributedSettings());
		var settingMap = new HashMap<String, ContributedAdministrationSetting>();
		for (var setting: contributedSettings)
			settingMap.put(setting.getClass().getName(), setting);
    	settingService.saveContributedSettings(settingMap);
		auditService.audit(null, "changed contributed settings via RESTful API", 
				oldAuditContent, getAuditContent(settingMap));
    	return Response.ok().build();
    }
	
}
