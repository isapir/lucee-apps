package net.twentyonesolutions.lucee.app;

import lucee.commons.io.log.Log;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;

/**
 * Created by Igal on 10/5/2016.
 */
public class LuceeAppListener {

	Component listener;
	LuceeApp app;
	String componentPath;

	public LuceeAppListener(Component listener, LuceeApp app) {

		this.listener = listener;
		this.app = app;
	}

	public LuceeAppListener(String componentPath, LuceeApp app) {

		this.componentPath = componentPath;
		this.app = app;
	}

	public LuceeApp getApp() {
		return app;
	}

	public Component getComponent() {

		// TODO:
		// if (listener == null) {
		//
		// //* throws exception because PageSource of the PageContext is null:
		// lucee.runtime.exp.ApplicationException: The Lucee dialect is disabled, to enable the dialect set the
		// environment variable or system property "lucee.enable.dialect" to "true" or set the attribute
		// "allow-lucee-dialect" to "true" with the "compiler" tag inside the lucee-server.xml.
		// at lucee.runtime.PageContextImpl.notSupported(PageContextImpl.java:942)
		// at lucee.runtime.component.ComponentLoader._search(ComponentLoader.java:115)
		// at lucee.runtime.component.ComponentLoader._search(ComponentLoader.java:100)
		// at lucee.runtime.component.ComponentLoader.searchComponent(ComponentLoader.java:74)
		// at lucee.runtime.PageContextImpl.loadComponent(PageContextImpl.java:3010)
		// //*/
		//
		// listener = app.loadComponent(componentPath);
		// }

		return listener;
	}

	//
	//
	// /**
	// * returns true if the listener component has a method with the specified name
	// *
	// * @param key - the method's name as a Key
	// * @return
	// */
	// public boolean hasMethod(Collection.Key key){
	//
	// getComponent();
	//// return listener.get(key, null) instanceof Function;
	// return LuceeApps.hasMethod(listener, key);
	// }
	//
	//
	// /**
	// * a helper method that accepts a string instead of key and then calls hasMethod(Key)
	// *
	// * @param key - the method's name
	// * @return
	// */
	// public boolean hasMethod(String key){
	//
	// return hasMethod(LuceeApps.toKey(key));
	// }

	public Object invoke(Collection.Key methodName, Object... args) {

		if (!LuceeApps.hasMethod(this.getComponent(), methodName)) {
			return null;
		}

		PageContext pc = app.createPageContext();

		try {

			Object result = listener.call(pc, methodName, args);
			return result;
		}
		catch (Exception e) {

			app.log(Log.LEVEL_ERROR, e.toString(), app.getName(), "exception");
			e.printStackTrace();
			return e;
		}
		finally {
			LuceeAppsUtil.releasePageContext(pc);
		}
	} // */

	public Object invokeWithNamedArgs(Collection.Key methodName, Struct args) {

		if (!LuceeApps.hasMethod(this.getComponent(), methodName)) {
			return null;
		}

		PageContext pc = app.createPageContext();

		try {

			Object result = listener.callWithNamedValues(pc, methodName, args);
			return result;
		}
		catch (Exception e) {

			app.log(Log.LEVEL_ERROR, e.toString(), app.getName(), "exception");
			e.printStackTrace();
			return e;
		}
		finally {
			LuceeAppsUtil.releasePageContext(pc);
		}
	}

}