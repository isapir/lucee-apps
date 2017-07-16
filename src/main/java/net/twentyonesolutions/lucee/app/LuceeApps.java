package net.twentyonesolutions.lucee.app;

import lucee.commons.io.log.Log;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Collection;
import lucee.runtime.util.Cast;
import lucee.runtime.util.Creation;
import lucee.runtime.util.Decision;
import lucee.runtime.util.Operation;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Igal on 9/30/2016.
 */
public class LuceeApps {

	final static Map<String, LuceeApp> luceeApps; // <appName, LuceeApp>
	final static Map<String, LuceeAppListener> appListeners; // <key, LuceeAppListener>
	final public static CFMLEngine engine;
	// final public static Observable observable;

	static {

		engine = CFMLEngineFactory.getInstance();
		luceeApps = new ConcurrentHashMap();
		appListeners = new ConcurrentHashMap();
		// observable = new Observable(LuceeApps.class);
	}

	public static LuceeApp registerApp(LuceeApp app, String key) {

		LuceeApp previousApp = luceeApps.put(key, app);

		// observable.notify("LuceeAppUpdated", key, app, previousApp);

		return app;
	}

	public static LuceeApp registerApp(LuceeApp app) {

		return registerApp(app, app.getKey());
	}

	public static LuceeApp getAppByName(String name) {

		LuceeApp result = luceeApps.get(name);
		return result;
	}

	public static LuceeAppListener getAppListener(String key) {

		return appListeners.get(key);
	}

	public static LuceeAppListener registerListener(LuceeApp app, Component component, String key) {

		LuceeAppListener listener = new LuceeAppListener(component, app);
		appListeners.put(key, listener);

		return listener;
	}

	public static LuceeAppListener registerListener(LuceeApp app, String componentPath, String key) {

		LuceeAppListener listener = new LuceeAppListener(componentPath, app);
		appListeners.put(key, listener);

		return listener;
	}

	public static LuceeApp registerAppFromPageContext(PageContext pc) {

		LuceeApp app = LuceeApp.createFromPageContext(pc);
		return registerApp(app);
	}


	public static boolean hasMethod(LuceeAppListener appListener, Collection.Key method) {

		return LuceeApps.hasMethod(appListener.getComponent(), method);
	}

	public static void log(String appListenerKey, int logLevel, String message, String appName, String logfileName) {

		LuceeAppListener appListener = appListeners.get(appListenerKey);

		if (appListener != null) {

			LuceeApp luceeApp = appListener.getApp();
			Log l = luceeApp.getConfigWeb().getLog(logfileName);
			l.log(logLevel, appName, message);
		}
	}


	// <editor-fold desc="Util Methods">
	// TODO: moved to LuceeAppsUtil -- remove from here

	public static Collection.Key toKey(String key) {

		return getCastUtil().toKey(key);
	}

	public static PageException toPageException(Throwable t) {

		return getCastUtil().toPageException(t);
	}

	public static Cast getCastUtil() {

		return engine.getCastUtil();
	}

	public static Creation getCreationUtil() {

		return engine.getCreationUtil();
	}

	public static Decision getDecisionUtil() {

		return engine.getDecisionUtil();
	}

	public static Operation getOperationUtil() {

		return engine.getOperatonUtil();
	}

	/**
	 * returns true if the object is a cfml boolean and is false
	 *
	 * @param obj
	 * @return
	 */
	public static boolean isBooleanFalse(Object obj) {

		if (getDecisionUtil().isBoolean(obj)) {
			try {
				boolean result = getCastUtil().toBoolean(obj);
				return (result == false);
			}
			catch (Exception ex) {
			}
		}

		return false;
	}

	/**
	 * returns true if the object is a cfml boolean and is true
	 *
	 * @param obj
	 * @return
	 */
	public static boolean isBooleanTrue(Object obj) {

		if (getDecisionUtil().isBoolean(obj)) {
			try {
				boolean result = getCastUtil().toBoolean(obj);
				return (result == true);
			}
			catch (Exception ex) {
			}
		}

		return false;
	}

	/**
	 * returns true if the component has a method with the specified name
	 *
	 * @param method
	 *            - the method's name as a Key
	 * @return
	 */
	public static boolean hasMethod(Component component, Collection.Key method) {

		return (component.get(method, null) instanceof Function);
	}

	// </editor-fold>
}