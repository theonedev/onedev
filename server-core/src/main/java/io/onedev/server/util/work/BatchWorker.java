package io.onedev.server.util.work;

import java.util.Collection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;

import io.onedev.server.util.concurrent.Prioritized;

public abstract class BatchWorker {
	
	private final String id;
	
	private final int maxBatchSize;
	
	public BatchWorker(String id, int maxBatchSize) {
		this.id = id;
		Preconditions.checkArgument(maxBatchSize>=1);
		this.maxBatchSize = maxBatchSize;
	}
	
	public BatchWorker(String id) {
		this(id, Integer.MAX_VALUE);
	}
	
	public String getId() {
		return id;
	}
	
	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BatchWorker))
			return false;
		if (this == other)
			return true;
		BatchWorker otherWorker = (BatchWorker) other;
		if (id == null || otherWorker.id == null)
			return super.equals(other);
		else 
			return new EqualsBuilder().append(id, otherWorker.id).isEquals();
	}

	@Override
	public int hashCode() {
		if (id == null)
			return super.hashCode();
		else
			return new HashCodeBuilder(17, 37).append(id).toHashCode();
	}
	
	public abstract void doWorks(Collection<Prioritized> works);
}
