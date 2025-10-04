package io.onedev.server.service.impl;

import static io.onedev.server.model.Alert.PROP_SUBJECT;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.collect.Sets;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.cluster.ConnectionLost;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.mail.MailService;
import io.onedev.server.model.Alert;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.AlertService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.websocket.WebSocketService;

@Singleton
public class DefaultAlertService extends BaseEntityService<Alert> implements AlertService {

	@Inject
	private MailService mailService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private WebSocketService webSocketService;

	@Inject
	private SettingService settingService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private SessionService sessionService;

	@Inject
	private UserService userService;

	@Transactional
	@Override
	public void alert(String subject, String detail, boolean mailError) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_SUBJECT, subject));
		var alert = find(criteria);
		if (alert == null) {
			alert = new Alert();
			alert.setSubject(subject);
			alert.setDetail(detail);
			alert.setMailError(mailError);
		}
		alert.setDate(new Date());
		dao.persist(alert);
	}

	@Override
	public void alert(String subject, String detail) {
		alert(subject, detail, false);
	}

	@Listen
	public void on(ConnectionLost event) {
		if (clusterService.isLeaderServer()) {
			var server = event.getServer() + " (" + clusterService.getServerName(event.getServer()) + ")";
			alert("Server '" + server + "' can not be reached", 
					"Server '" + server + "' is part of OneDev cluster, but can not be reached for some reason", 
					false);
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Alert) {
			var alert = (Alert)event.getEntity();			
			if (!alert.isMailError()) {
				var alertSetting = settingService.getAlertSetting();
				if (!alertSetting.getNotifyUsers().isEmpty()) {
					transactionService.runAfterCommit(() -> sessionService.runAsync(() -> {
						var mailConnector = settingService.getMailConnector();
						if (mailConnector == null) {
							alert("Unable to send alert email: Mail service not specified yet", null, true);
						} else {
							var emailAddresses = new ArrayList<String>();
							for (var userName: alertSetting.getNotifyUsers()) {
								var user = userService.findByName(userName);
								if (user == null) {
									alert("Unable to send alert email: User '" + userName + "' does not exist", null, true);
								} else {
									var emailAddress = user.getPrimaryEmailAddress();
									if (emailAddress == null) {
										alert("Unable to send alert email: Primary email address of user "
												+ userName + " is not defined", null, true);
									} else if (!emailAddress.isVerified()) {
										alert("Unable to send alert email: Primary email address of user "
												+ userName + " is not verified yet", null, true);
									} else {
										emailAddresses.add(emailAddress.getValue());
									}
								}
							}
							if (!emailAddresses.isEmpty()) {
								String serverUrl = settingService.getSystemSetting().getServerUrl();
								Map<String, Object> bindings = new HashMap<>();
								bindings.put("alert", alert);
								bindings.put("serverUrl", serverUrl);
								
								String template = settingService.getEmailTemplates().getAlert();
								var htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
								var textBody = EmailTemplates.evalTemplate(false, template, bindings);
								
								mailService.sendMail(emailAddresses, new ArrayList<>(), new ArrayList<>(), 
										"[Alert] " + alert.getSubject(), htmlBody, textBody, 
										null, null, null);
							}
						}
					}));
				}
			}
		}
	}

	@Override
	public int count() {
		return count(true);
	}

	@Transactional
	@Override
	public void clear() {
		var query = getSession().createQuery("delete from Alert");
		query.executeUpdate();
		webSocketService.notifyObservablesChange(Sets.newHashSet(Alert.getChangeObservable()), WicketUtils.getPageKey());		
	}
	
}
