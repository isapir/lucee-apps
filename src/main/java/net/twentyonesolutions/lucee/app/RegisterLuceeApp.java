package net.twentyonesolutions.lucee.app;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Admin on 12/5/2016.
 */
public class RegisterLuceeApp extends BIF {


    public static Object call(PageContext pc){

        LuceeApp app = LuceeApp.createFromPageContext(pc);
        LuceeApps.registerApp(app);

        return app;
    }


    public static Object call(PageContext pc, Struct listeners){

        LuceeApp app = (LuceeApp) call(pc);

        Iterator<Map.Entry<Collection.Key, Object>> it = listeners.entryIterator();
        while (it.hasNext()){

            Map.Entry<Collection.Key, Object> entry = it.next();
            Collection.Key key = entry.getKey();
            Object value = entry.getValue();

            if (LuceeApps.getDecisionUtil().isComponent(value)){
                LuceeApps.registerListener(app, (Component)value, key.getString());
            }
            else if (value instanceof String){
                // TODO: currently can't load component from path cause PageContext's PageSource is null
                throw new RuntimeException("Currently can't load component from path cause PageContext's PageSource is null. Pass a Component rather than a path to one.");
//                LuceeApps.registerListener(app, (String)value, key.getString());
            }

            // TODO: log
            System.out.printf("%s >>> %s %s\r\n", RegisterLuceeApp.class.getName(), key, value);
        }

        return app;
    }


    @Override
    public Object invoke(PageContext pc, Object[] args) throws PageException {

        if (args.length == 0)
            return call(pc);

        return call(pc, LuceeApps.getCastUtil().toStruct(args[0]));
    }

}