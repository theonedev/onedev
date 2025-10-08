package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.BuildParamService;

@Singleton
public class DefaultBuildParamService extends BaseEntityService<BuildParam> implements BuildParamService {

	private static final Logger logger = LoggerFactory.getLogger(DefaultBuildParamService.class);

	@Inject
	private TransactionService transactionService;

	@Inject
	private ClusterService clusterService;
	
	private volatile Map<Long, Collection<String>> paramNames;

	@SuppressWarnings("unchecked")
	@Sessional
	@Listen
	public void on(SystemStarting event) {
		logger.info("Caching build param info...");

		var hazelcastInstance = clusterService.getHazelcastInstance();
		paramNames = hazelcastInstance.getMap("buildParamNames");
		
		var cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("buildParamCacheInited");
		clusterService.initWithLead(cacheInited, () -> {
			Map<Long, Long> projectIds = new HashMap<>();
			Query<?> query = dao.getSession().createQuery("select id, project.id from Build");
			for (Object[] fields: (List<Object[]>)query.list())
				projectIds.put((Long)fields[0], (Long)fields[1]);

			query = dao.getSession().createQuery("select build.id, name from BuildParam");
			for (Object[] fields: (List<Object[]>)query.list()) {
				Long projectId = projectIds.get(fields[0]);
				if (projectId != null)
					addParam(projectId, (String) fields[1]);
			}
			return 1L;			
		});		
	}

	@Transactional
	@Override
	public void create(BuildParam param) {
		Preconditions.checkState(param.isNew());
		dao.persist(param);
	}

	private void addParam(Long projectId, String paramName) {
		Collection<String> paramsOfProject = paramNames.get(projectId);
		if (paramsOfProject == null) 
			paramsOfProject = new HashSet<>();
		paramsOfProject.add(paramName);
		paramNames.put(projectId, paramsOfProject);
	}
	
	@Override
	public Collection<String> getParamNames(@Nullable Project project) {
		Collection<String> paramNames = new HashSet<>();
		for (Map.Entry<Long, Collection<String>> entry: this.paramNames.entrySet()) {
			if (project == null || project.getId().equals(entry.getKey()))
				paramNames.addAll(entry.getValue());
		}
		return paramNames;
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof BuildParam) {
			BuildParam param = (BuildParam) event.getEntity();
			Long projectId = param.getBuild().getProject().getId();
			String paramName = param.getName();
			transactionService.runAfterCommit(() -> addParam(projectId, paramName));
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			transactionService.runAfterCommit(() -> paramNames.remove(projectId));
		}
	}
	
}
