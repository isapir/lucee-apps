package net.twentyonesolutions.lucee.app;

import lucee.runtime.PageContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Session;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class Reflection {


    public static Session getSessionScope(PageContext pc, String cfid){

        Session result = null;

        try {

            ClassLoader classLoader = pc.getClass().getClassLoader();

            Class cCfmlFactory = classLoader.loadClass("lucee.runtime.CFMLFactoryImpl");
            Object cfmlFactory = pc.getCFMLFactory();

            Method mGetScopeContext = cCfmlFactory.getDeclaredMethod("getScopeContext");
            Object scopeContext = mGetScopeContext.invoke(cfmlFactory);

            Class cScopeContext = classLoader.loadClass("lucee.runtime.type.scope.ScopeContext");
            Method mGetAllSessionScopes = cScopeContext.getDeclaredMethod("getAllSessionScopes", String.class);

            String applicationName = pc.getApplicationContext().getName();

            Struct allSessions = (Struct)mGetAllSessionScopes.invoke(scopeContext, applicationName);

            Collection.Key sessionId = LuceeApps.toKey(pc.getApplicationContext().getName() + "_" + cfid + "_0");

            result = (Session)allSessions.get(sessionId, null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return result;
    }


//    public boolean keepSessionAlive(Session sessionScope){
//
//        sessionScope.touch();
//    }

}