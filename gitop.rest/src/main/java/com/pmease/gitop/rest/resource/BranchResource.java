package com.pmease.gitop.rest.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/branches")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public class BranchResource {

	private final BranchManager branchManager;
	
	@Inject
	public BranchResource(BranchManager branchManager) {
		this.branchManager = branchManager;
	}
	
    @GET
    @Path("/{branchId}")
    public Branch get(@PathParam("branchId") Long branchId) {
    	Branch branch = branchManager.load(branchId);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(branch.getRepository())))
    		throw new UnauthorizedException();
    	return branch;
    }
    
	@GET
    public Collection<Branch> query(
    		@Nullable @QueryParam("repositoryId") Long repositoryId,
    		@Nullable @QueryParam("name") String name) {

		List<Criterion> criterions = new ArrayList<>();
		if (repositoryId != null)
			criterions.add(Restrictions.eq("repository.id", repositoryId));
		if (name != null)
			criterions.add(Restrictions.eq("name", name));
		List<Branch> branches = branchManager.query(criterions.toArray(new Criterion[criterions.size()]));
		
    	for (Branch branch: branches) {
    		if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(branch.getRepository()))) {
    			throw new UnauthorizedException("Unauthorized access to branch " + branch);
    		}
    	}
    	
    	return branches;
    }

	@DELETE
	@Path("/{branchId}")
	public void delete(@PathParam("branchId") Long branchId) {
		Branch branch = branchManager.load(branchId);
		
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(branch.getRepository())))
    		branchManager.delete(branch);
	}
	
    @POST
    public Long save(@Valid Branch branch) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(branch.getRepository())))
    		branchManager.save(branch);
    	
    	return branch.getId();
    }
    
}
