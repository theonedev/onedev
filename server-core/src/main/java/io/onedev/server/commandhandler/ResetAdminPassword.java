package io.onedev.server.commandhandler;

import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.ConnectionCallable;
import io.onedev.server.persistence.DataManager;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class ResetAdminPassword extends AbstractPlugin {

	public static final String COMMAND = "reset-admin-password";
	
	private static final Logger logger = LoggerFactory.getLogger(ResetAdminPassword.class);
	
	private final DataManager dataManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final UserManager userManager;
	
	private final PasswordService passwordService;
	
	@Inject
	public ResetAdminPassword(DataManager dataManager, SessionFactoryManager sessionFactoryManager, 
			UserManager userManager, PasswordService passwordService, 
			TransactionManager transactionManager) {
		this.dataManager = dataManager;
		this.sessionFactoryManager = sessionFactoryManager;
		this.userManager = userManager;
		this.passwordService = passwordService;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		if (Bootstrap.command.getArgs().length == 0) {
			logger.error("Missing password parameter. Usage: {} <new password>", Bootstrap.command.getScript());
			System.exit(1);
		}
		if (OneDev.isServerRunning(Bootstrap.installDir)) {
			logger.error("Please stop server before resetting admin password");
			System.exit(1);
		}

		sessionFactoryManager.start();
		
		dataManager.callWithConnection(new ConnectionCallable<Void>() {

			@Override
			public Void call(Connection conn) {
				dataManager.checkDataVersion(conn, false);
				return null;
			}
			
		});
		
		User root = userManager.get(User.ROOT_ID);
		if (root == null) {
			logger.error("Server not set up yet");
			System.exit(1);
		}
		String password = Bootstrap.command.getArgs()[0];
		root.setSsoConnector(null);
		root.setTwoFactorAuthentication(null);
		root.setPassword(passwordService.encryptPassword(password));
		userManager.save(root);
		
		// wait for a short period to have embedded db flushing data
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		logger.info("Password of user '" + root.getName() + "' has been reset to: " + password);
		System.exit(0);
	}

	@Override
	public void stop() {
		sessionFactoryManager.stop();
	}

}
