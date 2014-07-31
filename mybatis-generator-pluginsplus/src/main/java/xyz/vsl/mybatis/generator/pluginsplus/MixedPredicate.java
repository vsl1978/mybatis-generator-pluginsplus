package xyz.vsl.mybatis.generator.pluginsplus;

/**
 * @author Vladimir Lokhov
 */
abstract class MixedPredicate<T,U> implements Predicate<T>, BiPredicate<T, U> {

    @Override
    public Predicate<T> and(final Predicate<? super T> other) {
        final MixedPredicate<T,U> that = this;
        return new MixedPredicate<T,U> () {
            public boolean test(T t) { return that.test(t) && other.test(t); };
            public boolean test(T t, U u) { return that.test(t, u) && other.test(t); };
        };
    }

    @Override
    public BiPredicate<T,U> and(final BiPredicate<? super T, ? super U> other) {
        final MixedPredicate<T,U> that = this;
        return new MixedPredicate<T,U> () {
            public boolean test(T t) { return that.test(t) && ((Predicate<T>)other).test(t); };
            public boolean test(T t, U u) { return that.test(t, u) && other.test(t, u); };
        };
    }

    @Override
    public Predicate<T> or(final Predicate<? super T> other) {
        final MixedPredicate<T,U> that = this;
        return new MixedPredicate<T,U>() {
            public boolean test(T t) { return that.test(t) || other.test(t); };
            public boolean test(T t, U u) { return that.test(t, u) || other.test(t); };
        };
    }

    @Override
    public BiPredicate<T,U> or(final BiPredicate<? super T,? super U> other) {
        final MixedPredicate<T,U> that = this;
        return new MixedPredicate<T,U>() {
            public boolean test(T t) { return that.test(t) || ((Predicate<T>)other).test(t); };
            public boolean test(T t, U u) { return that.test(t, u) || other.test(t, u); };
        };
    }

    @Override
    public MixedPredicate<T,U> negate() {
        final MixedPredicate<T,U> that = this;
        return new MixedPredicate<T,U>() {
            public boolean test(T t) { return !that.test(t); };
            public boolean test(T t, U u) { return !that.test(t, u); };
        };
    }

}
