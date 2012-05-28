package com.pmease.commons.tapestry.persistence;

import java.util.List;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

/**
 * A simple implementation of {@link org.apache.tapestry5.grid.GridDataSource}
 * based on a Hibernate Session and a known entity class. This implementation
 * does support multiple {@link org.apache.tapestry5.grid.SortConstraint sort
 * constraints}; however it assumes a direct mapping from sort constraint
 * property to Hibernate property.
 * <p/>
 * This class is <em>not</em> thread-safe; it maintains internal state.
 * <p/>
 * Typically, an instance of this object is created fresh as needed (that is, it
 * is not stored between requests).
 */
public class EntityDataSource implements GridDataSource {
	private final Session session;

	private final Class<?> entityType;

	private int startIndex;

	private List<?> preparedResults;

	public EntityDataSource(Session session, Class<?> entityType) {
		assert session != null;
		assert entityType != null;
		this.session = session;
		this.entityType = entityType;
	}

	/**
	 * Returns the total number of rows for the configured entity type.
	 */
	public int getAvailableRows() {
		Criteria criteria = session.createCriteria(entityType);

		applyAdditionalConstraints(criteria);

		criteria.setProjection(Projections.rowCount());

		Number result = (Number) criteria.uniqueResult();

		return result.intValue();
	}

	/**
	 * Prepares the results, performing a query (applying the sort results, and
	 * the provided start and end index). The results can later be obtained from
	 * {@link #getRowValue(int)} .
	 * 
	 * @param startIndex
	 *            index, from zero, of the first item to be retrieved
	 * @param endIndex
	 *            index, from zero, of the last item to be retrieved
	 * @param sortConstraints
	 *            zero or more constraints used to set the order of the returned
	 *            values
	 */
	public void prepare(int startIndex, int endIndex,
			List<SortConstraint> sortConstraints) {
		assert sortConstraints != null;
		Criteria crit = session.createCriteria(entityType);

		crit.setFirstResult(startIndex)
				.setMaxResults(endIndex - startIndex + 1);

		for (SortConstraint constraint : sortConstraints) {

			String propertyName = constraint.getPropertyModel()
					.getPropertyName();

			switch (constraint.getColumnSort()) {

			case ASCENDING:

				crit.addOrder(Order.asc(propertyName));
				break;

			case DESCENDING:
				crit.addOrder(Order.desc(propertyName));
				break;

			default:
			}
		}

		applyAdditionalConstraints(crit);

		this.startIndex = startIndex;

		preparedResults = crit.list();
	}

	/**
	 * Invoked after the main criteria has been set up (firstResult, maxResults
	 * and any sort contraints). This gives subclasses a chance to apply
	 * additional constraints before the list of results is obtained from the
	 * criteria. This implementation does nothing and may be overridden.
	 */
	protected void applyAdditionalConstraints(Criteria crit) {
	}

	/**
	 * Returns a row value at the given index (which must be within the range
	 * defined by the call to {@link #prepare(int, int, java.util.List)} ).
	 * 
	 * @param index
	 *            of object
	 * @return object at that index
	 */
	public Object getRowValue(int index) {
		return preparedResults.get(index - startIndex);
	}

	/**
	 * Returns the entity type, as provided via the constructor.
	 */
	public Class<?> getRowType() {
		return entityType;
	}
}
