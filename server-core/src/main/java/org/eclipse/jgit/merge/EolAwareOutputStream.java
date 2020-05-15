/*
 * Copyright (C) 2014, André de Oliveira <andre.oliveira@liferay.com>
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eclipse.jgit.merge;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream which is aware of newlines and can be asked to begin a new
 * line if not already in one.
 */
class EolAwareOutputStream extends OutputStream {
	private final OutputStream out;

	private boolean bol = true;

	/**
	 * Initialize a new EOL aware stream.
	 *
	 * @param out
	 *            stream to output all writes to.
	 */
	EolAwareOutputStream(OutputStream out) {
		this.out = out;
	}

	/**
	 * Begin a new line if not already in one.
	 *
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	void beginln() throws IOException {
		if (!bol)
			write('\n');
	}

	/** @return true if a new line has just begun. */
	boolean isBeginln() {
		return bol;
	}

	/** {@inheritDoc} */
	@Override
	public void write(int val) throws IOException {
		out.write(val);
		bol = (val == '\n');
	}

	/** {@inheritDoc} */
	@Override
	public void write(byte[] buf, int pos, int cnt) throws IOException {
		if (cnt > 0) {
			out.write(buf, pos, cnt);
			bol = (buf[pos + (cnt - 1)] == '\n');
		}
	}
}
