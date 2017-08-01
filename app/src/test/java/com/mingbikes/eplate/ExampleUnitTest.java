package com.mingbikes.eplate;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        int freeParkSpace = 15 - 16;

        if (freeParkSpace > 0) {
            System.out.printf(freeParkSpace + "个空闲车位");
        } else {
            System.out.printf("超载" + Math.abs(freeParkSpace) + "辆");
        }
        assertEquals(4, 2 + 2);
    }
}