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
public class CloverMetric extends AbstractEntity implements BuildMetric {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build build;
	
	@Column(nullable=false)
	private String reportName;

	private int methodCoverage;
	
	private int statementCoverage;
	
	private int branchCoverage;
	
	private int lineCoverage;
	
	private int totalMethods;
	
	private int totalStatements;
	
	private int totalBranches;
	
	private int totalLines;
	
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

	@MetricIndicator(group="Code Coverage", order=100, valueFormatter=BuildMetric.FORMAT_PERCENTAGE, 
			maxValue=100, minValue=0, name="Method", color="#F64E60")
	public int getMethodCoverage() {
		return methodCoverage;
	}

	public void setMethodCoverage(int methodCoverage) {
		this.methodCoverage = methodCoverage;
	}

	@MetricIndicator(group="Code Coverage", order=200, valueFormatter=BuildMetric.FORMAT_PERCENTAGE, 
			maxValue=100, minValue=0, name="Branch", color="#1BC5BD")
	public int getBranchCoverage() {
		return branchCoverage;
	}

	public void setBranchCoverage(int branchCoverage) {
		this.branchCoverage = branchCoverage;
	}

	@MetricIndicator(group="Code Coverage", order=300, valueFormatter=BuildMetric.FORMAT_PERCENTAGE, 
			maxValue=100, minValue=0, name="Statement", color="#8950FC")
	public int getStatementCoverage() {
		return statementCoverage;
	}

	public void setStatementCoverage(int statementCoverage) {
		this.statementCoverage = statementCoverage;
	}

	@MetricIndicator(group="Code Coverage", order=400, valueFormatter=BuildMetric.FORMAT_PERCENTAGE, 
			maxValue=100, minValue=0, name="Line", color="#FFA800")
	public int getLineCoverage() {
		return lineCoverage;
	}

	public void setLineCoverage(int lineCoverage) {
		this.lineCoverage = lineCoverage;
	}

	@MetricIndicator(group="Total Number", order=500, minValue=0, name="Method", color="#F64E60")
	public int getTotalMethods() {
		return totalMethods;
	}

	public void setTotalMethods(int totalMethods) {
		this.totalMethods = totalMethods;
	}

	@MetricIndicator(group="Total Number", order=600, minValue=0, name="Branch", color="#1BC5BD")
	public int getTotalBranches() {
		return totalBranches;
	}

	public void setTotalBranches(int totalBranches) {
		this.totalBranches = totalBranches;
	}

	@MetricIndicator(group="Total Number", order=700, minValue=0, name="Statement", color="#8950FC")
	public int getTotalStatements() {
		return totalStatements;
	}

	public void setTotalStatements(int totalStatements) {
		this.totalStatements = totalStatements;
	}

	@MetricIndicator(group="Total Number", order=800, minValue=0, name="Effective Line", color="#FFA800")
	public int getTotalLines() {
		return totalLines;
	}

	public void setTotalLines(int totalLines) {
		this.totalLines = totalLines;
	}

}
