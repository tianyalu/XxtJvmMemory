package com.sty.xxt.jvmmemory;

import org.junit.Test;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import static org.junit.Assert.assertEquals;

public class ReferenceTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        /**
         * 软引用
         */
        Object softObject =new Object();
        SoftReference<Object> objectSoftReference = new SoftReference<>(softObject);
        softObject = null;


        System.gc();
        System.out.println("soft:"+ objectSoftReference.get()); //soft:java.lang.Object@32a1bec0
        System.out.println("======================================");

        /**
         * 弱引用
         */
        Object weakObject =new Object();
        WeakReference<Object> objectWeakReference = new WeakReference<>(weakObject);
        weakObject = null;


        System.gc();
        System.out.println("weak:"+objectWeakReference.get()); //weak:null
        System.out.println("======================================");

        /**
         * 虚引用
         */
        Object phantomObject = new Object();
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        PhantomReference<Object> objectPhantomReference = new PhantomReference<Object>(phantomObject, queue);

        System.out.println(objectPhantomReference.get()); //null
        System.out.println(queue.poll()); //null
        phantomObject = null;
        System.gc();

        System.out.println(objectPhantomReference.get()); //null
        System.out.println(queue.poll()); //java.lang.ref.PhantomReference@22927a81
    }
}
