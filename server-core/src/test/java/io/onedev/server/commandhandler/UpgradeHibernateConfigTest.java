package io.onedev.server.commandhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UpgradeHibernateConfigTest {

	private static final String LEGACY_DIALECT = "org.hibernate.dialect.MySQL5InnoDBDialect";

	@Test
	void migratesSavedDialectUsingSavedConnectionUrl() throws Exception {
		for (var database : new String[] { "mysql", "mariadb" }) {
			var config = "hibernate.connection.url=jdbc:" + database + "://localhost/onedev\n\n"
					+ " hibernate.dialect : " + LEGACY_DIALECT + "  \n"
					+ "hibernate.connection.password=keep$This\\\\Password\n";
			var dialect = database.equals("mariadb") ? "MariaDBDialect" : "MySQLDialect";
			assertEquals(config.replace(LEGACY_DIALECT, "org.hibernate.dialect." + dialect),
					Upgrade.migrateHibernateDialects(config));
		}
	}

	@Test
	void prefersJakartaConnectionUrl() throws Exception {
		var config = "hibernate.dialect=" + LEGACY_DIALECT + "\n"
				+ "hibernate.connection.url=jdbc:mysql://localhost/onedev\n"
				+ "jakarta.persistence.jdbc.url=jdbc:mariadb://localhost/onedev\n";
		assertEquals(config.replace(LEGACY_DIALECT, "org.hibernate.dialect.MariaDBDialect"),
				Upgrade.migrateHibernateDialects(config));
	}

	@Test
	void migratesEachCommentedExampleIndependently() throws Exception {
		for (var newline : new String[] { "\n", "\r\n" }) {
			var mysql = "#hibernate.dialect=" + LEGACY_DIALECT + newline
					+ "#hibernate.connection.driver_class=com.mysql.cj.jdbc.Driver" + newline
					+ "#hibernate.connection.url=jdbc:mysql://localhost/onedev" + newline;
			var mariadb = "#hibernate.dialect=" + LEGACY_DIALECT + newline
					+ "#hibernate.connection.driver_class=org.mariadb.jdbc.Driver" + newline
					+ "#hibernate.connection.url=jdbc:mariadb://localhost/onedev" + newline
					+ "# Notes can contain literal \\uXXXX without being parsed as properties" + newline;
			var active = "hibernate.dialect=org.hibernate.dialect.HSQLDialect" + newline
					+ "hibernate.connection.url=jdbc:hsqldb:file:internaldb/onedev" + newline + newline;
			var migrated = Upgrade.migrateHibernateDialects(active + mysql + newline + mariadb);
			assertEquals(active + mysql.replace(LEGACY_DIALECT, "org.hibernate.dialect.MySQLDialect")
					+ newline + mariadb.replace(LEGACY_DIALECT, "org.hibernate.dialect.MariaDBDialect"), migrated);
			assertEquals(migrated, Upgrade.migrateHibernateDialects(migrated));
		}
	}

	@Test
	void leavesModernCustomAndUnrelatedValuesUnchanged() throws Exception {
		for (var dialect : new String[] { "org.hibernate.dialect.MySQLDialect",
				"org.hibernate.dialect.MariaDBDialect", "example.CustomDialect" }) {
			var config = "hibernate.dialect=" + dialect + "\n"
					+ "hibernate.connection.url=jdbc:mariadb://localhost/onedev\n"
					+ "hibernate.connection.password=" + LEGACY_DIALECT + "\n"
					+ "# Previous dialect: " + LEGACY_DIALECT + "\n";
			assertEquals(config, Upgrade.migrateHibernateDialects(config));
		}
	}
}
