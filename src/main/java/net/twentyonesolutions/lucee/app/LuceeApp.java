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
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 9/30/2016.
 */
public class LuceeApp {

    public static final CFMLEngine engine = CFMLEngineFactory.getInstance();

    public static final Collection.Key KEY_GET_SERVLET_CONFIG = LuceeApps.toKey("getServletConfig");
    public static final Collection.Key KEY_GET_SERVLET_CONTEXT = LuceeApps.toKey("getServletContext");
    public static final Collection.Key KEY_GET_CONFIG_WEB = LuceeApps.toKey("getConfigWeb");
    public static final Collection.Key KEY_GET_APPLICATION_CONTEXT = LuceeApps.toKey("getApplicationContext");
    public static final Collection.Key KEY_GET_APPLICATION_SCOPE = LuceeApps.toKey("getApplicationScope");

    private ServletConfig servletConfig;
    private ServletContext servletContext;  // WebContext, actually
    private ConfigWeb configWeb;
    private ApplicationContext applicationContext;
    private Application applicationScope;

    private File webRoot;
    private String httpHost;


    /** disable constructor */
    private LuceeApp(){}


    public static LuceeApp createFromPageContext(PageContext pc){

        LuceeApp result = new LuceeApp();

        result.servletConfig = pc.getServletConfig();
        result.servletContext = pc.getServletContext();
        result.configWeb = pc.getConfig();
        result.applicationContext = pc.getApplicationContext();

        result.webRoot = new File(result.configWeb.getRootDirectory().getAbsolutePath());

        try {

            result.applicationScope = pc.applicationScope();
            result.httpHost = (String)pc.cgiScope().get(LuceeApps.toKey("HTTP_HOST"));
        }
        catch (PageException ex){}

        return result;
    }


    /*
    public static Component loadComponent(String path, ApplicationContext applicationContext, String cfid) throws PageException {

        PageContext pc = createPageContext(applicationContext, cfid);
        Component result = pc.loadComponent(path);
        return result;
    }


    public Component loadComponent(String path, String cfid) {

        try {

            return loadComponent(path, this.applicationContext, cfid);
        }
        catch (PageException ex){
            // TODO: log
            ex.printStackTrace();
            return null;
        }
    }


    public Component loadComponent(String path) {

        return loadComponent(path, null);
    }
    //*/



    public static PageContext createPageContext(LuceeApp luceeApp, String cfid){

        Cookie[] cookies = null;
        if (cfid != null && !cfid.isEmpty())
            cookies = new Cookie[]{ new Cookie("cfid", cfid), new Cookie("cftoken", "0") };

        try {

            PageContext pc = engine.createPageContext(
                     luceeApp.webRoot                   // webroot  (new File("E:/Workspace/git/LuceeDebug/webapps/default/"))
                    ,luceeApp.httpHost                   // HOST, e.g. "localhost.com"
                    ,"/"                   // SCRIPT_NAME, e.g. "/websockets/test.cfm"
                    ,""                   // QUERY_STRING
                    ,cookies                // Cookies, cfid and cftoken are required for to retrieve Session
                    ,Collections.EMPTY_MAP  // headers, can also be null
                    ,Collections.EMPTY_MAP  // parameters
                    ,Collections.EMPTY_MAP  // attributes
                    ,System.out             // response stream where the output is written to
                    ,60_000                 // timeout for the simulated request in milli seconds
                    ,true                  // register the pc to the thread
            );

            if (luceeApp.applicationContext != null)
                pc.setApplicationContext(luceeApp.applicationContext);

            return pc;
        }
        catch (ServletException ex){
            // TODO: log
            ex.printStackTrace();
            return null;
        }
    }


    public PageContext createPageContext(String cfid){

//        return createPageContext(this.applicationContext, cfid);
        return createPageContext(this, cfid);
    }


    public PageContext createPageContext(){

//        return createPageContext(this.applicationContext, null);
        return createPageContext(this, null);
    }


    /*
    public static Object invoke(PageContext pc, Component component, Collection.Key methodName, Object... args){

        if (!LuceeApps.hasMethod(component, methodName))
            return null;

        try {

            Object result = component.call(pc, methodName, args);
            return result;
        }
        catch (Exception e){
            // TODO: log
            e.printStackTrace();
            return e;
        }
    } //*/


    public String getName(){
        return applicationContext.getName();
    }


    public ApplicationContext getApplicationContext(){
        return applicationContext;
    }


    public Application getApplicationScope(){
        return applicationScope;
    }


    public ServletConfig getServletConfig() { return servletConfig; }


    public ServletContext getServletContext(){
        return servletContext;
    }


    public ConfigWeb getConfigWeb() { return configWeb; }


    @Override
    public String toString(){
        return getName();
    }


    public void log(int logLevel, String message, String appName, String logfileName){

        Log l = getConfigWeb().getLog(logfileName);
        l.log(logLevel, appName, message);
    }


    public Session getSessionScope(String cfid){

        if (this.applicationContext == null)
            return null;

        Session result = null;
        PageContext pc = createPageContext(this, cfid);

        if (pc != null){

            try {
                result = pc.sessionScope();
            }
            catch (PageException e) {
                e.printStackTrace();
            }
            finally {
                this.releasePageContext(pc);
            }
        }

        return result;
    }


//    public Session getSessionScopeByReflection(String id){
//
//        if (this.applicationContext == null)
//            return null;
//
//        Session result = null;
//        PageContext pc = createPageContext(this, id);
//
//        if (pc != null){
//
//            try {
//                result = Reflection.getSessionScope(pc, id);
//            }
//            finally {
//                this.releasePageContext(pc);
//            }
//        }
//
//        return result;
//    }


    public void releasePageContext(PageContext pc){

        engine.releasePageContext(pc, true);
    }


    public List<String> getLoggerNames(){

        try {
            // getLoggers() is inherited from ConfigImpl so we have to use getMethod() rather than getDeclaredMethod()
            Method method = getConfigWeb().getClass().getMethod("getLoggers");
            Map<String, Object> map = (Map)method.invoke(getConfigWeb());
            List<String> result = new ArrayList(map.keySet());
            return result;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     *
     * @param obj
     * @param methodName
     * @param argTypes
     * @param args
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invokeByReflection(Object obj, String methodName, Class[] argTypes, Object[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class c = obj.getClass();
        Method method = c.getDeclaredMethod(methodName, argTypes);

        Object result = method.invoke(obj, args);
        return result;
    }

}