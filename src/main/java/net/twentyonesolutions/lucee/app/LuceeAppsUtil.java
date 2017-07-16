package net.twentyonesolutions.lucee.app;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;
import lucee.runtime.util.Creation;
import lucee.runtime.util.Decision;
import lucee.runtime.util.Operation;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public class LuceeAppsUtil {

    final public static CFMLEngine engine;

    static {

        engine = CFMLEngineFactory.getInstance();

    }


    public static PageContext createPageContext(
             String rootDir
            ,String httpHost
            ,String scriptName
            ,String queryString
            ,Cookie[] cookies
            ,Map headers
            ,Map parameters
            ,Map attributes
            ,OutputStream outputStream
            ,long requestTimeoutMs
            ,boolean registerToThread
            ,ApplicationContext applicationContext
            ) {

        File rootFile = new File(rootDir);

        try {

            PageContext pc = engine.createPageContext(
                     rootFile               // webroot (new File("E:/Workspace/git/LuceeDebug/webapps/default/"))
                    ,httpHost               // HOST, e.g. "localhost.com"
                    ,scriptName               // SCRIPT_NAME, e.g. "/websockets/test.cfm"
                    ,queryString // QUERY_STRING
                    ,cookies // Cookies, cfid and cftoken are required for to retrieve Session
                    ,headers // headers, can also be null
                    ,parameters // parameters
                    ,attributes // attributes
                    ,outputStream // response stream where the output is written to
                    ,requestTimeoutMs // timeout for the simulated request in milli seconds
                    ,registerToThread // register the pc to the thread
            );

            if (applicationContext != null)
                pc.setApplicationContext(applicationContext);

            return pc;
        }
        catch (ServletException ex) {
            // TODO: log
            ex.printStackTrace();
            return null;
        }
    }

    public static PageContext createPageContext(String rootDir, String cfid, ApplicationContext applicationContext){

        Cookie[] cookies = null;
        if (cfid != null && !cfid.isEmpty())
            cookies = new Cookie[] { new Cookie("cfid", cfid), new Cookie("cftoken", "0") };

        PageContext result = createPageContext(
                 rootDir
                ,"localhost"
                ,"/"
                ,""
                ,cookies
                ,Collections.EMPTY_MAP
                ,Collections.EMPTY_MAP
                ,Collections.EMPTY_MAP
                ,System.out
                ,60_000
                ,true
                ,applicationContext
        );

        return result;
    }

    public static PageContext createPageContext(){

        PageContext result = createPageContext(
                 "."
                ,"localhost"
                ,"/"
                ,""
                ,new Cookie[]{}
                ,Collections.EMPTY_MAP
                ,Collections.EMPTY_MAP
                ,Collections.EMPTY_MAP
                ,System.out
                ,60_000
                ,true
                ,null
        );

        return result;
    }


    public static void releasePageContext(PageContext pc) {

        engine.releasePageContext(pc, true);
    }


    public Object invokeWithPositionedArgs(Component component, Collection.Key methodName, Object... args) {

        if (!hasMethod(component, methodName)) {
            return null;
        }

        PageContext pc = createPageContext();

        try {

            Object result = component.call(pc, methodName, args);
            return result;
        }
        catch (Exception e) {

            e.printStackTrace();
            return e;
        }
        finally {
            releasePageContext(pc);
        }
    } // */


    public Object invokeWithNamedArgs(Component component, Collection.Key methodName, Struct args) {

        if (!hasMethod(component, methodName)) {
            return null;
        }

        PageContext pc = createPageContext();

        try {

            Object result = component.callWithNamedValues(pc, methodName, args);
            return result;
        }
        catch (Exception e) {

            e.printStackTrace();
            return e;
        }
        finally {
            releasePageContext(pc);
        }
    }




    public static Array createArray(){

        return getCreationUtil().createArray();
    }

    public static Struct createStruct(){

        return getCreationUtil().createStruct();
    }

    public static void populateStruct(Struct struct, java.util.function.Function filter, Object... args){

        if (args.length % 2 != 0)
            throw new IllegalArgumentException("args must be of even length");

        Collection.Key key;
        Object value;

        // args must have even size, where each key is followed by value
        for (int i = 0; i < args.length; i += 2) {

            // even index is the key, which must be either Collection.Key or String
            key = (args[i] instanceof Collection.Key) ?
                (Collection.Key) args[i]
                    :
                toKey((String) args[i]);

            value = args[i + 1];

            if (filter != null)
                value = filter.apply(value);

            struct.setEL(key, value);
        }
    }


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
    public Object invokeByReflection(Object obj, String methodName, Class[] argTypes, Object[] args)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class c = obj.getClass();
        Method method = c.getDeclaredMethod(methodName, argTypes);

        Object result = method.invoke(obj, args);
        return result;
    }

}
