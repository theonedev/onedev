package io.onedev.server.crypto;

public interface CryptoService {

	byte[] getCipherKey();

	byte[] encrypt(byte[] data);

	byte[] decrypt(byte[] data);

}
