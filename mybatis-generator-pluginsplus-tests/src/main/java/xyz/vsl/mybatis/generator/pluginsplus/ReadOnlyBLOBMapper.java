package xyz.vsl.mybatis.generator.pluginsplus;

import java.util.List;

public interface ReadOnlyBLOBMapper<C,M> {

    public List<M> selectByExampleWithBLOBs(C example);

}
