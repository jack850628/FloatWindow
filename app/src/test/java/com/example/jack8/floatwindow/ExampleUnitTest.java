package com.example.jack8.floatwindow;

import org.junit.Test;

import static org.junit.Assert.*;


import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void pathReverseTest(){
        List<String> a = new LinkedList<String>(Arrays.asList("/poi".split("/")));
        a.remove(0);
        Collections.reverse(a);
        System.out.println("/" + String.join("/", a));
    }
}