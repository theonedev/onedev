package com.pmease.gitop.web.page.repository.source.commit.diff.patch;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.util.RawParseUtils;

/** An error in a patch script */
public class FormatError {
	/** Classification of an error. */
	public static enum Severity {
		/** The error is unexpected, but can be worked around. */
		WARNING,

		/** The error indicates the script is severely flawed. */
		ERROR;
	}

	private final byte[] buf;

	private final int offset;

	private final Severity severity;

	private final String message;

	FormatError(final byte[] buffer, final int ptr, final Severity sev,
			final String msg) {
		buf = buffer;
		offset = ptr;
		severity = sev;
		message = msg;
	}

	/** @return the severity of the error. */
	public Severity getSeverity() {
		return severity;
	}

	/** @return a message describing the error. */
	public String getMessage() {
		return message;
	}

	/** @return the byte buffer holding the patch script. */
	public byte[] getBuffer() {
		return buf;
	}

	/** @return byte offset within {@link #getBuffer()} where the error is */
	public int getOffset() {
		return offset;
	}

	/** @return line of the patch script the error appears on. */
	public String getLineText() {
		final int eol = RawParseUtils.nextLF(buf, offset);
		return RawParseUtils.decode(Constants.CHARSET, buf, offset, eol);
	}

	@Override
	public String toString() {
		final StringBuilder r = new StringBuilder();
		r.append(getSeverity().name().toLowerCase());
		r.append(": at offset "); //$NON-NLS-1$
		r.append(getOffset());
		r.append(": "); //$NON-NLS-1$
		r.append(getMessage());
		r.append("\n"); //$NON-NLS-1$
		r.append("  in "); //$NON-NLS-1$
		r.append(getLineText());
		return r.toString();
	}
}
