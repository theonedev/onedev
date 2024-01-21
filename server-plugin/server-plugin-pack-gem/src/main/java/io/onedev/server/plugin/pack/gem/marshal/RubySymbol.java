package io.onedev.server.plugin.pack.gem.marshal;

public class RubySymbol implements CharSequence, Cloneable {

	private final String name;

	public RubySymbol(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public char charAt(int index) {
		return name.charAt(index);
	}

	@Override
	public int length() {
		return name.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return name.subSequence(start, end);
	}

	@Override
	public String toString () {
		return name;
	}

	@Override
	protected Object clone() {
		return new RubySymbol(name);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof RubySymbol) && o.toString().equals(name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}