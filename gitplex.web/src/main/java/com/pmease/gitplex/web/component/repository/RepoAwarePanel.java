package com.pmease.gitplex.web.component.repository;

import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class RepoAwarePanel extends Panel {

	private transient RepositoryAware repoAware;
	
	public RepoAwarePanel(String id) {
		super(id);
	}

	public RepoAwarePanel(String id, IModel<?> model) {
		super(id, model);
	}
	
	private RepositoryAware getRepoAware() {
		if (repoAware == null) {
			if (this instanceof RepositoryAware) {
				repoAware = (RepositoryAware) this;
			} else { 
				repoAware = Preconditions.checkNotNull(findParent(RepositoryAware.class), 
						"No any container of component '" + this + "' implements interface 'RepositoryAware'.");
			}
		}
		return repoAware;
	}
	
	@Override
	protected void onRemove() {
		super.onRemove();

		repoAware = null;
	}

	/**
	 * Get repository object.
	 * 
	 * @return
	 * 			repository object
	 */
	protected Repository getRepository() {
		return getRepoAware().getRepository();
	}
	
	/**
	 * Get current revision of the repository.
	 * 
	 * @return
	 * 			current revision of the repository, or default branch if current revision is unknown
	 */
	protected String getCurrentRevision() {
		return getRepoAware().getCurrentRevision();
	}
	
	/**
	 * Get current object path of the repository.
	 * 
	 * @return
	 * 			current object path of this repository, or <tt>null</tt> if current object path is unknown
	 */
	protected String getCurrentPath() {
		return getRepoAware().getCurrentPath();
	}
	
	/**
	 * Get current object path of the repository as segments. 
	 * 
	 * @return
	 * 			current object path of the repository as segments, or an empty list if current object path is unknown
	 */
	protected List<String> getCurrentPathSegments() {
		return getRepoAware().getCurrentPathSegments();
	}

}
