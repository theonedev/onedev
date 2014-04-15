package com.pmease.gitop.core.manager.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.MembershipManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.operation.GeneralOperation;

@Singleton
public class DefaultUserManager extends AbstractGenericDao<User> implements UserManager {

    private volatile Long rootId;

    private final TeamManager teamManager;
    
    private final MembershipManager membershipManager;
    
    private final PullRequestManager pullRequestManager;
    
    private final PullRequestUpdateManager pullRequestUpdateManager;
    
    private final BranchManager branchManager;
    
    @Inject
    public DefaultUserManager(GeneralDao generalDao, TeamManager teamManager, 
    		MembershipManager membershipManager, PullRequestManager pullRequestManager, 
    		PullRequestUpdateManager pullRequestUpdateManager,BranchManager branchManager) {
        super(generalDao);

        this.teamManager = teamManager;
        this.membershipManager = membershipManager;
        this.pullRequestManager = pullRequestManager;
        this.pullRequestUpdateManager = pullRequestUpdateManager;
        this.branchManager = branchManager;
    }

    @Transactional
    @Override
	public void save(User user) {
    	boolean isNew = user.getId() == null;
    	super.save(user);
    	
    	if (isNew) {
        	Team team = new Team();
        	team.setOwner(user);
        	team.setAuthorizedOperation(GeneralOperation.NO_ACCESS);
        	team.setName(Team.ANONYMOUS);
        	teamManager.save(team);
        	
        	team = new Team();
        	team.setOwner(user);
        	team.setName(Team.LOGGEDIN);
        	team.setAuthorizedOperation(GeneralOperation.NO_ACCESS);
        	teamManager.save(team);
        	
        	team = new Team();
        	team.setOwner(user);
        	team.setName(Team.OWNERS);
        	team.setAuthorizedOperation(GeneralOperation.ADMIN);
        	teamManager.save(team);
        	
        	Membership membership = new Membership();
        	membership.setTeam(team);
        	membership.setUser(user);
        	membershipManager.save(membership);
    	}
    }
    
    @Sessional
    @Override
    public User getRoot() {
        User root;
        if (rootId == null) {
            // The first created user should be root user
            root = find(null, new Order[] {Order.asc("id")});
            Preconditions.checkNotNull(root);
            rootId = root.getId();
        } else {
            root = load(rootId);
        }
        return root;
    }

    @Transactional
    @Override
	public void delete(User user) {
    	for (PullRequest request: user.getSubmittedRequests()) {
    		request.setSubmittedBy(null);
    		pullRequestManager.save(request);
    	}
    	
    	for (PullRequest request: user.getClosedRequests()) {
    		request.getCloseInfo().setClosedBy(null);
    		pullRequestManager.save(request);
    	}
    	
    	for (PullRequestUpdate update: user.getUpdates()) {
    		update.setUser(null);
    		pullRequestUpdateManager.save(update);
    	}
    	
    	for (Branch branch: user.getBranches()) {
    		branch.setCreator(null);
    		branchManager.save(branch);
    	}
    	
		super.delete(user);
	}

	@Sessional
    @Override
    public User findByName(String userName) {
        return find(new Criterion[] {Restrictions.eq("name", userName)});
    }

    @Sessional
    @Override
    public User findByEmail(String email) {
        return find(new Criterion[] {Restrictions.eq("email", email)});
    }

    @Override
	public User getCurrent() {
		Long userId = User.getCurrentId();
		if (userId != 0L) {
			User user = get(userId);
			if (user != null)
				return user;
		}
		return null;
	}

	@Override
	public void trim(Collection<Long> userIds) {
		for (Iterator<Long> it = userIds.iterator(); it.hasNext();) {
			if (get(it.next()) == null)
				it.remove();
		}
	}

	@Override
	@Sessional
	public List<User> getManagableAccounts(User user) {
		Preconditions.checkNotNull(user);
		Collection<Membership> memberships = user.getMemberships();
		List<User> result = Lists.newArrayList();
		for (Membership each : memberships) {
			if (each.getTeam().isOwners()) {
				result.add(each.getTeam().getOwner());
			}
		}
		
		return result;
	}

}
