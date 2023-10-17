package io.onedev.server.commandhandler;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.data.DataManager;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.SQLException;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

@Singleton
public class ResetAdminPassword extends CommandHandler {

	public static final String COMMAND = "reset-admin-password";
	
	private static final Logger logger = LoggerFactory.getLogger(ResetAdminPassword.class);
	
	private final DataManager dataManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final UserManager userManager;
	
	private final PasswordService passwordService;
	
	@Inject
	public ResetAdminPassword(HibernateConfig hibernateConfig, DataManager dataManager, 
							  SessionFactoryManager sessionFactoryManager, UserManager userManager, 
							  PasswordService passwordService) {
		super(hibernateConfig);
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

		try {
			doMaintenance(() -> {
				sessionFactoryManager.start();

				try (var conn = dataManager.openConnection()) {
					callWithTransaction(conn, () -> {
						dataManager.checkDataVersion(conn, false);
						return null;
					});
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}

				User root = userManager.get(User.ROOT_ID);
				if (root == null)
					throw new ExplicitException("Server not set up yet");
				String password = Bootstrap.command.getArgs()[0];
				root.setSsoConnector(null);
				root.setTwoFactorAuthentication(null);
				root.setPassword(passwordService.encryptPassword(password));
				userManager.update(root, null);

				// wait for a short period to have embedded db flushing data
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				logger.info("Password of user '" + root.getName() + "' has been reset to: " + password);
				return null;
			});
			System.exit(0);
		} catch (ExplicitException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}

	@Override
	public void stop() {
		sessionFactoryManager.stop();
	}

}
