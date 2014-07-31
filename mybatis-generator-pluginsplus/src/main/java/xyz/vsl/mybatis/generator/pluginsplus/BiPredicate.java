package xyz.vsl.mybatis.generator.pluginsplus;

/**
 * Backport of java.util.function.BiPredicate
 * @author Vladimir Lokhov
 */
interface BiPredicate<T,U> {

    public BiPredicate<T, U> and(final BiPredicate<? super T, ? super U> other);

    public BiPredicate<T, U> or(final BiPredicate<? super T, ? super U> other);

    public BiPredicate<T, U> negate();

    public boolean test(T t, U u);
}