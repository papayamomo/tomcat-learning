package org.apache.catalina.util;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class ParameterMap extends HashMap<Object, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4869179604693909422L;

	public ParameterMap() {
		super();
	}

	public ParameterMap(int initialCapacity) {
		super(initialCapacity);
	}

	public ParameterMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	@SuppressWarnings("rawtypes")
	public ParameterMap(Map map) {
		super(map);
	}

	private boolean locked = false;

	public boolean isLocked() {
		return (this.locked);
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	private static final StringManager sm = StringManager
			.getManager("org.apache.catalina.util");

	public void clear() {
		if (locked)
			throw new IllegalStateException(sm.getString("parameterMap.locked"));
		super.clear();
	}

	public Object put(Object key, Object value) {
		if (locked)
			throw new IllegalStateException(sm.getString("parameterMap.locked"));
		return (super.put(key, value));
	}

	@SuppressWarnings("rawtypes")
	public void putAll(Map map) {
		if (locked)
			throw new IllegalStateException(sm.getString("parameterMap.locked"));
		super.putAll(map);
	}

	public Object remove(Object key) {
		if (locked)
			throw new IllegalStateException(sm.getString("parameterMap.locked"));
		return (super.remove(key));
	}

}
