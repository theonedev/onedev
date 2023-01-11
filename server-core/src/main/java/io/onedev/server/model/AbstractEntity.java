package io.onedev.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.util.facade.EntityFacade;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.proxy.HibernateProxy;

import javax.annotation.Nullable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@MappedSuperclass
@JsonIgnoreProperties("handler")
public abstract class AbstractEntity implements Serializable, Comparable<AbstractEntity> {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ID = "id";
	
	public static final String PROP_NUMBER_SCOPE = "numberScope";
	
	public static final String NAME_NUMBER = "Number";
	
	public static final String PROP_NUMBER = "number";
	
	private transient EntityFacade oldVersion;
	
	@Api(order=1)
	@Id
	@GenericGenerator(name="entity_id", strategy="io.onedev.server.persistence.IdGenerator")
	@GeneratedValue(generator="entity_id") 	
	private Long id;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Collection<? extends EntityWatch> getWatches() {
		return new ArrayList<>();
	}
	
	@Nullable
	public EntityWatch getWatch(User user, boolean createIfNotExist) {
		if (createIfNotExist) {
			throw new UnsupportedOperationException();
		} else {
			for (EntityWatch watch: getWatches()) {
				if (watch.getUser().equals(user)) 
					return watch;
			}
			return null;
		}
	}

	@Override
	public final boolean equals(Object other) {
		return equals(this, other);
	}

	@Override
	public final int hashCode() {
		return hashCode(this);
	}
	
	public static boolean equals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		} else if (o1 instanceof AbstractEntity && o2 instanceof AbstractEntity) {
			Long id1 = ((AbstractEntity) o1).getId();
			Long id2 = ((AbstractEntity) o2).getId();
			if (id1 != null && id2 != null)
				return new EqualsBuilder().append(id1, id2).isEquals();
			else 
				return false;
		} else {
			return false;
		}
	}
	
	public static int hashCode(AbstractEntity entity) {
		if (entity.getId() != null)
			return new HashCodeBuilder(17, 37).append(entity.getId()).toHashCode();
		else
			return System.identityHashCode(entity);
	}
	
	@Override
	public int compareTo(AbstractEntity entity) {
		if (getId() != null) {
			if (entity.getId() != null)
				return getId().compareTo(entity.getId());
			else
				return -1;
		} else if (entity.getId() != null) {
			return 1;
		} else {
			return 0;
		}
	}

	@Nullable
	public EntityFacade getOldVersion() {
		return oldVersion;
	}

	public void setOldVersion(@Nullable EntityFacade oldVersion) {
		this.oldVersion = oldVersion;
	}

	public boolean isNew() {
		return getId() == null;
	}
	
	/**
	 * This method is created to get identifier of entity without triggering 
	 * lazy load the whole entity object.
	 * @param entity
	 * @return
	 */
	@Nullable
	public static Long idOf(@Nullable AbstractEntity entity) {
		if (entity == null) {
			return null;
		} else if (entity instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy) entity;
			return (Long) proxy.getHibernateLazyInitializer().getIdentifier();
		} else {
			return entity.getId();
		}
	}
	
	@Nullable
	public EntityFacade getFacade() {
		return null;
	}

}