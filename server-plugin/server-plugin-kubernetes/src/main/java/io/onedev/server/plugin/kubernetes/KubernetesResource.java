package io.onedev.server.plugin.kubernetes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TarUtils;
import io.onedev.server.OneException;
import io.onedev.server.ci.job.JobManager;
import io.onedev.server.model.support.JobContext;

@Path("/k8s")
@Consumes(MediaType.WILDCARD)
@Singleton
public class KubernetesResource {

	public static final String TEST_JOB_TOKEN = UUID.randomUUID().toString();
	
	private final JobManager jobManager;
	
    @Context
    private HttpServletRequest request;
    
    @Inject
    public KubernetesResource(JobManager jobManager) {
    	this.jobManager = jobManager;
	}
    
	@Path("/job-context")
	@Produces(MediaType.APPLICATION_JSON)
    @GET
    public Map<String, Object> getJobContextAsMap() {
		JobContext context = getJobContext();
		Map<String, Object> contextMap = new HashMap<>();
		contextMap.put("commands", context.getCommands());
		contextMap.put("cloneSource", context.isCloneSource());
		contextMap.put("projectName", context.getProjectName());
		contextMap.put("commitHash", context.getCommitId().name());
		contextMap.put("collectFiles.includes", context.getCollectFiles().getIncludes());
		contextMap.put("collectFiles.excludes", context.getCollectFiles().getExcludes());
		
		return contextMap;
    }
	
	@Path("/download-dependencies")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadDependencies() {
		StreamingOutput os = new StreamingOutput() {

			@Override
		   public void write(OutputStream output) throws IOException {
				JobContext context = getJobContext();
				TarUtils.tar(context.getServerWorkspace(), Lists.newArrayList("**"), new ArrayList<>(), output);
				output.flush();
		   }				   
		   
		};
		return Response.ok(os).build();
	}
	
	@POST
	@Path("/upload-outcomes")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)	
	public Response uploadOutcomes(InputStream is) {
		JobContext context = getJobContext();
		TarUtils.untar(is, context.getServerWorkspace());
		return Response.ok().build();
	}
	
	@GET
	@Path("/test")
	public Response test() {
		String jobToken = request.getHeader(JobManager.JOB_TOKEN_HTTP_HEADER);
		if (TEST_JOB_TOKEN.equals(jobToken))
			return Response.ok().build();
		else
			return Response.status(400).entity("Invalid or no job token").build();
	}
	
	private JobContext getJobContext() {
		String jobToken = request.getHeader(JobManager.JOB_TOKEN_HTTP_HEADER);
		if (jobToken == null)
			throw new OneException("Http header '" + JobManager.JOB_TOKEN_HTTP_HEADER + "' is expected");
		JobContext context = jobManager.getJobContext(jobToken);
		if (context == null) 
			throw new OneException("No job context found for specified job token");
		return context;
	}
	
}
