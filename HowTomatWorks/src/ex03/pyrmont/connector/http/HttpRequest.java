package ex03.pyrmont.connector.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;

import ex03.pyrmont.connector.RequestStream;

public class HttpRequest implements HttpServletRequest {

	private String contentType;
	private int contentLength;
	private InputStream input;
	private String method;
	private String protocol;
	private String queryString;
	private String requestURI;

	protected HashMap<String, String> attributes = new HashMap<String, String>();
	protected String authorization = null;
	protected String contextPath = "";
	protected ArrayList<Cookie> cookies = new ArrayList<Cookie>();
	protected static ArrayList<String> empty = new ArrayList<String>();

	protected SimpleDateFormat formats[] = {
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };

	protected HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	protected ParameterMap parameters = null;

	protected boolean parsed = false;
	protected String pathInfo = null;
	protected BufferedReader reader = null;

	protected ServletInputStream stream = null;

	public HttpRequest(InputStream input) {
		this.input = input;
	}

	public void addHeader(String name, String value) {
		name = name.toLowerCase();
		synchronized (headers) {
			ArrayList<String> values = (ArrayList<String>) headers.get(name);
			if (values == null) {
				values = new ArrayList<String>();
				headers.put(name, values);
			}
			values.add(value);
		}
	}

	protected void parseParameters() {
		if (parsed)
			return;
		ParameterMap results = parameters;
		if (results == null)
			results = new ParameterMap();
		results.setLocked(false);
		String encoding = getCharacterEncoding();
		if (encoding == null)
			encoding = "ISO-8859-1";

		String queryString = getQueryString();
		try {
			RequestUtil.parseParameters(results, queryString, encoding);
		} catch (UnsupportedEncodingException e) {
			;
		}

		String contentType = getContentType();
		if (contentType == null)
			contentType = "";
		int semicolon = contentType.indexOf(';');
		if (semicolon >= 0) {
			contentType = contentType.substring(0, semicolon).trim();
		} else {
			contentType = contentType.trim();
		}
		if ("POST".equals(getMethod()) && (getContentLength() > 0)
				&& "application/x-www-form-urlencoded".equals(contentType)) {
			try {
				int max = getContentLength();
				int len = 0;
				byte buf[] = new byte[getContentLength()];
				ServletInputStream is = getInputStream();
				while (len < max) {
					int next = is.read(buf, len, max - len);
					if (next < 0) {
						break;
					}
					len += next;
				}
				is.close();
				if (len < max) {
					throw new RuntimeException("Content length mismatch");
				}
				RequestUtil.parseParameters(results, buf, encoding);
			} catch (UnsupportedEncodingException ue) {
				;
			} catch (IOException e) {
				throw new RuntimeException("Content read fail");
			}
		}

		results.setLocked(true);
		parsed = true;
		parameters = results;
	}

	public void addCookie(Cookie cookie) {
		synchronized (cookies) {
			cookies.add(cookie);
		}
	}

	public ServletInputStream createInputStream() throws IOException {
		return (new RequestStream(this));
	}

	public InputStream getStream() {
		return input;
	}

	public void setContentLength(int length) {
		this.contentLength = length;
	}

	public void setContentType(String type) {
		this.contentType = type;
	}

	public void setInet(InetAddress inetAddress) {
	}

