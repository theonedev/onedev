package io.onedev.server.model.support;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.BuildQuery;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class BuildPreservation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String condition;
	
	private Integer count;

	@Editable(order=100, description="Specify the condition preserved builds must match")
	@BuildQuery(withCurrentUserCriteria = false, withUnfinishedCriteria = false)
	@NotEmpty
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	@Editable(order=200, description="Number of builds to preserve")
	@NameOfEmptyValue("Unlimited")
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
}
