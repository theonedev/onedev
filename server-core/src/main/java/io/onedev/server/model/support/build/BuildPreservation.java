package io.onedev.server.model.support.build;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.BuildQuery;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class BuildPreservation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String condition;
	
	private Integer count;

	@Editable(order=100, description="Specify the condition preserved builds must match")
	@BuildQuery(withOrder = false, withCurrentUserCriteria = false, withUnfinishedCriteria = false)
	@NameOfEmptyValue("All")
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
