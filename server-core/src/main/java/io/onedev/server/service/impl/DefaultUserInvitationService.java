package io.onedev.server.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.server.mail.MailService;
import io.onedev.server.model.UserInvitation;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserInvitationService;

@Singleton
public class DefaultUserInvitationService extends BaseEntityService<UserInvitation> implements UserInvitationService {

	@Inject
	private SettingService settingService;

	@Inject
	private MailService mailService;

	@Override
	public UserInvitation findByEmailAddress(String emailAddress) {
		EntityCriteria<UserInvitation> criteria = EntityCriteria.of(UserInvitation.class);
		criteria.add(Restrictions.eq(UserInvitation.PROP_EMAIL_ADDRESS, emailAddress));
		return find(criteria);
	}

	@Override
	public UserInvitation findByInvitationCode(String invitationCode) {
		EntityCriteria<UserInvitation> criteria = EntityCriteria.of(UserInvitation.class);
		criteria.add(Restrictions.eq(UserInvitation.PROP_INVITATION_CODE, invitationCode));
		return find(criteria);
	}

	@Override
	public void sendInvitationEmail(UserInvitation invitation) {
		Preconditions.checkState(settingService.getMailConnector() != null);
		
		String serverUrl = settingService.getSystemSetting().getServerUrl();
		
		String setupAccountUrl = String.format("%s/~create-user-from-invitation/%d/%s", 
				serverUrl, invitation.getId(), invitation.getInvitationCode());
		
		Map<String, Object> bindings = new HashMap<>();
		bindings.put("setupAccountUrl", setupAccountUrl);
		
		String template = settingService.getEmailTemplates().getUserInvitation();
		String htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
		String textBody = EmailTemplates.evalTemplate(false, template, bindings);
		
		mailService.sendMail(Arrays.asList(invitation.getEmailAddress()),
				Lists.newArrayList(), Lists.newArrayList(), 
				"[Invitation] You are Invited to Use OneDev", 
				htmlBody, textBody, null, null, null);
	}
	
	private EntityCriteria<UserInvitation> getCriteria(@Nullable String term) {
		EntityCriteria<UserInvitation> criteria = EntityCriteria.of(UserInvitation.class);
		if (term != null) 
			criteria.add(Restrictions.ilike(UserInvitation.PROP_EMAIL_ADDRESS, term, MatchMode.ANYWHERE));
		else
			criteria.setCacheable(true);
		return criteria;
	}
	
	@Sessional
	@Override
	public List<UserInvitation> query(String term, int firstResult, int maxResults) {
		EntityCriteria<UserInvitation> criteria = getCriteria(term);
		criteria.addOrder(Order.asc(UserInvitation.PROP_EMAIL_ADDRESS));
		return query(criteria, firstResult, maxResults);
	}

	@Transactional
	@Override
	public void create(UserInvitation invitation) {
		Preconditions.checkState(invitation.isNew());
		dao.persist(invitation);
	}

	@Sessional
	@Override
	public int count(String term) {
		return count(getCriteria(term));
	}
	
}