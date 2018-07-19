package io.onedev.server.manager.impl;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.lifecycle.SystemStopping;
import io.onedev.utils.FileUtils;

import com.google.common.base.Charsets;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.EnvironmentConfig;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;

public abstract class AbstractEnvironmentManager {
	
	private static final String VERSION_FILE = "version.txt";
	
	private static final long DEFAULT_LOG_FILE_SIZE = 8192;
	
	private static final int MEMORY_USAGE_PERCENT = 25;
	
	private final Map<String, Environment> envs = new ConcurrentHashMap<>();
	
	protected void checkVersion(String envKey) {
		File versionFile = new File(getEnvDir(envKey), VERSION_FILE);
		int versionFromFile;
		if (versionFile.exists()) {
			try {
				versionFromFile = Integer.parseInt(FileUtils.readFileToString(versionFile).trim());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			versionFromFile = 0;
		}
		if (versionFromFile != getEnvVersion()) {
			FileUtils.cleanDir(versionFile.getParentFile());
			FileUtils.writeFile(versionFile, String.valueOf(getEnvVersion()));
		} 
	}
	
	protected void writeVersion(String envKey) {
		File versionFile = new File(getEnvDir(envKey), VERSION_FILE);
		FileUtils.writeFile(versionFile, String.valueOf(getEnvVersion()));
	}

	protected abstract File getEnvDir(String envKey);
	
	protected abstract int getEnvVersion();
	
	protected long getLogFileSize() {
		return DEFAULT_LOG_FILE_SIZE;
	}
	
	protected Environment getEnv(String envKey) {
		Environment env = envs.get(envKey);
		if (env == null) synchronized (envs) {
			env = envs.get(envKey);
			if (env == null) {
				checkVersion(envKey);
				EnvironmentConfig config = new EnvironmentConfig();
				config.setEnvCloseForcedly(true);
				config.setMemoryUsagePercentage(MEMORY_USAGE_PERCENT);
				config.setLogFileSize(getLogFileSize());
				env = Environments.newInstance(getEnvDir(envKey), config);
				envs.put(envKey, env);
			}
		}
		return env;
	}
	
	protected Store getStore(Environment env, String storeName) {
		return env.computeInTransaction(new TransactionalComputable<Store>() {
		    @Override
		    public Store compute(Transaction txn) {
		        return env.openStore(storeName, StoreConfig.WITHOUT_DUPLICATES, txn);
		    }
		});		
	}

	protected void removeEnv(String envKey) {
		synchronized (envs) {
			Environment env = envs.remove(envKey);
			if (env != null)
				env.close();
		}
	}

	@Listen
	public void on(SystemStopping event) {
		synchronized (envs) {
			for (Environment env: envs.values())
				env.close();
			envs.clear();
		}
	}

	@Nullable 
	protected byte[] readBytes(Store store, Transaction txn, ByteIterable key) {
		ByteIterable value = store.get(txn, key);
		if (value != null) 
			return Arrays.copyOf(value.getBytesUnsafe(), value.getLength());
		else
			return null;
	}
	
	protected int readInt(Store store, Transaction txn, ByteIterable key, int defaultValue) {
		byte[] bytes = readBytes(store, txn, key);
		if (bytes != null)
			return ByteBuffer.wrap(bytes).getInt();
		else
			return defaultValue;
	}
	
	protected boolean readBoolean(Store store, Transaction txn, ByteIterable key, boolean defaultValue) {
		byte[] bytes = readBytes(store, txn, key);
		if (bytes != null)
			return bytes[0] == 1;
		else
			return defaultValue;
	}
	
	protected void writeInt(Store store, Transaction txn, ByteIterable key, int value) {
		byte[] bytes = ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
		store.put(txn, key, new ArrayByteIterable(bytes));
	}
	
	protected long readLong(Store store, Transaction txn, ByteIterable key, long defaultValue) {
		byte[] bytes = readBytes(store, txn, key);
		if (bytes != null)
			return ByteBuffer.wrap(bytes).getLong();
		else
			return defaultValue;
	}
	
	protected void writeLong(Store store, Transaction txn, ByteIterable key, long value) {
		byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(value).array();
		store.put(txn, key, new ArrayByteIterable(bytes));
	}
	
	protected void writeBoolean(Store store, Transaction txn, ByteIterable key, boolean value) {
		byte[] bytes = new byte[] {(byte)(value?1:0)};
		store.put(txn, key, new ArrayByteIterable(bytes));
	}
	
	static class IntByteIterable extends ArrayByteIterable {
		IntByteIterable(int value) {
			super(new ArrayByteIterable(ByteBuffer.allocate(Integer.BYTES).putInt(value).array()));
		}
	}
	
	static class LongByteIterable extends ArrayByteIterable {
		LongByteIterable(long value) {
			super(new ArrayByteIterable(ByteBuffer.allocate(Long.BYTES).putLong(value).array()));
		}
	}
	
	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes(Charsets.UTF_8));
		}
	}

	static class StringPairByteIterable extends ArrayByteIterable {
		
		StringPairByteIterable(String value1, String value2) {
			super(getBytes(value1, value2));
		}
		
		private static byte[] getBytes(String value1, String value2) {
			byte[] bytes1 = value1.getBytes();
			byte[] bytes2 = value2.getBytes();
			byte[] bytes = new byte[bytes1.length+bytes2.length];
			System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
			System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
			return bytes;
		}
	}

}
