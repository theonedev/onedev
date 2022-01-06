package io.onedev.server.buildspec.step;

import javax.validation.constraints.NotNull;

import io.onedev.k8shelper.CheckoutFacade;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.buildspec.job.gitcredential.DefaultCredential;
import io.onedev.server.buildspec.job.gitcredential.GitCredential;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=50, name="Checkout Code")
public class CheckoutStep extends Step {

	private static final long serialVersionUID = 1L;

	private GitCredential cloneCredential = new DefaultCredential();
	
	private boolean withLfs;
	
	private boolean withSubmodules;
	
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

	@Editable(order=120, name="Retrieve LFS Files", description="Check this to retrieve Git LFS files")
	public boolean isWithLfs() {
		return withLfs;
	}

	public void setWithLfs(boolean withLfs) {
		this.withLfs = withLfs;
	}

	@Editable(order=180, name="Retrieve Submodules", description="Check this to retrieve submodules")
	public boolean isWithSubmodules() {
		return withSubmodules;
	}

	public void setWithSubmodules(boolean withSubmodules) {
		this.withSubmodules = withSubmodules;
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
	public StepFacade getFacade(Build build, String jobToken, ParamCombination paramCombination) {
		return new CheckoutFacade(cloneDepth!=null?cloneDepth:0, withLfs, withSubmodules, 
				cloneCredential.newCloneInfo(build, jobToken));
	}

}
