package com.pmease.commons.util.trimmable;

import javax.annotation.Nullable;

/**
 * Use this interface to trim unused parts in a data structure.
 * <p>
 * For instance, some parts of a data structure might refer to database entities 
 * (where using foreign key is inappropriate), and you may implement this 
 * interface to remove those parts if the entity is removed from database. 
 *   
 * @author robin
 *
 */
public interface Trimmable {
	/**
	 * Trim unused parts of current data structure.
	 * <p>
	 * @param context
	 * 			context to help the trimming. Note context object is not the object to 
	 * 			be checked for trimming, instead it is used to help the trim process
	 * @return
	 * 			null if the data structure has to be removed. If not null, caller should 
	 * 			check if the return value is the same instance as current instance, and 
	 * 			to replace that instance if not the same.
	 */
	Object trim(@Nullable Object context);
}
