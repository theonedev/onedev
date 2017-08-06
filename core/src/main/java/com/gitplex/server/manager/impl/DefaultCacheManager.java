package com.gitplex.server.manager.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.model.GroupAuthorization;
import com.gitplex.server.model.Membership;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityPersisted;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.util.facade.GroupAuthorizationFacade;
import com.gitplex.server.util.facade.GroupFacade;
import com.gitplex.server.util.facade.MembershipFacade;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserAuthorizationFacade;
import com.gitplex.server.util.facade.UserFacade;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
	
	private final Map<Long, GroupFacade> groups = new HashMap<>();
	
	private final BiMap<String, Long> groupIdsByName = HashBiMap.create();
	
	private final ReadWriteLock groupsLock = new ReentrantReadWriteLock();
	
	private final Map<Long, MembershipFacade> memberships = new HashMap<>();
	
	private final ReadWriteLock membershipsLock = new ReentrantReadWriteLock();
	
	private final Map<Long, GroupAuthorizationFacade> groupAuthorizations = new HashMap<>(); 
	
	private final ReadWriteLock groupAuthorizationsLock = new ReentrantReadWriteLock();
	
	private final Map<Long, UserAuthorizationFacade> userAuthorizations = new HashMap<>();
	
	private final ReadWriteLock userAuthorizationsLock = new ReentrantReadWriteLock();

	@Inject
	public DefaultCacheManager(Dao dao) {
		this.dao = dao;
	}
	
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching meta data...");
		for (Project project: dao.findAll(Project.class)) {
			projects.put(project.getId(), project.getFacade());
			projectIdsByName.inverse().put(project.getId(), project.getName());
		}
		for (User user: dao.findAll(User.class)) {
			users.put(user.getId(), user.getFacade());
			userIdsByName.inverse().put(user.getId(), user.getName());
			if (user.getEmail() != null)
				userIdsByEmail.inverse().put(user.getId(), user.getEmail());
		}
		for (Group group: dao.findAll(Group.class)) {
			groups.put(group.getId(), group.getFacade());
			groupIdsByName.inverse().put(group.getId(), group.getName());
		}
		for (Membership membership: dao.findAll(Membership.class))
			memberships.put(membership.getId(), membership.getFacade());
		for (GroupAuthorization groupAuthorization: dao.findAll(GroupAuthorization.class))
			groupAuthorizations.put(groupAuthorization.getId(), groupAuthorization.getFacade());
		for (UserAuthorization userAuthorization: dao.findAll(UserAuthorization.class))
			userAuthorizations.put(userAuthorization.getId(), userAuthorization.getFacade());
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				if (event.getEntity() instanceof Project) {
					projectsLock.writeLock().lock();
					try {
						ProjectFacade project = ((Project) event.getEntity()).getFacade();
						projects.put(event.getEntity().getId(), project);
						projectIdsByName.inverse().put(project.getId(), project.getName());
					} finally {
						projectsLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof User) {
					usersLock.writeLock().lock();
					try {
						UserFacade user = ((User) event.getEntity()).getFacade();
						users.put(event.getEntity().getId(), user);
						userIdsByName.inverse().put(user.getId(), user.getName());
						if (user.getEmail() != null)
							userIdsByEmail.inverse().put(user.getId(), user.getEmail());
					} finally {
						usersLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof Group) {
					groupsLock.writeLock().lock();
					try {
						GroupFacade group = ((Group) event.getEntity()).getFacade();
						groups.put(event.getEntity().getId(), group);
						groupIdsByName.inverse().put(group.getId(), group.getName());
					} finally {
						groupsLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof Membership) {
					membershipsLock.writeLock().lock();
					try {
						memberships.put(event.getEntity().getId(), ((Membership) event.getEntity()).getFacade());
					} finally {
						membershipsLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof UserAuthorization) {
					userAuthorizationsLock.writeLock().lock();
					try {
						userAuthorizations.put(event.getEntity().getId(), 
								((UserAuthorization) event.getEntity()).getFacade());
					} finally {
						userAuthorizationsLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof GroupAuthorization) {
					groupAuthorizationsLock.writeLock().lock();
					try {
						groupAuthorizations.put(event.getEntity().getId(), 
								((GroupAuthorization) event.getEntity()).getFacade());
					} finally {
						groupAuthorizationsLock.writeLock().unlock();
					}
				}
			}
			
		});
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				if (event.getEntity() instanceof Project) {
					projectsLock.writeLock().lock();
					try {
						projects.remove(event.getEntity().getId());
						projectIdsByName.inverse().remove(event.getEntity().getId());
					} finally {
						projectsLock.writeLock().unlock();
					}
					userAuthorizationsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, UserAuthorizationFacade>> it = userAuthorizations.entrySet().iterator(); it.hasNext();) {
							if (it.next().getValue().getProjectId().equals(event.getEntity().getId()))
								it.remove();
						}
					} finally {
						userAuthorizationsLock.writeLock().unlock();
					}
					groupAuthorizationsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, GroupAuthorizationFacade>> it = groupAuthorizations.entrySet().iterator(); it.hasNext();) {
							if (it.next().getValue().getProjectId().equals(event.getEntity().getId()))
								it.remove();
						}
					} finally {
						groupAuthorizationsLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof User) {
					usersLock.writeLock().lock();
					try {
						users.remove(event.getEntity().getId());
						userIdsByName.inverse().remove(event.getEntity().getId());
						userIdsByEmail.inverse().remove(event.getEntity().getId());
					} finally {
						usersLock.writeLock().unlock();
					}
					userAuthorizationsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, UserAuthorizationFacade>> it = userAuthorizations.entrySet().iterator(); it.hasNext();) {
							if (it.next().getValue().getUserId().equals(event.getEntity().getId()))
								it.remove();
						}
					} finally {
						userAuthorizationsLock.writeLock().unlock();
					}
					membershipsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, MembershipFacade>> it = memberships.entrySet().iterator(); it.hasNext();) {
							if (it.next().getValue().getUserId().equals(event.getEntity().getId()))
								it.remove();
						}
					} finally {
						membershipsLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof Group) {
					groupsLock.writeLock().lock();
					try {
						groups.remove(event.getEntity().getId());
						groupIdsByName.inverse().remove(event.getEntity().getId());
					} finally {
						groupsLock.writeLock().unlock();
					}
					groupAuthorizationsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, GroupAuthorizationFacade>> it = groupAuthorizations.entrySet().iterator(); it.hasNext();) {
							if (it.next().getValue().getGroupId().equals(event.getEntity().getId()))
								it.remove();
						}
					} finally {
						groupAuthorizationsLock.writeLock().unlock();
					}
					membershipsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, MembershipFacade>> it = memberships.entrySet().iterator(); it.hasNext();) {
							if (it.next().getValue().getGroupId().equals(event.getEntity().getId()))
								it.remove();
						}
					} finally {
						membershipsLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof Membership) {
					membershipsLock.writeLock().lock();
					try {
						memberships.remove(event.getEntity().getId());
					} finally {
						membershipsLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof UserAuthorization) {
					userAuthorizationsLock.writeLock().lock();
					try {
						userAuthorizations.remove(event.getEntity().getId());
					} finally {
						userAuthorizationsLock.writeLock().unlock();
					}
				} else if (event.getEntity() instanceof GroupAuthorization) {
					groupAuthorizationsLock.writeLock().lock();
					try {
						groupAuthorizations.remove(event.getEntity().getId());
					} finally {
						groupAuthorizationsLock.writeLock().unlock();
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
	public Map<Long, GroupFacade> getGroups() {
		groupsLock.readLock().lock();
		try {
			return new HashMap<>(groups);
		} finally {
			groupsLock.readLock().unlock();
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
	public Map<Long, UserAuthorizationFacade> getUserAuthorizations() {
		userAuthorizationsLock.readLock().lock();
		try {
			return new HashMap<>(userAuthorizations);
		} finally {
			userAuthorizationsLock.readLock().unlock();
		}
	}

	@Override
	public Map<Long, GroupAuthorizationFacade> getGroupAuthorizations() {
		groupAuthorizationsLock.readLock().lock();
		try {
			return new HashMap<>(groupAuthorizations);
		} finally {
			groupAuthorizationsLock.readLock().unlock();
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
	public Long getGroupIdByName(String name) {
		groupsLock.readLock().lock();
		try {
			return groupIdsByName.get(name);
		} finally {
			groupsLock.readLock().unlock();
		}
	}
	
	@Override
	public GroupFacade getGroup(Long id) {
		groupsLock.readLock().lock();
		try {
			return groups.get(id);
		} finally {
			groupsLock.readLock().unlock();
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

	@Override
	public UserAuthorizationFacade getUserAuthorization(Long id) {
		userAuthorizationsLock.readLock().lock();
		try {
			return userAuthorizations.get(id);
		} finally {
			userAuthorizationsLock.readLock().unlock();
		}
	}

	@Override
	public GroupAuthorizationFacade getGroupAuthorization(Long id) {
		groupAuthorizationsLock.readLock().lock();
		try {
			return groupAuthorizations.get(id);
		} finally {
			groupAuthorizationsLock.readLock().unlock();
		}
	}

}
