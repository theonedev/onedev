package ${package}.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.pmease.commons.hibernate.AbstractEntity;

@Entity
public class User extends AbstractEntity {
	
	@Column(unique = true, nullable = false)
	private String name;

	private String fullName;

	private String email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
