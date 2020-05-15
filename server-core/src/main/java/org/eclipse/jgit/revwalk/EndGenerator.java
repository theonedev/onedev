/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

class EndGenerator extends Generator {
	static final EndGenerator INSTANCE = new EndGenerator();

	private EndGenerator() {
		super(false);
	}

	@Override
	RevCommit next() {
		return null;
	}

	@Override
	int outputType() {
		return 0;
	}
}
