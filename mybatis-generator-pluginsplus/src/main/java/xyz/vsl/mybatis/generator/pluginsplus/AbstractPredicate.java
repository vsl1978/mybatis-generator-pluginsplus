package xyz.vsl.mybatis.generator.pluginsplus;

/**
 * backport of java.util.function.Predicate (1.8)
 * @author Vladimir Lokhov
 */
abstract class AbstractPredicate<T> implements Predicate<T> {

    @Override
    public Predicate<T> and(final Predicate<? super T> other) {
        final Predicate<T> that = this;
        return new AbstractPredicate<T>() {
            public boolean test(T t) { return that.test(t) && other.test(t); };
        };
    }

    @Override
    public Predicate<T> or(final Predicate<? super T> other) {
        final Predicate<T> that = this;
        return new AbstractPredicate<T>() {
            public boolean test(T t) { return that.test(t) || other.test(t); };
        };
    }

    @Override
    public Predicate<T> negate() {
        final Predicate<T> that = this;
        return new AbstractPredicate<T>() {
            public boolean test(T t) { return !that.test(t); };
        };
    }

    private final static Predicate<?> NONE = new AbstractPredicate<Object>() {
        @Override public boolean test(Object t) { return false; }
    };

    @SuppressWarnings("unchecked")
    public final static <T> Predicate<T> none() {
        return (Predicate<T>)NONE;
    }

}
