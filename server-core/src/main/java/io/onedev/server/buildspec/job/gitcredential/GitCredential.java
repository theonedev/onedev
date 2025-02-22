package io.onedev.server.buildspec.job.gitcredential;

import com.google.common.collect.Lists;
import io.onedev.k8shelper.CloneInfo;
import io.onedev.server.OneDev;
import io.onedev.server.ServerConfig;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ImplementationProvider;
import io.onedev.server.model.Build;

import java.io.Serializable;
import java.util.Collection;

@Editable
@ImplementationProvider("getImplementations")
public interface GitCredential extends Serializable {
	
	CloneInfo newCloneInfo(Build build, String jobToken);
	
	@SuppressWarnings("unused")
	private static Collection<Class<? extends GitCredential>> getImplementations() {
		var implementations = Lists.newArrayList(DefaultCredential.class, HttpCredential.class);
		if (OneDev.getInstance(ServerConfig.class).getSshPort() != 0)
			implementations.add(SshCredential.class);
		return implementations;
	}
	
}
