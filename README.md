mybatis-generator-pluginsplus
=============================

MyBatis Generator plugins

**AddComplexCriteriaPlugin**  
Adds custom criterion to specified *Example-class.

**AddCriteriaActionsPlugin**  
List&lt;Foo&gt; list = new FooExample().createCriteria().and.....list(sql);

**AnyCriteriaPlugin**  
new FooExample().createCriteria().andIf("lower(lastname)","like",lastName.toLowerCase());

**DistinctPlugin**  
List&lt;Integer&gt; list = Foo.distinctBarId(fooList);

**ExampleMethodsChainPlugin**  
sql.getMapper(FooMapper.class).selectByExample(  
    new FooExample().createCriteria().andBarIdEqualTo(123).criteria().setOrderByClause("id desc")  
);  
or  
new FooExample().setOrderByClause("id desc").createCriteria().andBarIdEqualTo(123).list(sql);  

**JoinPlugin**  
new FooExample().addFromClause("join bar b on b.id=foo.bar_id").createCriteria().andIf("b.code","=",....);

**MapPlugin**  
Map&lt;Integer, Foo&gt; map = Foo.mapByBarId(fooList);  
Map&lt;Integer, List&lt;Foo&gt;&gt; map = Foo.mapAllByBarId(fooList);

**ModelSettersChainPlugin**  
Foo foo = new Foo().setAnswer(42);

**NullableInCriteriaPlugin**  
Modifies Example class to allow using null and empty lists as parameter of andXxxIn(List) and andXxxNotIn(List) methods.

**PaginationPlugin**  
Adds limit and offset (top, first...skip, offset...row...fetch next, etc) clause to the Example class.

**SimpleOrCriteriaPlugin**  
Provides the ability to use criteria like "... and (a or b or c) and ..."

**ToStringPlugin**  
Generates a toString method that returns a string representation of all fields.

