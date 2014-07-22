package com.pmease.gitplex.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

import com.google.common.base.Optional;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;

public class GitPlexSession extends WebSession {

	private static final long serialVersionUID = 1L;

	public GitPlexSession(Request request) {
		super(request);
	}

	public static GitPlexSession get() {
		return (GitPlexSession) WebSession.get();
	}

	public static Optional<User> getCurrentUser() {
		return Optional.fromNullable(GitPlex.getInstance(UserManager.class).getCurrent());
	}
	
	/**
	 * Peform the actual authentication using Shiro's {@link Subject#login
	 * login()}.
	 * <p>
	 * <b>Important:</b> this method is written to ensure that the user's
	 * session is replaced with a new session before authentication is
	 * performed. This is to prevent a <a
	 * href="https://www.owasp.org/index.php/Session_Fixation">session
	 * fixation</a> attack. As a side effect, any existing session data will
	 * therefore be lost.
	 * 
	 * @return {@code true} if authentication succeeded
	 */
	public void login(String loginName, String password, boolean remember)
			throws AuthenticationException {
		
		Subject currentUser = SecurityUtils.getSubject();

		// Force a new session to prevent fixation attack.
		// We have to invalidate via both Shiro and Wicket; otherwise it doesn't
		// work.
		currentUser.getSession().stop(); // Shiro
		replaceSession(); // Wicket

		UsernamePasswordToken token;
		token = new UsernamePasswordToken(loginName, password, remember);
		currentUser.login(token);
	}

}
