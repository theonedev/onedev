package io.onedev.server.model.support.administration;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.validation.Validatable;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Editable
@ClassValidating
public class ClusterSetting implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_REPLICA_COUNT = "replicaCount";
	
	private int replicaCount;

	@Editable(order = 100, name="Replica Count", description = "Number of project replicas, including primary and backups")
	@OmitName
	@Min(value = 1, message = "At least one replica should be specified")
	public int getReplicaCount() {
		return replicaCount;
	}

	public void setReplicaCount(int replicaCount) {
		this.replicaCount = replicaCount;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		int memberCount = OneDev.getInstance(ClusterManager.class)
				.getHazelcastInstance()
				.getCluster().getMembers().size();
		if (replicaCount > memberCount) {
			context.buildConstraintViolationWithTemplate("No enough servers to store replicas")
					.addPropertyNode(PROP_REPLICA_COUNT).addConstraintViolation();
			isValid = false;
		}
		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}
	
}
