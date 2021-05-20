package io.onedev.server.plugin.executor.kubernetes;

import static io.onedev.k8shelper.KubernetesHelper.readInt;
import static io.onedev.k8shelper.KubernetesHelper.readString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
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

import org.apache.commons.lang.SerializationUtils;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.k8shelper.CacheAllocationRequest;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.k8shelper.ServerExecutionResult;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.util.ExceptionUtils;
import io.onedev.server.util.SimpleLogger;

@Api(exclude=true)
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
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
    @POST
    public byte[] getJobContext(@Nullable String jobWorkspace) {
		JobContext context = jobManager.getJobContext(getJobToken(), true);
		if (StringUtils.isNotBlank(jobWorkspace))
			context.reportJobWorkspace(jobWorkspace);		
		Map<String, Object> contextMap = new HashMap<>();
		contextMap.put("actions", context.getActions());
		contextMap.put("projectName", context.getProjectName());
		contextMap.put("commitHash", context.getCommitId().name());
		return SerializationUtils.serialize((Serializable) contextMap);
    }
	
	@Path("/allocate-job-caches")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
    @POST
    public byte[] allocateJobCaches(String cacheAllocationRequestString) {
		CacheAllocationRequest cacheAllocationRequest = CacheAllocationRequest.fromString(cacheAllocationRequestString);
		return SerializationUtils.serialize((Serializable) jobManager.allocateJobCaches(
				getJobToken(), cacheAllocationRequest.getCurrentTime(), cacheAllocationRequest.getInstances()));
    }
	
	@Path("/report-job-caches")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public void reportJobCaches(String cacheInstancesString) {
		Collection<CacheInstance> cacheInstances = new ArrayList<>();
		for (String field: Splitter.on(';').omitEmptyStrings().split(cacheInstancesString))
			cacheInstances.add(CacheInstance.fromString(field));
		jobManager.reportJobCaches(getJobToken(), cacheInstances);
	}
	
	@Path("/run-server-step")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response runServerStep(InputStream is) {
		File filesDir = FileUtils.createTempDir();
		try {
			int length = readInt(is);
			List<Integer> stepPosition = new ArrayList<>();
			for (int i=0; i<length; i++) 
				stepPosition.add(readInt(is));
			
			Map<String, String> placeholderValues = new HashMap<>();
			length = readInt(is);
			for (int i=0; i<length; i++) 
				placeholderValues.put(readString(is), readString(is));
			
			TarUtils.untar(is, filesDir);
			
			List<String> logMessages = new ArrayList<>();
			Map<String, byte[]> outputFiles = jobManager.runServerStep(getJobToken(), stepPosition, 
					filesDir, placeholderValues, new SimpleLogger() {

				@Override
				public void log(String message) {
					logMessages.add(message);
				}
				
			});
			
			ServerExecutionResult result = new ServerExecutionResult(logMessages, outputFiles);
			return Response.ok(SerializationUtils.serialize(result)).build();
		} catch (Exception e) {
			String errorMessage = ExceptionUtils.getExpectedError(e);
			if (errorMessage == null)
				errorMessage = Throwables.getStackTraceAsString(e);
			return Response.serverError().entity(errorMessage).build();
		} finally {
			FileUtils.deleteDir(filesDir);
		}
	}
	
	@Path("/download-dependencies")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadDependencies() {
		StreamingOutput os = new StreamingOutput() {

			@Override
		   public void write(OutputStream output) throws IOException {
				JobContext context = jobManager.getJobContext(getJobToken(), true);
				File tempDir = FileUtils.createTempDir();
				try {
					context.copyDependencies(tempDir);
					TarUtils.tar(tempDir, Lists.newArrayList("**"), new ArrayList<>(), output);
					output.flush();
				} finally {
					FileUtils.deleteDir(tempDir);
				}
		   }				   
		   
		};
		return Response.ok(os).build();
	}
	 
	@GET
	@Path("/test")
	public Response test() {
		String jobToken = Job.getToken(request);
		if (TEST_JOB_TOKEN.equals(jobToken))
			return Response.ok().build();
		else 
			return Response.status(400).entity("Invalid or missing job token").build();
	}
	
	private String getJobToken() {
		String jobToken = Job.getToken(request);
		if (jobToken != null)
			return jobToken;
		else
			throw new ExplicitException("Job token is expected");
	}
	
}
