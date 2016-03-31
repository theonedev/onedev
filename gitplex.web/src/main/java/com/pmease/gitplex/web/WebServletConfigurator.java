package com.pmease.gitplex.web;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.jetty.ClasspathAssetServlet;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.assets.Assets;

@Singleton
public class WebServletConfigurator implements ServletConfigurator {

	private final UnitOfWork unitOfWork;
	
	@Inject
	public WebServletConfigurator(UnitOfWork unitOfWork) {
		this.unitOfWork = unitOfWork;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		context.getSessionHandler().addEventListener(new HttpSessionListener() {

			@Override
			public void sessionCreated(HttpSessionEvent se) {
			}

			@Override
			public void sessionDestroyed(HttpSessionEvent se) {
				SimplePrincipalCollection principals = (SimplePrincipalCollection) se.getSession().getAttribute(
						"org.apache.shiro.subject.support.DefaultSubjectContext_PRINCIPALS_SESSION_KEY");
				if (principals != null) {
					Long userId = (Long) principals.getPrimaryPrincipal();
					if (!userId.equals(0L)) {
						unitOfWork.begin();
						try {
							Account user = GitPlex.getInstance(AccountManager.class).load(userId);
							WebSession.from(se.getSession()).flushDepotVisits(user);
						} finally {
							unitOfWork.end();
						}
					}
				}
			}
			
		});
		ServletHolder servletHolder = new ServletHolder(new ClasspathAssetServlet(Assets.class));
		context.addServlet(servletHolder, "/assets/*");
		context.addServlet(servletHolder, "/favicon.ico");
	}

}
