package org.apache.catalina.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public final class Enumerator implements Enumeration<Object> {

	private Iterator<?> iterator = null;

	public Enumerator(Collection<?> collection) {
		this(collection.iterator());
	}

	public Enumerator(Iterator<?> iterator) {
		super();
		this.iterator = iterator;
	}

	public Enumerator(Map<?, ?> map) {
		this(map.values().iterator());
	}

	public boolean hasMoreElements() {
		return (iterator.hasNext());
	}

	public Object nextElement() throws NoSuchElementException {
		return (iterator.next());
	}

}
