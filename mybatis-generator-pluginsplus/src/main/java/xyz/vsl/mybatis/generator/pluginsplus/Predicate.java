package xyz.vsl.mybatis.generator.pluginsplus;

/**
 * Backport of java.util.function.Predicate (1.8)
 * @author Vladimir Lokhov
 */
interface Predicate<T> {
    Predicate<T> and(Predicate<? super T> other);

    Predicate<T> or(Predicate<? super T> other);

    Predicate<T> negate();

    boolean test(T t);
}