	public void setContextPath(String path) {
		if (path == null)
			this.contextPath = "";
		else
			this.contextPath = path;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setPathInfo(String path) {
		this.pathInfo = path;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public void setServerName(String name) {
	}

	public void setServerPort(int port) {
	}

	public void setSocket(Socket socket) {
	}

	public void setRequestedSessionCookie(boolean flag) {
	}

	public void setRequestedSessionId(String requestedSessionId) {
	}

	public void setRequestedSessionURL(boolean flag) {
	}

	public Object getAttribute(String name) {
		synchronized (attributes) {
			return (attributes.get(name));
		}
	}

	public Enumeration getAttributeNames() {
		synchronized (attributes) {
			return (new Enumerator(attributes.keySet()));
		}
	}

	public String getAuthType() {
		return null;
	}

	public String getCharacterEncoding() {
		return null;
	}

	public int getContentLength() {
		return contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public String getContextPath() {
		return contextPath;
	}

	public Cookie[] getCookies() {
		synchronized (cookies) {
			if (cookies.size() < 1)
				return (null);
			Cookie results[] = new Cookie[cookies.size()];
			return ((Cookie[]) cookies.toArray(results));
		}
	}

	public long getDateHeader(String name) {
		String value = getHeader(name);
		if (value == null)
			return (-1L);

		value += " ";

		for (int i = 0; i < formats.length; i++) {
			try {
				Date date = formats[i].parse(value);
				return (date.getTime());
			} catch (ParseException e) {
				;
			}
		}
		throw new IllegalArgumentException(value);
	}

	public String getHeader(String name) {
		name = name.toLowerCase();
		synchronized (headers) {
			ArrayList<String> values = (ArrayList<String>) headers.get(name);
			if (values != null)
				return ((String) values.get(0));
			else
				return null;
		}
	}

	public Enumeration getHeaderNames() {
		synchronized (headers) {
			return (new Enumerator(headers.keySet()));
		}
	}

	public Enumeration getHeaders(String name) {
		name = name.toLowerCase();
		synchronized (headers) {
			ArrayList<String> values = (ArrayList<String>) headers.get(name);
			if (values != null)
				return (new Enumerator(values));
			else
				return (new Enumerator(empty));
		}
	}

	public ServletInputStream getInputStream() throws IOException {
		if (reader != null)
			throw new IllegalStateException("getInputStream has been called");

		if (stream == null)
			stream = createInputStream();
		return (stream);
	}

	public int getIntHeader(String name) {
		String value = getHeader(name);
		if (value == null)
			return (-1);
		else
			return (Integer.parseInt(value));
	}

	public Locale getLocale() {
		return null;
	}

	public Enumeration getLocales() {
		return null;
	}

	public String getMethod() {
		return method;
	}

	public String getParameter(String name) {
		parseParameters();
		String values[] = (String[]) parameters.get(name);
		if (values != null)
			return (values[0]);
		else
			return (null);
	}

	public Map getParameterMap() {
		parseParameters();
		return (this.parameters);
	}

	public Enumeration getParameterNames() {
		parseParameters();
		return (new Enumerator(parameters.keySet()));
	}

	public String[] getParameterValues(String name) {
		parseParameters();
		String values[] = (String[]) parameters.get(name);
		if (values != null)
			return (values);
		else
			return null;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public String getPathTranslated() {
		return null;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getQueryString() {
		return queryString;
	}

	public BufferedReader getReader() throws IOException {
		if (stream != null)
			throw new IllegalStateException("getInputStream has been called.");
		if (reader == null) {
			String encoding = getCharacterEncoding();
			if (encoding == null)
				encoding = "ISO-8859-1";
			InputStreamReader isr = new InputStreamReader(createInputStream(),
					encoding);
			reader = new BufferedReader(isr);
		}
		return (reader);
	}

	public String getRealPath(String path) {
		return null;
	}

	public String getRemoteAddr() {
		return null;
	}

	public String getRemoteHost() {
		return null;
	}

	public String getRemoteUser() {
		return null;
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	public String getScheme() {
		return null;
	}

	public String getServerName() {
		return null;
	}

	public int getServerPort() {
		return 0;
	}

	public String getRequestedSessionId() {
		return null;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public StringBuffer getRequestURL() {
		return null;
	}

	public HttpSession getSession() {
		return null;
	}

	public HttpSession getSession(boolean create) {
		return null;
	}

	public String getServletPath() {
		return null;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	public boolean isRequestedSessionIdValid() {
		return false;
	}

	public boolean isSecure() {
		return false;
	}

	public boolean isUserInRole(String role) {
		return false;
	}

	public void removeAttribute(String attribute) {
	}

	public void setAttribute(String key, Object value) {
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public void setCharacterEncoding(String encoding)
			throws UnsupportedEncodingException {
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}
}
