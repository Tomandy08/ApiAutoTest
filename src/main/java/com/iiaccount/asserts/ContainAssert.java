package com.iiaccount.asserts;

import org.testng.Assert;

/*
 *判断预期结果是否包含在响应报文中
 */
public class ContainAssert {

    public static boolean contains(String source, String search) {
        return source.contains(search);
        /*
        Assert.assertTrue(source.contains(search),
                String.format("期待'%s'包含'%s'，实际为不包含.", source, search));
        */
    }

    public static void notContains(String source, String search) {
        Assert.assertFalse(source.contains(search),
                String.format("期待'%s'不包含'%s'，实际为包含.", source, search));
    }
}
