package io.onedev.server.plugin.pack.gem.marshal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Marshaller {
	
	private final OutputStream stream;
	
	private final List<String> symbolCache = new ArrayList<>();

	public Marshaller(OutputStream stream) {
		this.stream = stream;
	}
	
	public void marshal(Object value) {
		try {
			stream.write(new byte[]{4, 8});
			marshalValue(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void marshalValue(Object value) throws IOException {
		if (value == null) {
			marshalNull();
		} else if (value instanceof Boolean) {
			marshalBoolean((Boolean) value);
		} else if (value instanceof Long) {
			marshalLong((Long) value);
		} else if (value instanceof Integer) {
			marshalLong((Integer) value);
		} else if (value instanceof String) {
			marshalString((String) value);
		} else if (value instanceof RubySymbol) {
			marshalSymbol(((RubySymbol) value).getName());
		} else if (value.getClass().isArray()) {
			marshalArray((Object[]) value);
		} else if (value instanceof List) {
			marshalList((List<?>) value);
		} else if (value instanceof Map) {
			marshalMap((Map<?, ?>) value);
		} else if (value instanceof UserMarshal) {
			marshalUserMarshal((UserMarshal) value);
		} else if (value instanceof UserDefined) {
			marshalUserDefined((UserDefined) value);
		} else if (value instanceof RubyObject) {
			marshalRubyObject((RubyObject) value);
		} else {
			throw new RuntimeException("not supported");
		}
	}

	private void marshalNull() throws IOException {
		stream.write('0');
	}

	private void marshalBoolean(boolean value) throws IOException {
		stream.write(value ? 'T' : 'F');
	}

	private void marshalLong(long value) throws IOException {
		if (value > 1073741823L || value < -1073741824L) {
			stream.write('l');
			writeBigInteger(value);
		} else {
			stream.write('i');
			writeInt((int) value);
		}
	}

	private void marshalString(String value) throws IOException {
		stream.write('I');
		stream.write('"');
		writeBytes(value.getBytes(UTF_8));		
		writeInt(1);
		marshalSymbol("E");
		marshalBoolean(true);
	}

	private void marshalArray(Object[] value) throws IOException {
		stream.write('[');
		writeArray(value);
	}

	private void marshalList(List<?> value) throws IOException {
		stream.write('[');
		writeList(value);
	}

	private void marshalMap(Map<?, ?> value) throws IOException {
		stream.write('{');
		writeMap(value);
	}

	private void marshalSymbol(String value) throws IOException {
		int i = symbolCache.indexOf(value);
		if (i != -1) {
			stream.write(';');
			writeInt(i);
			return;
		}

		symbolCache.add(value);
		stream.write(':');
		writeBytes(value.getBytes(UTF_8));
	}
	
	private void marshalUserMarshal(UserMarshal value) throws IOException {
		stream.write('U');
		marshalSymbol(value.getName());
		marshalValue(value.getValue());
	}

	private void marshalUserDefined(UserDefined value) throws IOException {
		stream.write('u');
		marshalSymbol(value.getName());
		
		var baos = new ByteArrayOutputStream();
		new Marshaller(baos).marshal(value.getValue());
		writeBytes(baos.toByteArray());
	}

	private void marshalRubyObject(RubyObject value) throws IOException {
		stream.write('o');
		marshalSymbol(value.getName());
		writeInt(value.getMembers().size());
		for (var member: value.getMembers().entrySet()) {
			marshalSymbol(member.getKey());
			marshalValue(member.getValue());
		}
	}
	
	private void writeList(List<?> list) throws IOException {
		writeInt(list.size());
		for (Object o : list) {
			marshalValue(o);
		}
	}

	private void writeArray(Object[] list) throws IOException {
		writeInt(list.length);
		for (Object o : list) {
			marshalValue(o);
		}
	}

	private void writeMap(Map<?, ?> map) throws IOException {
		writeInt(map.size());
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			marshalValue(entry.getKey());
			marshalValue(entry.getValue());
		}
	}

	private void writeBytes(byte[] value) throws IOException {
		writeInt(value.length);
		stream.write(value);
	}

	private void writeInt(int value) throws IOException {
		if (value == 0) {
			stream.write(0);
		} else if (0 < value && value < 123) {
			stream.write(value + 5);
		} else if (-124 < value && value < 0) {
			stream.write((value - 5) & 0xff);
		} else {
			byte[] buf = new byte[4];
			int i = 0;
			do {
				buf[i++] = (byte) (value & 0xff);
				value >>= 8;
			} while (i < buf.length && value != 0 && value != -1);
			stream.write(value < 0 ? -i : i);
			stream.write(buf, 0, i);
		}
	}

	private void writeBigInteger(long bigint) throws IOException {
		stream.write(bigint >= 0 ? '+' : '-');

		long absValue = Math.abs(bigint);

		byte[] digits;
		int size;
		if (bigint < 0x100L)
			size = 1;
		else if (bigint < 0x10000L)
			size = 2;
		else if (bigint < 0x1000000L)
			size = 3;
		else if (bigint < 0x100000000L)
			size = 4;
		else if (bigint < 0x10000000000L)
			size = 5;
		else if (bigint < 0x1000000000000L)
			size = 6;
		else if (bigint < 0x100000000000000L)
			size = 7;
		else
			size = 8;
		digits = new byte[size];
		for (int i = 0; i < size; ++i) {
			digits[i] = (byte) (absValue >> (i << 3));
		}

		boolean oddLengthNonzeroStart = (digits.length % 2 != 0 && digits[0] != 0);
		int shortLength = digits.length / 2;
		if (oddLengthNonzeroStart) {
			shortLength++;
		}
		writeInt(shortLength);

		for (int i = 0; i < shortLength * 2 && i < digits.length; i++) {
			stream.write(digits[i]);
		}

		if (oddLengthNonzeroStart) {
			// Pad with a 0
			stream.write(0);
		}
	}
	
}