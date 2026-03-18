package io.onedev.server.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.server.ai.dispatch.AiDispatchAgent;
import io.onedev.server.model.AiDispatchRun;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;

public interface AiDispatchRunService extends EntityService<AiDispatchRun> {

	void create(AiDispatchRun run);

	void update(AiDispatchRun run);

	List<AiDispatchRun> query(PullRequest request);

	List<AiDispatchRun> query(PullRequestComment comment);

	List<AiDispatchRun> queryRecent();

	List<AiDispatchRun> queryRecent(@Nullable AiDispatchAgent agent,
									@Nullable Collection<AiDispatchRun.State> states);

	List<AiDispatchRun> queryUnfinished();

	Map<AiDispatchRun.State, Long> countByState(@Nullable AiDispatchAgent agent);

	int countActive();

	@Nullable
	AiDispatchRun findLast(PullRequestComment comment);

}
