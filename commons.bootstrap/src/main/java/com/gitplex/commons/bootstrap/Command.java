package com.gitplex.commons.bootstrap;

public class Command {

	// command for database restore
	public static final String RESTORE = "restore";
	
	// command for database upgrade
	public static final String UPGRADE = "upgrade";
	
	// command for database constraint apply
	public static final String APPLY_DB_CONSTRAINTS = "apply_db_constraints";
	
	// command for database backup
	public static final String BACKUP = "backup";
	
	// command for database clean
	public static final String CLEAN = "clean";
	
	// command for data version lookup
	public static final String CHECK_DATA_VERSION = "check_data_version";
	
	// command for printing database dialect
	public static final String DB_DIALECT = "db_dialect";

	private final String name;
	
	private final String[] args;
	
	public Command(String name, String[] args) {
		this.name = name;
		this.args = args;
	}

	public String getName() {
		return name;
	}

	public String[] getArgs() {
		return args;
	}
	
	public String getScript() {
		return getScript(name);
	}
	
	public static String getScript(String commandName) {
		if (BootstrapUtils.isWindows())
			return commandName + ".bat";
		else
			return commandName + ".sh";
	}
	
}
