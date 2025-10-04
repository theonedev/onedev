package io.onedev.server.commandhandler;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.data.DataService;
import io.onedev.server.persistence.SessionFactoryService;
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
	
	private final DataService dataService;
	
	private final SessionFactoryService sessionFactoryService;
	
	private final UserService userService;
	
	private final PasswordService passwordService;
	
	@Inject
	public ResetAdminPassword(HibernateConfig hibernateConfig, DataService dataService,
                              SessionFactoryService sessionFactoryService, UserService userService,
                              PasswordService passwordService) {
		super(hibernateConfig);
		this.dataService = dataService;
		this.sessionFactoryService = sessionFactoryService;
		this.userService = userService;
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
				sessionFactoryService.start();

				try (var conn = dataService.openConnection()) {
					callWithTransaction(conn, () -> {
						dataService.checkDataVersion(conn, false);
						return null;
					});
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}

				User root = userService.get(User.ROOT_ID);
				if (root == null)
					throw new ExplicitException("Server not set up yet");
				String password = Bootstrap.command.getArgs()[0];
				root.setTwoFactorAuthentication(null);
				root.setPassword(passwordService.encryptPassword(password));
				userService.update(root, null);

				// wait for a short period to have embedded db flushing data
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				logger.info("Password of user '" + root.getName() + "' has been reset");
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
		sessionFactoryService.stop();
	}

}
