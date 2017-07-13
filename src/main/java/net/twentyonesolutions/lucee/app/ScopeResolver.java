package net.twentyonesolutions.lucee.app;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.CFMLFactory;
import lucee.runtime.PageContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Application;
import lucee.runtime.type.scope.Session;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ScopeResolver {

    final public static CFMLEngine engine;

    static {

        engine = CFMLEngineFactory.getInstance();
    }


    public static CFMLFactory getCfmlFactory(ServletConfig servletConfig){

        CFMLFactory result = null;

        try {
            result = engine.getCFMLFactory(servletConfig, null);
        }
        catch (NullPointerException | ServletException ex){
            // we could get NPE if CFMLFactory is not initialized for this ServletContext cause we're passing null for HttpServletRequest
        }

        return result;
    }


    public static Application getApplicationScope(ServletConfig servletConfig, String appName){

        CFMLFactory cfmlFactory = getCfmlFactory(servletConfig);
        if (cfmlFactory == null)
            return null;

        ClassLoader classLoader = cfmlFactory.getClass().getClassLoader();

        try {

            Class c_CfmlFactoryImpl = classLoader.loadClass("lucee.runtime.CFMLFactoryImpl");
            Method m_GetScopeContext = c_CfmlFactoryImpl.getDeclaredMethod("getScopeContext");

            Object scopeContext = m_GetScopeContext.invoke(cfmlFactory);

            Class c_ScopeContext = classLoader.loadClass("lucee.runtime.type.scope.ScopeContext");
            Method m_getAllApplicationScopes = c_ScopeContext.getDeclaredMethod("getAllApplicationScopes");

            Struct allApplications = (Struct)m_getAllApplicationScopes.invoke(scopeContext);

            Application result = (Application)allApplications.get(LuceeApps.toKey(appName), null);
            return result;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Session getSessionScope(ServletConfig servletConfig, String appName, String cfid){

        CFMLFactory cfmlFactory = getCfmlFactory(servletConfig);
        if (cfmlFactory == null)
            return null;

        ClassLoader classLoader = cfmlFactory.getClass().getClassLoader();

        try {

            Class c_CfmlFactoryImpl = classLoader.loadClass("lucee.runtime.CFMLFactoryImpl");
            Method m_GetScopeContext = c_CfmlFactoryImpl.getDeclaredMethod("getScopeContext");

            Object scopeContext = m_GetScopeContext.invoke(cfmlFactory);

            Class c_ScopeContext = classLoader.loadClass("lucee.runtime.type.scope.ScopeContext");
            Method m_getAllSessionScopes = c_ScopeContext.getDeclaredMethod("getAllSessionScopes", String.class);

            Struct allSessions = (Struct)m_getAllSessionScopes.invoke(scopeContext, appName);

            String sessionId = appName + "_" + cfid + "_0";

            Session result = (Session)allSessions.get(LuceeApps.toKey(sessionId), null);
            return result;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

}