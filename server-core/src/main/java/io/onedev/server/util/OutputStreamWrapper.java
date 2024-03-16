package io.onedev.server.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamWrapper extends FilterOutputStream {

	public OutputStreamWrapper(OutputStream os) {
		super(os);
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

}
