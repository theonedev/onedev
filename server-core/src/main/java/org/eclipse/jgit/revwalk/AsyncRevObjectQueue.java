/*
 * Copyright (C) 2010, Google Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

import java.io.IOException;

import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AsyncOperation;

/**
 * Queue to lookup and parse objects asynchronously.
 *
 * A queue may perform background lookup of objects and supply them (possibly
 * out-of-order) to the application.
 */
public interface AsyncRevObjectQueue extends AsyncOperation {
	/**
	 * Obtain the next object.
	 *
	 * @return the object; null if there are no more objects remaining.
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 *             the object does not exist. There may be more objects
	 *             remaining in the iteration, the application should call
	 *             {@link #next()} again.
	 * @throws java.io.IOException
	 *             the object store cannot be accessed.
	 */
	RevObject next() throws MissingObjectException, IOException;
}
