package com.sty.xxt.jvmmemory;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    class TestGC {
        byte[] bytes = new byte[300 * 1024];
    }

    @Test
    public void testGc() {
        List<TestGC> list = new ArrayList<>();
        while (true) {
            list.add(new TestGC());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}