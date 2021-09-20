package io.onedev.server.util;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper extends InputStream {

	private final InputStream delegate;
	
	public InputStreamWrapper(InputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public int read() throws IOException {
		return delegate.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return delegate.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return delegate.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}

	@Override
	public int available() throws IOException {
		return delegate.available();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		delegate.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		delegate.reset();
	}

	@Override
	public boolean markSupported() {
		return delegate.markSupported();
	}
	
}
