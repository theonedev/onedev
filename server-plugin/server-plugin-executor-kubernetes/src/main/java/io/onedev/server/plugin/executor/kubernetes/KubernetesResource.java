package io.onedev.server.plugin.executor.kubernetes;

import static io.onedev.k8shelper.KubernetesHelper.readInt;
import static io.onedev.k8shelper.KubernetesHelper.readString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.google.common.collect.Lists;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CacheAllocationRequest;
import io.onedev.k8shelper.JobData;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.rest.annotation.Api;

@Api(internal=true)
@Path("/k8s")
@Consumes(MediaType.WILDCARD)
@Singleton
public class KubernetesResource {

	private final JobManager jobManager;
	
    @Context
    private HttpServletRequest request;
    
    @Inject
    public KubernetesResource(JobManager jobManager) {
    	this.jobManager = jobManager;
	}
    
	@Path("/job-data")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
    @POST
    public byte[] getJobData(@Nullable String jobWorkspace) {
		JobContext context = jobManager.getJobContext(getJobToken(), true);
		if (StringUtils.isNotBlank(jobWorkspace))
			context.reportJobWorkspace(jobWorkspace);	
		JobData jobData = new JobData(context.getCommitId().name(), context.getActions());
		return SerializationUtils.serialize(jobData);
    }
	
	@Path("/allocate-job-caches")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
    @POST
    public byte[] allocateJobCaches(String requestString) {
		CacheAllocationRequest request = CacheAllocationRequest.fromString(requestString);
		return SerializationUtils.serialize((Serializable) jobManager.allocateJobCaches(
				getJobToken(), request));
    }
	
	@Path("/run-server-step")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response runServerStep(InputStream is) {
		StreamingOutput os = new StreamingOutput() {

			@Override
		   public void write(OutputStream output) throws IOException {
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
					
					FileUtils.untar(is, filesDir, false);
					
					Map<String, byte[]> outputFiles = jobManager.runServerStep(getJobToken(), stepPosition, 
							filesDir, placeholderValues, new TaskLogger() {

						@Override
						public void log(String message, String sessionId) {
							// While testing, ngrok.io buffers response and build can not get log entries 
							// timely. This won't happen on pagekite however
							KubernetesHelper.writeInt(output, 1);
							KubernetesHelper.writeString(output, message);
							try {
								output.flush();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						
					});
					if (outputFiles == null)
						outputFiles = new HashMap<>();
					byte[] bytes = SerializationUtils.serialize((Serializable) outputFiles); 
					KubernetesHelper.writeInt(output, 2);
					KubernetesHelper.writeInt(output, bytes.length);
					output.write(bytes);
				} finally {
					FileUtils.deleteDir(filesDir);
				}						
		   }				   
		   
		};
		return Response.ok(os).build();
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
					FileUtils.tar(tempDir, Lists.newArrayList("**"), new ArrayList<>(), output, false);
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
		if (jobToken != null) 
			return Response.ok().build();
		else 
			return Response.status(400).entity("Missing job token").build();
	}
	
	private String getJobToken() {
		String jobToken = Job.getToken(request);
		if (jobToken != null)
			return jobToken;
		else
			throw new ExplicitException("Job token is expected");
	}
	
}
