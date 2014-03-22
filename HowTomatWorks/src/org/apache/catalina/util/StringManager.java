package org.apache.catalina.util;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class StringManager {

	private ResourceBundle bundle;

	private StringManager(String packageName) {
		String bundleName = packageName + ".LocalStrings";
		bundle = ResourceBundle.getBundle(bundleName);
	}

	public String getString(String key) {
		if (key == null) {
			String msg = "key is null";
			throw new NullPointerException(msg);
		}
		String str = null;

		try {
			str = bundle.getString(key);
		} catch (MissingResourceException mre) {
			str = "Cannot find message associated with key '" + key + "'";
		}

		return str;
	}

	public String getString(String key, Object[] args) {
		String iString = null;
		String value = getString(key);

		try {
			Object nonNullArgs[] = args;
			for (int i = 0; i < args.length; i++) {
				if (args[i] == null) {
					if (nonNullArgs == args) {
						nonNullArgs = (Object[]) args.clone();
					}
					nonNullArgs[i] = "null";
				}
			}

			iString = MessageFormat.format(value, nonNullArgs);
		} catch (IllegalArgumentException iae) {
			StringBuffer buf = new StringBuffer();
			buf.append(value);
			for (int i = 0; i < args.length; i++) {
				buf.append(" arg[" + i + "]=" + args[i]);
			}
			iString = buf.toString();
		}
		return iString;
	}

	public String getString(String key, Object arg1, Object arg2) {
		Object[] args = new Object[] { arg1, arg2 };
		return getString(key, args);
	}

	public String getString(String key, Object arg1, Object arg2, Object arg3,
			Object arg4) {
		Object[] args = new Object[] { arg1, arg2, arg3, arg4 };
		return getString(key, args);
	}

	private static Hashtable<String, StringManager> managers = new Hashtable<String, StringManager>();

	public synchronized static StringManager getManager(String packageName) {
		StringManager mgr = (StringManager) managers.get(packageName);
		if (mgr == null) {
			mgr = new StringManager(packageName);
			managers.put(packageName, mgr);
		}
		return mgr;
	}
}
