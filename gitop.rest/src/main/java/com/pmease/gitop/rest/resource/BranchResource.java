package com.pmease.gitop.rest.resource;

import java.util.Collection;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/branches")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BranchResource {

	private final GeneralDao generalDao;
	
	@Inject
	public BranchResource(GeneralDao generalDao) {
		this.generalDao = generalDao;
	}
	
    @GET
    @Path("/{branchId}")
    public Branch get(@PathParam("branchId") Long branchId) {
    	Branch branch = generalDao.load(Branch.class, branchId);
    	if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(branch.getRepository())))
    		throw new UnauthorizedException();
    	return branch;
    }
    
    @SuppressWarnings("unchecked")
	@GET
    public Collection<Branch> query(
    		@QueryParam("userName") String userName,
    		@QueryParam("userId") Long userId,
    		@QueryParam("repositoryId") Long repositoryId,
    		@QueryParam("repositoryName") String repositoryName, 
    		@QueryParam("branchName") String branchName) {
    	Collection<Branch> branches;
    	if (repositoryId != null) {
    		if (userName != null || userId != null || repositoryName != null) {
    			throw new IllegalArgumentException("Parameter userName, userId, or repositoryName should not be specified "
    					+ "if repositoryId is specified.");
    		} else {
    			Repository repository = generalDao.load(Repository.class, repositoryId);
    			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(repository)))
    				throw new UnauthorizedException();
    			
    			if (branchName != null) {
    				DetachedCriteria criteria = DetachedCriteria.forClass(Branch.class);
    				criteria.add(Restrictions.eq("repository", repository));
    				criteria.add(Restrictions.eq("name", branchName));
    				branches = (Collection<Branch>) generalDao.query(criteria, 0, 0);
    			} else {
    				branches = repository.getBranches();
    			}
    		}
    	} else if (userId != null) {
    		if (userName != null) 
    			throw new IllegalArgumentException("Parameter userName should not be specified if userId is specified.");
    		User user = generalDao.load(User.class, userId);
    		
    		if (repositoryName != null) {
    			DetachedCriteria criteria = DetachedCriteria.forClass(Repository.class);
    			criteria.add(Restrictions.eq("user", user));
    			criteria.add(Restrictions.eq("name", repositoryName));
    			Repository repository = (Repository) generalDao.find(criteria);
    			if (repository == null)
    				throw new NotFoundException("Unable to find repository " + repositoryName + " under user " + user.getName());

    			if (branchName != null) {
    				criteria = DetachedCriteria.forClass(Branch.class);
    				criteria.add(Restrictions.eq("repository", repository));
    				criteria.add(Restrictions.eq("name", branchName));
    				branches = (Collection<Branch>) generalDao.query(criteria, 0, 0);
    			} else {
    				branches = repository.getBranches();
    			}
    		} else if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserRead(user))) {
				throw new UnauthorizedException();
    		} else {
    			DetachedCriteria criteria = DetachedCriteria.forClass(Branch.class);
    			criteria.createCriteria("repository").add(Restrictions.eq("owner", user));
    			
    			if (branchName != null)
    				criteria.add(Restrictions.eq("name", branchName));
    			branches = (Collection<Branch>) generalDao.query(criteria, 0, 0);
    		}
    	} else {
			DetachedCriteria criteria = DetachedCriteria.forClass(Branch.class);
			if (branchName != null) 
				criteria.add(Restrictions.eq("name", branchName));
			DetachedCriteria repositoryCriteria = criteria.createCriteria("repository");
			if (repositoryName != null) 
				repositoryCriteria.add(Restrictions.eq("name", repositoryName));
			if (userName != null)
				repositoryCriteria.createCriteria("owner").add(Restrictions.eq("name", userName));
			
			branches = (Collection<Branch>) generalDao.query(criteria, 0, 0);
    	}
    	
    	for (Branch branch: branches) {
    		if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(branch.getRepository())))
    			throw new UnauthorizedException();
    	}
    	
    	return branches;
    }
    
    @POST
    public Long save(@Valid Branch branch) {
    	if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(branch.getRepository())))
    		generalDao.save(branch);
    	
    	return branch.getId();
    }
    
}
