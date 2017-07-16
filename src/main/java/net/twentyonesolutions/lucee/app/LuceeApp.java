package net.twentyonesolutions.lucee.app;

import lucee.commons.io.log.Log;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.scope.Application;
import lucee.runtime.type.scope.Session;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Igal on 9/30/2016.
 */
public class LuceeApp {

	public static final CFMLEngine engine = CFMLEngineFactory.getInstance();

	public static final Collection.Key KEY_GET_SERVLET_CONFIG = LuceeApps.toKey("getServletConfig");
	public static final Collection.Key KEY_GET_SERVLET_CONTEXT = LuceeApps.toKey("getServletContext");
	public static final Collection.Key KEY_GET_CONFIG_WEB = LuceeApps.toKey("getConfigWeb");
	public static final Collection.Key KEY_GET_APPLICATION_CONTEXT = LuceeApps.toKey("getApplicationContext");
	public static final Collection.Key KEY_GET_APPLICATION_SCOPE = LuceeApps.toKey("getApplicationScope");

	private ServletConfig servletConfig;
	private ServletContext servletContext; // WebContext, actually
	private ConfigWeb configWeb;
	private ApplicationContext applicationContext;

	private String rootDir;
	private File rootFile;
	private String httpHost;

	/** disable constructor */
	private LuceeApp() {
	}

	public static LuceeApp createFromPageContext(PageContext pc) {

		LuceeApp result = new LuceeApp();

		result.servletConfig = pc.getServletConfig();
		result.servletContext = pc.getServletContext();
		result.configWeb = pc.getConfig();
		result.applicationContext = pc.getApplicationContext();

		result.rootDir = result.servletContext.getRealPath("/");
		result.rootFile = new File(result.rootDir);

		try {

			result.httpHost = (String) pc.cgiScope().get(LuceeApps.toKey("HTTP_HOST"));
		}
		catch (PageException ex) {
		}

		return result;
	}


	public PageContext createPageContext(String cfid) {

		Cookie[] cookies = null;
		if (cfid != null && !cfid.isEmpty())
			cookies = new Cookie[] { new Cookie("cfid", cfid), new Cookie("cftoken", "0") };

		PageContext result = LuceeAppsUtil.createPageContext(this.rootDir
				,this.httpHost
				,"/"
				,""
				,cookies
				,Collections.EMPTY_MAP
				,Collections.EMPTY_MAP
				,Collections.EMPTY_MAP
				,System.out
				,60_000
				,true
				,this.applicationContext
		);

		return result;
	}

	public PageContext createPageContext() {

		return createPageContext(null);
	}


	public String getName() {
		return applicationContext.getName();
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public ConfigWeb getConfigWeb() {
		return configWeb;
	}

	public String getKey() {
		return rootDir + "@" + getName();
	}

	@Override
	public String toString() {
		return getKey();
	}

	public void log(int logLevel, String message, String appName, String logfileName) {

		Log l = getConfigWeb().getLog(logfileName);
		l.log(logLevel, appName, message);
	}

	public Application getApplicationScope() {

		Application result = ScopeResolver.getApplicationScope(this.servletConfig, this.applicationContext.getName());
		return result;
	}

	public Session getSessionScope(String cfid) {

		Session result = ScopeResolver.getSessionScope(this.servletConfig, this.applicationContext.getName(), cfid);
		return result;
	}


	public List<String> getLoggerNames() {

		try {
			// getLoggers() is inherited from ConfigImpl so we have to use getMethod() rather than getDeclaredMethod()
			Method method = getConfigWeb().getClass().getMethod("getLoggers");
			Map<String, Object> map = (Map) method.invoke(getConfigWeb());
			List<String> result = new ArrayList(map.keySet());
			return result;
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

}