package io.onedev.server.model;

import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.util.MetricIndicator;

import javax.persistence.*;

import static io.onedev.server.model.support.BuildMetric.PROP_REPORT;

@Entity
@Table(
		indexes={@Index(columnList="o_build_id"), @Index(columnList= PROP_REPORT)},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_build_id", PROP_REPORT})}
)
public class CoverageMetric extends AbstractEntity implements BuildMetric {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build build;
	
	@Column(nullable=false)
	private String reportName;
	
	private int branchCoverage;
	
	private int lineCoverage;
	
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

	@MetricIndicator(group="Code Coverage", order=200, valueFormatter=BuildMetric.FORMAT_PERCENTAGE, 
			maxValue=100, minValue=0, name="Branch", color="#1BC5BD")
	public int getBranchCoverage() {
		return branchCoverage;
	}

	public void setBranchCoverage(int branchCoverage) {
		this.branchCoverage = branchCoverage;
	}

	@MetricIndicator(group="Code Coverage", order=400, valueFormatter=BuildMetric.FORMAT_PERCENTAGE, 
			maxValue=100, minValue=0, name="Line", color="#FFA800")
	public int getLineCoverage() {
		return lineCoverage;
	}

	public void setLineCoverage(int lineCoverage) {
		this.lineCoverage = lineCoverage;
	}

}
