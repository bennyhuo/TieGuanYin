package com.bennyhuo.tieguanyin.runtime.core;

import com.bennyhuo.tieguanyin.runtime.core.FinderTestClasses.A$B;

import junit.framework.Assert;

import org.junit.Test;

public class BuilderClassFinderTest {

    @Test
    public void testFindBuilderClass(){
        A$B a$B = new A$B();
        Assert.assertEquals(BuilderClassFinder.findBuilderClassName(a$B), "com.bennyhuo.tieguanyin.runtime.core.FinderTestClasses.A$BBuilder");
        A$B.C c = new A$B.C();
        Assert.assertEquals(BuilderClassFinder.findBuilderClassName(c), "com.bennyhuo.tieguanyin.runtime.core.FinderTestClasses.A$B_CBuilder");
        A$B.C.D$E d$E = c.new D$E();
        Assert.assertEquals(BuilderClassFinder.findBuilderClassName(d$E), "com.bennyhuo.tieguanyin.runtime.core.FinderTestClasses.A$B_C_D$EBuilder");
    }
}
