package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.List;

public interface ReadOnlyMapper<C,M> {

    public int countByExample(C example);

    public List<M> selectByExample(C example);

}
