package core.tools;

public class CacheValue<T> {

    private static final ThreadLocal<Long> GLOBAL_CACHE = new ThreadLocal<>();
    private long elementCache = 0;
    private T value;

    private static long getGlobalCache() {
        Long v = GLOBAL_CACHE.get();
        if (v == null) {
            GLOBAL_CACHE.set(0L);
            return 0L;
        }
        return v;
    }

    public T get() {
        return value;
    }

    public void setForce(T value) {
        elementCache = getGlobalCache();
        this.value = value;
    }

    public boolean hasValue() {
        return isUseCache() && value != null && elementCache == getGlobalCache();
    }

    public boolean isUseCache() {
        return elementCache > -1;
    }
}



