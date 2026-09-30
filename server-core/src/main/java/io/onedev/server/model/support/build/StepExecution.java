package io.onedev.server.model.support.build;

import java.io.Serializable;

/** Status and timing of an executed step, excluding initialization and finalization. */
public class StepExecution implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Status {
		RUNNING, SUCCESSFUL, FAILED, CANCELLED, UNKNOWN
	}

	private Status status = Status.RUNNING;
	private long duration;
	private long startedAt = System.currentTimeMillis();
	private boolean skipped;
	private int stepIndex;
	private int stepCount;

	public int getStepIndex() {
		return stepIndex;
	}

	public int getStepCount() {
		return stepCount;
	}

	public void setStepPosition(int stepIndex, int stepCount) {
		this.stepIndex = stepIndex;
		this.stepCount = stepCount;
	}

	public Status getStatus() {
		return status;
	}

	public boolean isSkipped() {
		return skipped;
	}

	/** Running time in milliseconds for this execution. */
	public long getDuration() {
		return duration + (status == Status.RUNNING ? Math.max(0, System.currentTimeMillis() - startedAt) : 0);
	}

	/** Record the step outcome or UNKNOWN when unavailable, preserving an already completed execution. */
	public void complete(Status status) {
		if (this.status == Status.RUNNING) {
			duration = getDuration();
			this.status = status;
		}
	}

	public void skip() {
		complete(Status.CANCELLED);
		skipped = true;
	}

}
