package io.onedev.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.util.MetricIndicator;

@Entity
@Table(indexes={@Index(columnList="o_build_id"), @Index(columnList=BuildMetric.PROP_REPORT)})
public class CheckstyleMetric extends AbstractEntity implements BuildMetric {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build build;
	
	@Column(nullable=false)
	private String reportName;

	private int totalErrors;
	
	private int totalWarnings;
	
	private int totalInfos;
	
	@Override
	public Build getBuild() {
		return build;
	}

	public void setBuild(Build build) {
		this.build = build;
	}
	
	@Override
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	@MetricIndicator(group="Total Violations", order=100, minValue=0, name="Errors", color="#F64E60")
	public int getTotalErrors() {
		return totalErrors;
	}

	public void setTotalErrors(int totalErrors) {
		this.totalErrors = totalErrors;
	}

	@MetricIndicator(group="Total Violations", order=200, minValue=0, name="Warnings", color="#FFA800")
	public int getTotalWarnings() {
		return totalWarnings;
	}

	public void setTotalWarnings(int totalWarnings) {
		this.totalWarnings = totalWarnings;
	}

	@MetricIndicator(group="Total Violations", order=300, minValue=0, name="Infos", color="#8950FC")
	public int getTotalInfos() {
		return totalInfos;
	}

	public void setTotalInfos(int totalInfos) {
		this.totalInfos = totalInfos;
	}

}
