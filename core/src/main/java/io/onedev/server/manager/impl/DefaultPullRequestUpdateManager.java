package io.onedev.server.manager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.RefSpec;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Throwables;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.pullrequest.PullRequestUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.manager.PullRequestCommentManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestUpdateManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultPullRequestUpdateManager extends AbstractEntityManager<PullRequestUpdate> 
		implements PullRequestUpdateManager {
	
	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestManager pullRequestManager;
	
	@Inject
	public DefaultPullRequestUpdateManager(Dao dao, ListenerRegistry listenerRegistry,
			PullRequestCommentManager commentManager, PullRequestManager pullRequestManager) {
		super(dao);
		
		this.listenerRegistry = listenerRegistry;
		this.pullRequestManager = pullRequestManager;
	}

	@Transactional
	@Override
	public void save(PullRequestUpdate update) {
		save(update, true);
	}
	
	@Transactional
	@Override
	public void save(PullRequestUpdate update, boolean independent) {
		PullRequest request = update.getRequest();
		
		dao.persist(update);
		
		ObjectId updateHeadId = ObjectId.fromString(update.getHeadCommitHash());
		if (!request.getTargetProject().equals(request.getSourceProject())) {
			try {
				request.getTargetProject().git().fetch()
						.setRemote(request.getSourceProject().getGitDir().getAbsolutePath())
						.setRefSpecs(new RefSpec(GitUtils.branch2ref(request.getSourceBranch()) + ":" + update.getHeadRef()))
						.call();
				if (!request.getTargetProject().getObjectId(update.getHeadRef()).equals(updateHeadId)) {
					RefUpdate refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), 
							update.getHeadRef());
					refUpdate.setNewObjectId(updateHeadId);
					GitUtils.updateRef(refUpdate);
				}
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		} else {
			RefUpdate refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), 
					update.getHeadRef());
			refUpdate.setNewObjectId(updateHeadId);
			GitUtils.updateRef(refUpdate);
		}

		request.setHeadCommitHash(update.getHeadCommitHash());
		pullRequestManager.save(request);
		
		if (independent) {
			PullRequestUpdated event = new PullRequestUpdated(update);
			listenerRegistry.post(event);
		}
	}

	@Sessional
	@Override
	public List<PullRequestUpdate> queryAfter(Project project, Long afterUpdateId, int count) {
		EntityCriteria<PullRequestUpdate> criteria = newCriteria();
		criteria.createCriteria("request").add(Restrictions.eq("targetProject", project));
		criteria.addOrder(Order.asc("id"));
		if (afterUpdateId != null) 
			criteria.add(Restrictions.gt("id", afterUpdateId));
		return query(criteria, 0, count);
	}

}
