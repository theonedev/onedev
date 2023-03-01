package io.onedev.server.model.support.build;

import java.io.Serializable;

import io.onedev.server.annotation.BuildQuery;
import io.onedev.server.annotation.Editable;

@Editable
public class BuildPreservation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String condition;
	
	private Integer count;

	@Editable(order=100, placeholder="All", description="Specify the condition preserved builds must match")
	@BuildQuery(withOrder = false)
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	@Editable(order=200, placeholder="Unlimited", description="Number of builds to preserve")
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
}
