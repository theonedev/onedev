package io.onedev.server.infomanager;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.FileUtils;
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
	
	protected void checkVersion(File envDir) {
		File versionFile = new File(envDir, VERSION_FILE);
		int versionFromFile;
		if (versionFile.exists()) {
			try {
				versionFromFile = Integer.parseInt(FileUtils.readFileToString(versionFile, Charset.defaultCharset()).trim());
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
	
	protected void writeVersion(File envDir) {
		File versionFile = new File(envDir, VERSION_FILE);
		FileUtils.writeFile(versionFile, String.valueOf(getEnvVersion()));
	}

	protected abstract int getEnvVersion();
	
	protected long getLogFileSize() {
		return DEFAULT_LOG_FILE_SIZE;
	}
	
	protected Environment newEnv(File envDir) {
		checkVersion(envDir);
		EnvironmentConfig config = new EnvironmentConfig();
		config.setEnvCloseForcedly(true);
		config.setMemoryUsagePercentage(MEMORY_USAGE_PERCENT);
		config.setLogFileSize(getLogFileSize());
		return Environments.newInstance(envDir, config);
	}
	
	protected Store getStore(Environment env, String storeName) {
		return env.computeInTransaction(new TransactionalComputable<Store>() {
		    @Override
		    public Store compute(Transaction txn) {
		        return env.openStore(storeName, StoreConfig.WITHOUT_DUPLICATES, txn);
		    }
		});		
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
	
	protected Collection<Long> readLongs(Store store, Transaction txn, ByteIterable key) {
		Collection<Long> collection = new HashSet<>();
		byte[] bytes = readBytes(store, txn, key);
		if (bytes != null) {
			for (int i=0; i<bytes.length/Long.BYTES; i++) 
				collection.add(ByteBuffer.wrap(bytes, i*Long.BYTES, Long.BYTES).getLong());
		} 
		return collection;
	}

	protected Collection<ObjectId> readCommits(Store store, Transaction txn, ByteIterable key) {
		Collection<ObjectId> commits = new HashSet<>();
		byte[] bytes = readBytes(store, txn, key);
		if (bytes != null) {
			for (int i=0; i<bytes.length/20; i++)
				commits.add(ObjectId.fromRaw(bytes, i*20));
		} 
		return commits;
	}
	
	protected void writeLong(Store store, Transaction txn, ByteIterable key, long value) {
		byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(value).array();
		store.put(txn, key, new ArrayByteIterable(bytes));
	}
	
	protected void writeLongs(Store store, Transaction txn, ByteIterable key, Collection<Long> collection) {
		store.put(txn, key, new LongsByteIterable(collection));
	}
	
	protected void writeCommits(Store store, Transaction txn, ByteIterable key, Collection<ObjectId> commits) {
		byte[] bytes = new byte[commits.size()*20];
		int index = 0;
		for (ObjectId commit: commits) {
			commit.copyRawTo(bytes, index);
			index += 20;
		}
		store.put(txn, key, new ArrayByteIterable(bytes));
	}
	
	protected void writeBoolean(Store store, Transaction txn, ByteIterable key, boolean value) {
		byte[] bytes = new byte[] {(byte)(value?1:0)};
		store.put(txn, key, new ArrayByteIterable(bytes));
	}

	static class CommitByteIterable extends ArrayByteIterable {
		CommitByteIterable(ObjectId commit) {
			super(getBytes(commit));
		}

		private static byte[] getBytes(ObjectId commit) {
			byte[] commitBytes = new byte[20];
			commit.copyRawTo(commitBytes, 0);
			return commitBytes;
		}
		
	}
	
	static class IntByteIterable extends ArrayByteIterable {
		IntByteIterable(int value) {
			super(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
		}
	}
	
	static class LongByteIterable extends ArrayByteIterable {
		LongByteIterable(long value) {
			super(ByteBuffer.allocate(Long.BYTES).putLong(value).array());
		}
	}
	
	static class StringByteIterable extends ArrayByteIterable {
		StringByteIterable(String value) {
			super(value.getBytes(StandardCharsets.UTF_8));
		}
	}

	static class LongsByteIterable extends ArrayByteIterable {
		
		LongsByteIterable(Collection<Long> values) {
			super(getBytes(values));
		}
		
		private static byte[] getBytes(Collection<Long> values) {
			ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE * values.size());
			int index = 0;
			for (Long value: values) {
				buffer.putLong(index, value);
				index += Long.SIZE;
			}
			return buffer.array();
		}
	}

}
