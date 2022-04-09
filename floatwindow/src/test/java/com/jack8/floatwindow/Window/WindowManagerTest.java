package com.jack8.floatwindow.Window;

import junit.framework.TestCase;

public class WindowManagerTest extends TestCase {

    public void testGetMiniFreeNumber() {
        int id = WindowManager.getMiniFreeNumber(new Integer[]{1, 4, 3, 6, 5, 0});
        assert id == 2;

        id = WindowManager.getMiniFreeNumber(new Integer[]{1, 4, 3, 6, 2, 5});
        assert id == 0;

        id = WindowManager.getMiniFreeNumber(new Integer[]{1, 4, 3, 6, 2, 5, 0});
        assert id == 7;
    }
}