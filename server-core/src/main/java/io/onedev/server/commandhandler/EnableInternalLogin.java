package io.onedev.server.commandhandler;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.data.DataService;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.SessionFactoryService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.SettingService;

@Singleton
public class EnableInternalLogin extends CommandHandler {

	public static final String COMMAND = "enable-internal-login";

	private static final Logger logger = LoggerFactory.getLogger(EnableInternalLogin.class);

	private final DataService dataService;

	private final SessionFactoryService sessionFactoryService;

	private final SettingService settingService;

	private final TransactionService transactionService;

	@Inject
	public EnableInternalLogin(HibernateConfig hibernateConfig, DataService dataService,
				SessionFactoryService sessionFactoryService, SettingService settingService,
				TransactionService transactionService) {
		super(hibernateConfig);
		this.dataService = dataService;
		this.sessionFactoryService = sessionFactoryService;
		this.settingService = settingService;
		this.transactionService = transactionService;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();

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

				transactionService.run(() -> {
					var setting = settingService.findSetting(Key.SECURITY);
					if (setting == null || setting.getValue() == null)
						throw new ExplicitException("Server not set up yet");
					var securitySetting = (SecuritySetting) setting.getValue();
					securitySetting.setDisableInternalLogin(false);
					setting.setValue(securitySetting);
				});

				logger.info("Internal login form has been enabled");
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
