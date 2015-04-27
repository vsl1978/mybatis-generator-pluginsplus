package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.List;

public interface PrimaryKeyMapper<K,M> {

    public M selectByPrimaryKey(K key);

}
