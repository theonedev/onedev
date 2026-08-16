package io.onedev.server.search.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.util.criteria.Criteria;

public abstract class EntityQuery<T extends AbstractEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	private Criteria<T> criteria;

	private List<EntitySort> sorts;

	private List<EntitySort> baseSorts;

	public EntityQuery(@Nullable Criteria<T> criteria, List<EntitySort> sorts) {
		this(criteria, sorts, new ArrayList<>());
	}

	public EntityQuery(@Nullable Criteria<T> criteria, List<EntitySort> sorts, List<EntitySort> baseSorts) {
		this.criteria = criteria;
		this.sorts = sorts;
		this.baseSorts = baseSorts;
	}

	@Nullable
	public Criteria<T> getCriteria() {
		return criteria;
	}

	public void setCriteria(@Nullable Criteria<T> criteria) {
		this.criteria = criteria;
	}

	public List<EntitySort> getSorts() {
		return sorts;
	}

	public void setSorts(List<EntitySort> sorts) {
		this.sorts = sorts;
	}

	public List<EntitySort> getBaseSorts() {
		return baseSorts;
	}

	public void setBaseSorts(List<EntitySort> baseSorts) {
		this.baseSorts = baseSorts;
	}
	
	public boolean matches(T entity) {
		return getCriteria() == null || getCriteria().matches(entity);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (getCriteria() != null) 
			builder.append(getCriteria().toString()).append(" ");
		if (!getSorts().isEmpty()) {
			builder.append("order by ");
			builder.append(getSorts().stream().map(it->it.toString()).collect(Collectors.joining(", ")));
		}
		String toStringValue = builder.toString().trim();
		if (toStringValue.length() == 0)
			toStringValue = null;
		return toStringValue;
	}
	
	public EntityQuery<T> onMoveProject(String oldPath, String newPath) {
		if (getCriteria() != null)
			getCriteria().onMoveProject(oldPath, newPath);
		return this;
	}
	
	public boolean isUsingProject(String projectPath) {
		if (getCriteria() != null)
			return getCriteria().isUsingProject(projectPath);
		else
			return false;
	}
	
}
