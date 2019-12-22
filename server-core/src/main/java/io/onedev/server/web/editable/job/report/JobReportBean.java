package io.onedev.server.web.editable.job.report;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class JobReportBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private JobReport report;

	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public JobReport getReport() {
		return report;
	}

	public void setReport(JobReport report) {
		this.report = report;
	}

}
