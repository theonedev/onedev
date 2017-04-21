package com.gitplex.server.model.support;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.model.Depot;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.PathPattern;
import com.gitplex.server.util.reviewappointment.ReviewAppointment;

@Editable
public class FileProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private String path;
	
	private String reviewAppointmentExpr;
	
	private transient ReviewAppointment reviewAppointment;
	
	@Editable(order=100, description="Specify path to be protected. Wildcard can be used in the path "
			+ "to match multiple files")
	@PathPattern
	@NotEmpty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Editable(order=200, name="Reviewers", description="Optionally specify required reviewers if specified path is "
			+ "changed. Note that the user submitting the change is considered to reviewed the change automatically")
	@com.gitplex.server.util.editable.annotation.ReviewAppointment
	@NotEmpty
	public String getReviewAppointmentExpr() {
		return reviewAppointmentExpr;
	}

	public void setReviewAppointmentExpr(String reviewAppointmentExpr) {
		this.reviewAppointmentExpr = reviewAppointmentExpr;
	}
	
	public ReviewAppointment getReviewAppointment(Depot depot) {
		if (reviewAppointment == null)
			reviewAppointment = new ReviewAppointment(depot, reviewAppointmentExpr);
		return reviewAppointment;
	}
	
}
