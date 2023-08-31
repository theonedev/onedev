package io.onedev.server.model;

import javax.persistence.*;

import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.util.MetricIndicator;

import static io.onedev.server.model.support.BuildMetric.PROP_REPORT;

@Entity
@Table(
		indexes={@Index(columnList="o_build_id"), @Index(columnList= PROP_REPORT)},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_build_id", PROP_REPORT})}
)
public class ProblemMetric extends AbstractEntity implements BuildMetric {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build build;
	
	@Column(nullable=false)
	private String reportName;

	private int highSeverities;
	
	private int mediumSeverities;
	
	private int lowSeverities;
	
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

	@MetricIndicator(group="Total Problems", order=100, minValue=0, name="High Severity", color="#F64E60")
	public int getHighSeverities() {
		return highSeverities;
	}

	public void setHighSeverities(int totalErrors) {
		this.highSeverities = totalErrors;
	}

	@MetricIndicator(group="Total Problems", order=200, minValue=0, name="Medium Severity", color="#FFA800")
	public int getMediumSeverities() {
		return mediumSeverities;
	}

	public void setMediumSeverities(int mediumSeverities) {
		this.mediumSeverities = mediumSeverities;
	}

	@MetricIndicator(group="Total Problems", order=300, minValue=0, name="Low Severity", color="#8950FC")
	public int getLowSeverities() {
		return lowSeverities;
	}

	public void setLowSeverities(int lowSeverities) {
		this.lowSeverities = lowSeverities;
	}

}
