package io.onedev.server.manager.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.lifecycle.SystemStarted;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityPersisted;
import io.onedev.server.persistence.dao.EntityRemoved;
import io.onedev.server.util.facade.EntityFacade;
import io.onedev.server.util.facade.MembershipFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.utils.Pair;

@Singleton
public class DefaultCacheManager implements CacheManager {

	private final Logger logger = LoggerFactory.getLogger(DefaultCacheManager.class);
	
	private final Dao dao;
	
	private final Map<Long, UserFacade> users = new HashMap<>();
	
	private final BiMap<String, Long> userIdsByName = HashBiMap.create();
	
	private final BiMap<String, Long> userIdsByEmail = HashBiMap.create();
	
	private final ReadWriteLock usersLock = new ReentrantReadWriteLock();
	
	private final Map<Long, ProjectFacade> projects = new HashMap<>();
	
	private final BiMap<String, Long> projectIdsByName = HashBiMap.create();
	
	private final ReadWriteLock projectsLock = new ReentrantReadWriteLock();
	
	private final Map<Long, TeamFacade> teams = new HashMap<>();
	
	private final BiMap<Pair<Long, String>, Long> teamIds = HashBiMap.create();
	
	private final ReadWriteLock teamsLock = new ReentrantReadWriteLock();
	
	private final Map<Long, MembershipFacade> memberships = new HashMap<>();
	
	private final ReadWriteLock membershipsLock = new ReentrantReadWriteLock();

	@Inject
	public DefaultCacheManager(Dao dao) {
		this.dao = dao;
	}
	
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching meta data...");
		for (Project project: dao.query(Project.class)) {
			projects.put(project.getId(), project.getFacade());
			projectIdsByName.inverse().put(project.getId(), project.getName());
		}
		for (User user: dao.query(User.class)) {
			users.put(user.getId(), user.getFacade());
			userIdsByName.inverse().put(user.getId(), user.getName());
			if (user.getEmail() != null)
				userIdsByEmail.inverse().put(user.getId(), user.getEmail());
		}
		for (Team team: dao.query(Team.class)) {
			teams.put(team.getId(), team.getFacade());
			teamIds.inverse().put(team.getId(), new Pair<>(team.getProject().getId(), team.getName()));
		}
		for (Membership membership: dao.query(Membership.class))
			memberships.put(membership.getId(), membership.getFacade());
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		EntityFacade facade;
		
