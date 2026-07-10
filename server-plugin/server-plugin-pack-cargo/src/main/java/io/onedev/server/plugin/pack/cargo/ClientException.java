package io.onedev.server.plugin.pack.cargo;

public class ClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int statusCode;

	public ClientException(int statusCode) {
		this(statusCode, null);
	}

	public ClientException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
