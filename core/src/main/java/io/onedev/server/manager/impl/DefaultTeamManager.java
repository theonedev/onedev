package io.onedev.server.manager.impl;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.utils.StringUtils;

@Singleton
public class DefaultTeamManager extends AbstractEntityManager<Team> implements TeamManager {

	private final ProjectManager projectManager;
	
	private final CacheManager cacheManager;
	
	private final IssueFieldUnaryManager issueFieldManager;
	
	@Inject
	public DefaultTeamManager(Dao dao, ProjectManager projectManager, 
			CacheManager cacheManager, IssueFieldUnaryManager issueFieldManager) {
		super(dao);
		this.projectManager = projectManager;
		this.cacheManager = cacheManager;
		this.issueFieldManager = issueFieldManager;
	}

	@Transactional
	@Override
	public void save(Team team, String oldName) {
		if (oldName != null && !oldName.equals(team.getName())) {
			for (Project project: projectManager.query()) {
				for (BranchProtection protection: project.getBranchProtections()) 
					protection.onRenameTeam(project, oldName, team.getName());
				for (TagProtection protection: project.getTagProtections())
					protection.onRenameGroup(oldName, team.getName());
				project.getIssueWorkflow().onRenameGroup(oldName, team.getName());
			}
			
			issueFieldManager.onRenameGroup(oldName, team.getName());
		}
		dao.persist(team);
	}

	@Transactional
	@Override
	public void delete(Team group) {
		for (Project project: projectManager.query()) {
			for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext();) { 
				if (it.next().onDeleteTeam(project, group.getName()))
					it.remove();
			}
			for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext();) {
				if (it.next().onDeleteGroup(group.getName()))
					it.remove();
			}
			project.getIssueWorkflow().onDeleteGroup(group.getName());
		}
		
		dao.remove(group);
	}

	@Sessional
	@Override
	public Team find(Project project, String name) {
		Long id = cacheManager.getTeamId(project.getId(), name);
		if (id != null) 
			return load(id);
		else
			return null;
	}

	@Sessional
	@Override
	public Team find(String projectName, String teamName) {
		Project project = projectManager.find(projectName);
		if (project != null)
			return find(project, teamName);
		else
			return null;
	}
	
	@Sessional
	@Override
	public Team find(String teamFQN) {
		return find(StringUtils.substringBefore(teamFQN, Team.FQN_SEPARATOR), 
				StringUtils.substringAfter(teamFQN, Team.FQN_SEPARATOR));
	}

}