		if (event.getEntity() instanceof Project) {
			facade = ((Project) event.getEntity()).getFacade();
		} else if (event.getEntity() instanceof User) {
			facade = ((User) event.getEntity()).getFacade();
		} else if (event.getEntity() instanceof Team) {
			facade = ((Team) event.getEntity()).getFacade();
		} else if (event.getEntity() instanceof Membership) {
			facade = ((Membership) event.getEntity()).getFacade();
		} else {
			facade = null;
		}
		
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				if (facade instanceof ProjectFacade) {
					ProjectFacade project = (ProjectFacade) facade;
					projectsLock.writeLock().lock();
					try {
						projects.put(project.getId(), project);
						projectIdsByName.inverse().put(project.getId(), project.getName());
					} finally {
						projectsLock.writeLock().unlock();
					}
				} else if (facade instanceof UserFacade) {
					UserFacade user = (UserFacade) facade;
					usersLock.writeLock().lock();
					try {
						users.put(user.getId(), user);
						userIdsByName.inverse().put(user.getId(), user.getName());
						if (user.getEmail() != null)
							userIdsByEmail.inverse().put(user.getId(), user.getEmail());
					} finally {
						usersLock.writeLock().unlock();
					}
				} else if (facade instanceof TeamFacade) {
					TeamFacade team = (TeamFacade) facade;
					teamsLock.writeLock().lock();
					try {
						teams.put(team.getId(), team);
						teamIds.inverse().put(team.getId(), new Pair<>(team.getProjectId(), team.getName()));
					} finally {
						teamsLock.writeLock().unlock();
					}
				} else if (facade instanceof MembershipFacade) {
					MembershipFacade membership = (MembershipFacade) facade;
					membershipsLock.writeLock().lock();
					try {
						memberships.put(membership.getId(), membership);
					} finally {
						membershipsLock.writeLock().unlock();
					}
				} 
			}
			
		});
		
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		Long id = event.getEntity().getId();
		Class<?> clazz = event.getEntity().getClass();
		
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				if (Project.class.isAssignableFrom(clazz)) {
					projectsLock.writeLock().lock();
					try {
						projects.remove(id);
						projectIdsByName.inverse().remove(id);
					} finally {
						projectsLock.writeLock().unlock();
					}
					teamsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, TeamFacade>> it = teams.entrySet().iterator(); it.hasNext();) {
							TeamFacade team = it.next().getValue();
							if (team.getProjectId().equals(id)) {
								it.remove();
								teamIds.inverse().remove(team.getId());
								membershipsLock.writeLock().lock();
								try {
									for (Iterator<Map.Entry<Long, MembershipFacade>> it2 = memberships.entrySet().iterator(); it2.hasNext();) {
										if (it2.next().getValue().getTeamId().equals(team.getId()))
											it2.remove();
									}
								} finally {
									membershipsLock.writeLock().unlock();
								}
							}
						}
					} finally {
						teamsLock.writeLock().unlock();
					}
				} else if (User.class.isAssignableFrom(clazz)) {
					usersLock.writeLock().lock();
					try {
						users.remove(id);
						userIdsByName.inverse().remove(id);
						userIdsByEmail.inverse().remove(id);
					} finally {
						usersLock.writeLock().unlock();
					}
					membershipsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, MembershipFacade>> it = memberships.entrySet().iterator(); it.hasNext();) {
							if (it.next().getValue().getUserId().equals(id))
								it.remove();
						}
					} finally {
						membershipsLock.writeLock().unlock();
					}
				} else if (Team.class.isAssignableFrom(clazz)) {
					teamsLock.writeLock().lock();
					try {
						teams.remove(id);
						teamIds.inverse().remove(id);
					} finally {
						teamsLock.writeLock().unlock();
					}
					membershipsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, MembershipFacade>> it = memberships.entrySet().iterator(); it.hasNext();) {
							if (it.next().getValue().getTeamId().equals(id))
								it.remove();
						}
					} finally {
						membershipsLock.writeLock().unlock();
					}
				} else if (Membership.class.isAssignableFrom(clazz)) {
					membershipsLock.writeLock().lock();
					try {
						memberships.remove(id);
					} finally {
						membershipsLock.writeLock().unlock();
					}
				}
			}
			
		});
	}

	@Override
	public Map<Long, ProjectFacade> getProjects() {
		projectsLock.readLock().lock();
		try {
			return new HashMap<>(projects);
		} finally {
			projectsLock.readLock().unlock();
		}
	}

	@Override
	public Map<Long, UserFacade> getUsers() {
		usersLock.readLock().lock();
		try {
			return new HashMap<>(users);
		} finally {
			usersLock.readLock().unlock();
		}
	}

	@Override
	public Map<Long, TeamFacade> getTeams() {
		teamsLock.readLock().lock();
		try {
			return new HashMap<>(teams);
		} finally {
			teamsLock.readLock().unlock();
		}
	}

	@Override
	public Map<Long, MembershipFacade> getMemberships() {
		membershipsLock.readLock().lock();
		try {
			return new HashMap<>(memberships);
		} finally {
			membershipsLock.readLock().unlock();
		}
	}

	@Override
	public ProjectFacade getProject(Long id) {
		projectsLock.readLock().lock();
		try {
			return projects.get(id);
		} finally {
			projectsLock.readLock().unlock();
		}
	}

	@Override
	public UserFacade getUser(Long id) {
		usersLock.readLock().lock();
		try {
			return users.get(id);
		} finally {
			usersLock.readLock().unlock();
		}
	}
	
	@Override
	public Long getUserIdByName(String name) {
		usersLock.readLock().lock();
		try {
			return userIdsByName.get(name);
		} finally {
			usersLock.readLock().unlock();
		}
	}

	@Override
	public Long getUserIdByEmail(String email) {
		usersLock.readLock().lock();
		try {
			return userIdsByEmail.get(email);
		} finally {
			usersLock.readLock().unlock();
		}
	}
	
	@Override
	public Long getProjectIdByName(String name) {
		projectsLock.readLock().lock();
		try {
			return projectIdsByName.get(name);
		} finally {
			projectsLock.readLock().unlock();
		}
	}
	
	@Override
	public Map<String, Long> getProjectIds() {
		projectsLock.readLock().lock();
		try {
			return new HashMap<>(projectIdsByName);
		} finally {
			projectsLock.readLock().unlock();
		}
	}
	
	@Override
	public Long getTeamId(Long projectId, String name) {
		teamsLock.readLock().lock();
		try {
			return teamIds.get(new Pair<>(projectId, name));
		} finally {
			teamsLock.readLock().unlock();
		}
	}
	
	@Override
	public TeamFacade getTeam(Long id) {
		teamsLock.readLock().lock();
		try {
			return teams.get(id);
		} finally {
			teamsLock.readLock().unlock();
		}
	}

	@Override
	public MembershipFacade getMembership(Long id) {
		membershipsLock.readLock().lock();
		try {
			return memberships.get(id);
		} finally {
			membershipsLock.readLock().unlock();
		}
	}

}
