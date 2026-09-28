package io.onedev.server.logging.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.onedev.server.logging.LogEntry;

/** Combined stream entries and the corresponding positions in the stage files. */
public class BuildLogSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	BuildLogSnapshot() {
	}

	String logVersion;

	public final List<LogEntry> entries = new ArrayList<>();

	final Map<String, Integer> offsets = new HashMap<>();

}
