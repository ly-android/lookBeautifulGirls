package com.allen.mm.app.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 描述:conmunicate with others
 *
 * @author: liyong on 2015/1/16
 */
public class ClientFactory {
    public static final HashMap<Class<?>, CopyOnWriteArrayList<Object>> clients = new HashMap<Class<?>, CopyOnWriteArrayList<Object>>();

    public static void addClients(Object obj) {
        if (obj == null)
            return;
        Class<?> cls = obj.getClass();
        addClients(cls, obj);
    }

    private synchronized static void addClients(Class<?> cls, Object obj) {
        if (cls == null)
            return;
        if (!clients.containsKey(cls)) {
            CopyOnWriteArrayList<Object> objs = new CopyOnWriteArrayList<Object>();
            objs.add(obj);
            clients.put(cls, objs);
        } else {
            CopyOnWriteArrayList<Object> objs = clients.get(cls);
            objs.add(obj);
        }
        // super interfaces
        Class<?>[] interfaces = cls.getInterfaces();
        for (Class<?> class1 : interfaces) {
            addClients(class1, obj);
        }
        // super classes
        addClients(cls.getSuperclass(), obj);
    }

    public synchronized static void removeClients(Class<?> cls) {
        if (clients.containsKey(cls)) {
            clients.remove(cls);
        }
    }

    public synchronized static void removeClients(Object obj) {
        Collection<CopyOnWriteArrayList<Object>> values = clients.values();
        for (CopyOnWriteArrayList<Object> copyOnWriteArrayList : values) {
            copyOnWriteArrayList.remove(obj);
        }
    }

    public interface IClientCall<T> {
        public void onCall(T obj);
    }

    /**
     * find all registered client
     *
     * @param cls
     * @param iCall
     */
    public synchronized static <T> void notifyClients(Class<?> cls,
                                                      IClientCall<T> iCall) {
        if (clients.containsKey(cls)) {
            CopyOnWriteArrayList<Object> objs = clients.get(cls);
            System.out.println("clients size==" + objs.size());
            for (Object object : objs) {
                if (iCall != null)
                    iCall.onCall((T) object);
            }
        }
    }
}
