package io.onedev.server.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MapProxy<K, V> implements Map<K, V>, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected final Map<K, V> delegate;
	
	public MapProxy(Map<K, V> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return delegate.get(key);
	}

	@Override
	public V put(K key, V value) {
		return delegate.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return delegate.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		delegate.putAll(m);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public Set<K> keySet() {
		return delegate.keySet();
	}

	@Override
	public Collection<V> values() {
		return delegate.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}
	
}
