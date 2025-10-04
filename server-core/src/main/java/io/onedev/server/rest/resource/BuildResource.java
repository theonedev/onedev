package io.onedev.server.rest.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.BuildService;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildLabel;
import io.onedev.server.model.BuildParam;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.security.SecurityUtils;

@Api(description="In most cases, build resource is operated with build id, which is different from build number. "
		+ "To get build id of a particular build number, use the <a href='/~help/api/io.onedev.server.rest.BuildResource/queryBasicInfo'>Query Basic Info</a> operation with query for "
		+ "instance <code>&quot;Number&quot; is &quot;path/to/project#100&quot;</code> or <code>&quot;Number&quot; is &quot;PROJECTKEY-100&quot;</code>")
@Path("/builds")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class BuildResource {

	private final BuildService buildService;
	
	private final AuditService auditService;
	
	@Inject
	public BuildResource(BuildService buildService, AuditService auditService) {
		this.buildService = buildService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{buildId}")
    @GET
    public Build getBuild(@PathParam("buildId") Long buildId) {
		Build build = buildService.load(buildId);
    	if (!SecurityUtils.canAccessBuild(build)) 
			throw new UnauthorizedException();
    	return build;
    }

	@Api(order=150, description = "Get list of <a href='/~help/api/io.onedev.server.rest.BuildLabelResource'>labels</a>")
	@Path("/{buildId}/labels")
	@GET
	public Collection<BuildLabel> getLabels(@PathParam("buildId") Long buildId) {
		Build build = buildService.load(buildId);
		if (!SecurityUtils.canAccessBuild(build))
			throw new UnauthorizedException();
		return build.getLabels();
	}
	
	@Api(order=200)
	@Path("/{buildId}/params")
    @GET
    public Collection<BuildParam> getParams(@PathParam("buildId") Long buildId) {
		Build build = buildService.load(buildId);
    	if (!SecurityUtils.canAccessBuild(build)) 
			throw new UnauthorizedException();
    	
    	List<BuildParam> params = SerializationUtils.clone(new ArrayList<>(build.getParams()));
    	for (Iterator<BuildParam> it = params.iterator(); it.hasNext();) {
    		BuildParam param = it.next();
    		if (param.getType().equals(ParamSpec.SECRET))
    			param.setValue(SecretInput.MASK);
    	}
    	return params;
    }
	
	@Api(order=300)
	@Path("/{buildId}/dependencies")
    @GET
    public Collection<BuildDependence> getDependencies(@PathParam("buildId") Long buildId) {
		Build build = buildService.load(buildId);
    	if (!SecurityUtils.canAccessBuild(build)) 
			throw new UnauthorizedException();
    	return build.getDependencies();
    }
	
	@Api(order=400)
	@Path("/{buildId}/dependents")
    @GET
    public Collection<BuildDependence> getDependents(@PathParam("buildId") Long buildId) {
		Build build = buildService.load(buildId);
    	if (!SecurityUtils.canAccessBuild(build)) 
			throw new UnauthorizedException();
    	return build.getDependents();
    }
	
	@Api(order=500)
	@Path("/{buildId}/fixed-issue-ids")
    @GET
    public Collection<Long> getFixedIssueIds(@PathParam("buildId") Long buildId) {
		Build build = buildService.load(buildId);
    	if (!SecurityUtils.canAccessBuild(build)) 
			throw new UnauthorizedException();
    	return build.getFixedIssueIds();
    }
	
	@Api(order=600)
	@GET
    public List<Build> queryBuilds(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as in <a href='/~builds'>builds page</a>", example="\"Job\" is \"Release\"") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {

		var subject = SecurityUtils.getSubject();
		if (!SecurityUtils.isAdministrator(subject) && count > RestConstants.MAX_PAGE_SIZE)
    		throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

    	BuildQuery parsedQuery;
		try {
			parsedQuery = BuildQuery.parse(null, query, true, true);
		} catch (Exception e) {
			throw new NotAcceptableException("Error parsing query", e);
		}
    	
    	return buildService.query(subject, null, parsedQuery, false, offset, count);
    }

	@Api(order = 650)
	@Path("/{buildId}/description")
	@POST
	public Response setDescription(@PathParam("buildId") Long buildId, String description) {
		Build build = buildService.load(buildId);
		if (!SecurityUtils.canManageBuild(build))
			throw new UnauthorizedException();
		var oldDescription = build.getDescription();
		if (!Objects.equals(oldDescription, description)) {
			build.setDescription(description);
			buildService.update(build);
			auditService.audit(build.getProject(), "updated description of build \"" + build.getReference().toString(build.getProject()) + "\" via RESTful API", oldDescription, description);
		}
		return Response.ok().build();
	}
	
	@Api(order=700)
	@Path("/{buildId}")
    @DELETE
    public Response deleteBuild(@PathParam("buildId") Long buildId) {
    	Build build = buildService.load(buildId);
    	if (!SecurityUtils.canManageBuild(build))
			throw new UnauthorizedException();
    	buildService.delete(build);
		var oldAuditContent = VersionedXmlDoc.fromBean(build).toXML();
		auditService.audit(build.getProject(), "deleted build \"" + build.getReference().toString(build.getProject()) + "\" via RESTful API", oldAuditContent, null);
    	return Response.ok().build();
    }
	
}
