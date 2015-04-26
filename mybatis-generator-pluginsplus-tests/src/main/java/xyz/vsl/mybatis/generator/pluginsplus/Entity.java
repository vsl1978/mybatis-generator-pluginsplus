package xyz.vsl.mybatis.generator.pluginsplus;

public class Entity<T extends Entity> {

    public T foo() {
        return (T)this;
    }

}