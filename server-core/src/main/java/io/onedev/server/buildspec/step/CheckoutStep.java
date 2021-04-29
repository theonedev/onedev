package io.onedev.server.buildspec.step;

import javax.validation.constraints.NotNull;

import io.onedev.k8shelper.CheckoutExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.job.gitcredential.DefaultCredential;
import io.onedev.server.buildspec.job.gitcredential.GitCredential;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=5, name="Checkout Code")
public class CheckoutStep extends Step {

	private static final long serialVersionUID = 1L;

	private GitCredential cloneCredential = new DefaultCredential();
	
	private Integer cloneDepth;
	
	@Editable(order=100, description="By default code is cloned via an auto-generated credential, "
			+ "which only has read permission over current project. In case the job needs to <a href='$docRoot/pages/push-in-job.md' target='_blank'>push code to server</a>, or want "
			+ "to <a href='$docRoot/pages/clone-submodules-via-ssh.md' target='_blank'>clone private submodules</a>, you should supply custom credential with appropriate permissions here")
	@NotNull
	public GitCredential getCloneCredential() {
		return cloneCredential;
	}

	public void setCloneCredential(GitCredential cloneCredential) {
		this.cloneCredential = cloneCredential;
	}

	@Editable(order=200, description="Optionally specify depth for a shallow clone in order "
			+ "to speed up source retrieval")
	public Integer getCloneDepth() {
		return cloneDepth;
	}

	public void setCloneDepth(Integer cloneDepth) {
		this.cloneDepth = cloneDepth;
	}

	@Override
	public Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination) {
		return new CheckoutExecutable(cloneDepth!=null?cloneDepth:0, cloneCredential.newCloneInfo(build, jobToken));
	}

}
