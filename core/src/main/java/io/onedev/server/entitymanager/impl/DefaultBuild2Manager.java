package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import com.google.common.base.Preconditions;

import io.onedev.server.entitymanager.Build2Manager;
import io.onedev.server.entitymanager.BuildDependenceManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.model.Build2;
import io.onedev.server.model.Build2.Status;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultBuild2Manager extends AbstractEntityManager<Build2> implements Build2Manager {

	private final BuildParamManager buildParamManager;
	
	private final BuildDependenceManager buildDependenceManager;
	
	@Inject
	public DefaultBuild2Manager(Dao dao, BuildParamManager buildParamManager, BuildDependenceManager buildDependenceManager) {
		super(dao);
		this.buildParamManager = buildParamManager;
		this.buildDependenceManager = buildDependenceManager;
	}

	@Sessional
	@Override
	public Build2 find(Project project, long number) {
		EntityCriteria<Build2> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("number", number));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Sessional
	@Override
	public List<Build2> query(Project project, String commitHash, String jobName, Map<String, String> params) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Build2> query = builder.createQuery(Build2.class);
		Root<Build2> root = query.from(Build2.class);
		
		List<Predicate> restrictions = new ArrayList<>();
		restrictions.add(builder.equal(root.get("project"), project));
		restrictions.add(builder.equal(root.get("commitHash"), commitHash));
		restrictions.add(builder.equal(root.get("jobName"), jobName));
		
		for (Map.Entry<String, String> entry: params.entrySet()) {
			Join<?, ?> join = root.join("params", JoinType.INNER);
			restrictions.add(builder.equal(join.get("name"), entry.getKey()));
			restrictions.add(builder.equal(join.get("value"), entry.getValue()));
		}
		return getSession().createQuery(query.where(restrictions.toArray(new Predicate[0]))).list();
	}

	@Sessional
	@Override
	public Build2 find(String runInstanceId) {
		EntityCriteria<Build2> criteria = newCriteria();
		criteria.add(Restrictions.eq("runInstanceId", runInstanceId));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public List<Build2> queryUnfinished() {
		EntityCriteria<Build2> criteria = newCriteria();
		criteria.add(Restrictions.or(
				Restrictions.eq("status", Status.PENDING), 
				Restrictions.eq("status", Status.RUNNING), 
				Restrictions.eq("status", Status.WAITING)));
		criteria.setCacheable(true);
		return query(criteria);
	}

	@Transactional
	@Override
	public void create(Build2 build) {
		Preconditions.checkArgument(build.isNew());
		Query<?> query = getSession().createQuery("select max(number) from Build2 where project=:project");
		query.setParameter("project", build.getProject());
		build.setNumber(getNextNumber(build.getProject(), query));
		save(build);

		for (BuildParam param: build.getParams())
			buildParamManager.save(param);
		for (BuildDependence dependence: build.getDependencies())
			buildDependenceManager.save(dependence);
	}

}
