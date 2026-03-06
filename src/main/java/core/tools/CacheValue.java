package core.tools;

import java.util.function.Supplier;

public class CacheValue<T> {

    private final static ThreadLocal<Long> globalCache = new ThreadLocal<>();
    public long elementCache = 0;
    private T value;
    private final Supplier<T> getRule = () -> null;

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

    public T get(Supplier<T> defaultResult) {
        if (!isUseCache()) {
            return defaultResult.get();
        }
        if (elementCache < getGlobalCache() || value == null) {
            this.value = getRule.get();
            elementCache = getGlobalCache();
        }
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
