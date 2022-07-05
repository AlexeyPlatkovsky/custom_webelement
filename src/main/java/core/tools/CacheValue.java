package core.tools;

import core.tools.func.JFunc;

public class CacheValue<T> {

    private static ThreadLocal<Long> globalCache = new ThreadLocal<>();
    public long elementCache = 0;
    private T value;
    private JFunc<T> getRule = () -> null;

    public CacheValue() {
    }

    private static Long getGlobalCache() {
        if (globalCache.get() == null) {
            globalCache.set(0L);
        }
        return globalCache.get();
    }

    public T get() {
        return get(getRule);
    }

    public T get(JFunc<T> defaultResult) {
        if (!isUseCache()) {
            return defaultResult.execute();
        }
        if (elementCache < getGlobalCache() || value == null) {
            this.value = getRule.execute();
            elementCache = getGlobalCache();
        }
        return value;
    }

    public T setForce(T value) {
        elementCache = getGlobalCache();
        this.value = value;
        return value;
    }

    public boolean hasValue() {
        return isUseCache() && value != null && elementCache == getGlobalCache();
    }

    public boolean isUseCache() {
        return elementCache > -1;
    }
}
