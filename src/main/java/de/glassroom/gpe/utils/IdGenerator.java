package de.glassroom.gpe.utils;

import java.util.Date;

public class IdGenerator {
    // private static final Map<String, AtomicLong> COUNTERS;
    
    /*static {
        COUNTERS = new ConcurrentHashMap<>();
    }*/
    
    public static synchronized String generateId(String prefix) {
        // if (!COUNTERS.containsKey(prefix)) {
        //	COUNTERS.put(prefix, new AtomicLong(0l));
        //}
        //return prefix + COUNTERS.get(prefix).getAndIncrement();
        Date date = new Date();
        return prefix + Long.toHexString(date.getTime());
    }
}
