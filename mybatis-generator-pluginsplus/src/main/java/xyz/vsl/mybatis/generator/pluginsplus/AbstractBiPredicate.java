package xyz.vsl.mybatis.generator.pluginsplus;

/**
 * backport of java.util.function.BiPredicate (1.8)
 * @author Vladimir Lokhov
 */
abstract class AbstractBiPredicate<T, U> implements BiPredicate<T, U> {

    public BiPredicate<T, U> and(final BiPredicate<? super T, ? super U> other) {
        final BiPredicate<T, U> that = this;
        return new AbstractBiPredicate<T, U>() {
            public boolean test(T t, U u) { return that.test(t, u) && other.test(t, u); };
        };
    }

    public BiPredicate<T, U> or(final BiPredicate<? super T, ? super U> other) {
        final BiPredicate<T, U> that = this;
        return new AbstractBiPredicate<T, U>() {
            public boolean test(T t, U u) { return that.test(t, u) || other.test(t, u); };
        };
    }

    public BiPredicate<T, U> negate() {
        final BiPredicate<T, U> that = this;
        return new AbstractBiPredicate<T, U>() {
            public boolean test(T t, U u) { return !that.test(t, u); };
        };
    }

    public abstract boolean test(T t, U u);

    private final static BiPredicate<?, ?> NONE = new AbstractBiPredicate<Object, Object>() {
        @Override public boolean test(Object t, Object u) { return false; }
    };

    @SuppressWarnings("unchecked")
    public final static <T, U> BiPredicate<T, U> none() {
        return (BiPredicate<T, U>)NONE;
    }

}
