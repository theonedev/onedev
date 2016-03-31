package com.pmease.gitplex.web;

import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.protocol.http.WicketServlet;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.component.DepotVisits;
import com.pmease.gitplex.core.manager.AccountManager;

public class WebSession extends org.apache.wicket.protocol.http.WebSession {

	private static final long serialVersionUID = 1L;	

	private DepotVisits depotVisits;
	
	public WebSession(Request request) {
		super(request);
	}

	public static WebSession get() {
		return (WebSession) org.apache.wicket.protocol.http.WebSession.get();
	}

	public void login(String userName, String password, boolean rememberMe) {
		Subject subject = SecurityUtils.getSubject();

		// Force a new session to prevent fixation attack.
		// We have to invalidate via both Shiro and Wicket; otherwise it doesn't
		// work.
		subject.getSession().stop();
		WebSession.get().replaceSession(); 

		UsernamePasswordToken token;
		token = new UsernamePasswordToken(userName, password, rememberMe);
		
		subject.login(token);
	}
	
	public void logout() {
		Account user = com.pmease.gitplex.core.security.SecurityUtils.getAccount();
		if (user != null) {
			flushDepotVisits(user);
		}
		SecurityUtils.getSubject().logout();
        WebSession session = WebSession.get();
        session.replaceSession();
	}
	
	public void flushDepotVisits(Account user) {
		if (depotVisits != null) {
			user.setDepotVisits(depotVisits);
			GitPlex.getInstance(AccountManager.class).save(user, null);
		}
	}
	
	public DepotVisits getDepotVisits() {
		Preconditions.checkNotNull(RequestCycle.get());
		
		if (depotVisits == null) {
			Account user = com.pmease.gitplex.core.security.SecurityUtils.getAccount();
			if (user != null) {
				depotVisits = user.getDepotVisits();
			} else {
				depotVisits = new DepotVisits();
			}
		}
		return depotVisits;
	}
	
	public static WebSession from(HttpSession session) {
		String attributeName = "wicket:" + GitPlex.getInstance(WicketServlet.class).getServletName() + ":session";
		return (WebSession) session.getAttribute(attributeName);		
	}
	
}
