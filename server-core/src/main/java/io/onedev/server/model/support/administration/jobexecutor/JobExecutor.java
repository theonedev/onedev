package io.onedev.server.model.support.administration.jobexecutor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.util.ExceptionUtils;
import io.onedev.server.util.PKCS12CertExtractor;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.validation.annotation.DnsName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.JobRequirement;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@ExtensionPoint
@Editable
public abstract class JobExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String name;
	
	private String jobRequirement;
	
	private int cacheTTL = 7;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=10)
	@DnsName //this name may be used as namespace/network prefixes, so put a strict constraint
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=10000, name="Job Requirement", description="Optionally specify job requirement of this executor. "
			+ "Only satisfied jobs can use this executor")
	@JobRequirement
	@NameOfEmptyValue("Can be used by any jobs")
	public String getJobRequirement() {
		return jobRequirement;
	}

	public void setJobRequirement(String jobRequirement) {
		this.jobRequirement = jobRequirement;
	}

	@Editable(order=50000, group="More Settings", description="Specify job cache TTL (time to live) by days. "
			+ "OneDev may create multiple job caches even for same cache key to avoid cache conflicts when "
			+ "running jobs concurrently. This setting tells OneDev to remove caches inactive for specified "
			+ "time period to save disk space")
	public int getCacheTTL() {
		return cacheTTL;
	}

	public void setCacheTTL(int cacheTTL) {
		this.cacheTTL = cacheTTL;
	}
	
	public abstract void execute(String jobToken, JobContext context);
	
	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		if (io.onedev.server.job.requirement.JobRequirement.parse(jobRequirement).isUsingProject(projectPath))
			usage.add("job match" );
		return usage;
	}
	
	public void onRenameProject(String oldPath, String newPath) {
		if (jobRequirement != null) {
			io.onedev.server.job.requirement.JobRequirement parsedJobRequirement = 
					io.onedev.server.job.requirement.JobRequirement.parse(jobRequirement);
			parsedJobRequirement.onRenameProject(oldPath, newPath);
			jobRequirement = parsedJobRequirement.toString();
		}
	}

	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (io.onedev.server.job.requirement.JobRequirement.parse(jobRequirement).isUsingUser(userName))
			usage.add("job requirement" );
		return usage;
	}
	
	public void onRenameUser(String oldName, String newName) {
		io.onedev.server.job.requirement.JobRequirement parsedJobRequirement = 
				io.onedev.server.job.requirement.JobRequirement.parse(this.jobRequirement);
		parsedJobRequirement.onRenameUser(oldName, newName);
		jobRequirement = parsedJobRequirement.toString();
	}
	
	protected List<String> getTrustCertContent() {
		List<String> trustCertContent = new ArrayList<>();
		ServerConfig serverConfig = OneDev.getInstance(ServerConfig.class); 
		File keystoreFile = serverConfig.getKeystoreFile();
		if (keystoreFile != null) {
			String password = serverConfig.getKeystorePassword();
			for (Map.Entry<String, String> entry: new PKCS12CertExtractor(keystoreFile, password).extact().entrySet()) 
				trustCertContent.addAll(Splitter.on('\n').trimResults().splitToList(entry.getValue()));
		}
		if (serverConfig.getTrustCertsDir() != null) {
			for (File file: serverConfig.getTrustCertsDir().listFiles()) {
				if (file.isFile()) {
					try {
						trustCertContent.addAll(FileUtils.readLines(file, UTF_8));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return trustCertContent;
	}
	
	protected String getErrorMessage(Exception exception) {
		String errorMessage = ExceptionUtils.getExpectedError(exception);
		if (errorMessage == null) 
			errorMessage = Throwables.getStackTraceAsString(exception);
		return errorMessage;
	}
	
}
