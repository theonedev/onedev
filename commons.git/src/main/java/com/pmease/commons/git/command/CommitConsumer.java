package com.pmease.commons.git.command;

import com.pmease.commons.git.Commit;

public interface CommitConsumer {
	void consume(Commit commit);
}