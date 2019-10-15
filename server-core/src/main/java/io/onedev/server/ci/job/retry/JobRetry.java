package io.onedev.server.ci.job.retry;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.RetryCondition;

@Editable
@Horizontal
public class JobRetry implements Serializable {

	private static final long serialVersionUID = 1L;

	private String condition;
	
	private int maxRetries = 3;
	
	private int retryDelay = 30;

	@Editable(order=100, description="Specify the condition to retry the failed build")
	@NotEmpty
	@RetryCondition
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	@Editable(order=200, description="Maximum of retries before giving up")
	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Editable(order=300, description="Delay for the first retry in seconds. Delay of subsequent "
			+ "retries will be calculated using an exponential back-off based on this delay")
	public int getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(int retryDelay) {
		this.retryDelay = retryDelay;
	}

}
