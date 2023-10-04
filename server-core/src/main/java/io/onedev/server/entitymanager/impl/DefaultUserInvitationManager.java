package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserInvitationManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.model.UserInvitation;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class DefaultUserInvitationManager extends BaseEntityManager<UserInvitation> implements UserInvitationManager {

	private final SettingManager settingManager;
	
	private final MailManager mailManager;
	
	@Inject
    public DefaultUserInvitationManager(Dao dao, SettingManager settingManager, MailManager mailManager) {
        super(dao);
        this.settingManager = settingManager;
        this.mailManager = mailManager;
    }

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
		Preconditions.checkState(settingManager.getMailService() != null);
		
		String serverUrl = settingManager.getSystemSetting().getServerUrl();
		
		String setupAccountUrl = String.format("%s/~create-user-from-invitation/%d/%s", 
				serverUrl, invitation.getId(), invitation.getInvitationCode());
		
		Map<String, Object> bindings = new HashMap<>();
		bindings.put("setupAccountUrl", setupAccountUrl);
		
		String template = settingManager.getEmailTemplates().getUserInvitation();
		String htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
		String textBody = EmailTemplates.evalTemplate(false, template, bindings);
		
		mailManager.sendMail(Arrays.asList(invitation.getEmailAddress()),
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