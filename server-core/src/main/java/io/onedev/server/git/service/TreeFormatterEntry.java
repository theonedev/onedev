package io.onedev.server.git.service;

import java.nio.charset.StandardCharsets;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

public class TreeFormatterEntry implements Comparable<TreeFormatterEntry> {

	final String name;
	
	final int mode;
	
	final ObjectId id;
	
	final byte[] nameBytes;
	
	public TreeFormatterEntry(String name, int mode, ObjectId id) {
		this.name = name;
		this.mode = mode;
		this.id = id;
		if (FileMode.fromBits(mode).equals(FileMode.TYPE_TREE))
			nameBytes = (name + "/").getBytes(StandardCharsets.UTF_8);
		else
			nameBytes = name.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public int compareTo(TreeFormatterEntry o) {
		for (int i = 0; i<nameBytes.length && i<o.nameBytes.length; i++) {
			final int cmp = (nameBytes[i] & 0xff) - (o.nameBytes[i] & 0xff);
			if (cmp != 0)
				return cmp;
		}
		return nameBytes.length - o.nameBytes.length;
	}

}