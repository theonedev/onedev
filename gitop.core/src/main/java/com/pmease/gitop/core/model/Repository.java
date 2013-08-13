package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.model.gatekeeper.GateKeeper;
import com.pmease.gitop.core.model.gatekeeper.GateKeeper.CheckResult;

@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"account", "name"})
})
@SuppressWarnings("serial")
public class Repository extends AbstractEntity {
	
	@Column(nullable=false)
	private String name;
	
	private String description;

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_REPO_ACC")
	private Account account;
	
	@Column(nullable=false)
	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	public CheckResult checkMerge(MergeRequest request) {
		boolean undetermined = false;
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = each.check(request);
			if (result == CheckResult.REJECT)
				return CheckResult.REJECT;
			else if (result == CheckResult.UNDETERMINED)
				undetermined = true;
		}
		
		if (undetermined)
			return CheckResult.UNDETERMINED;
		else
			return CheckResult.ACCEPT;
	}
	
}
