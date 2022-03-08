package hu.elte.sbzbxr.phoneconnect.model.connection.buffer;

import static org.junit.Assert.*;

import android.graphics.Bitmap;

import org.junit.Before;
import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot;

public class BufferReducerAlgorithmsTest {
    LinkedBlockingQueue<ScreenShot> queue;
    ScreenShot toInsert;

    @Before
    public void setUp() throws Exception {
        queue = new LinkedBlockingQueue<>(4);
        queue.offer(new ScreenShot("1",null));
        queue.offer(new ScreenShot("2",null));
        queue.offer(new ScreenShot("3",null));
        queue.offer(new ScreenShot("4",null));
        toInsert = new ScreenShot("5",null);
    }

    @Test
    public void removeEvenIndices() {
        BufferReducerAlgorithms.removeEvenIndices(queue,toInsert);
        assertEquals("1", Objects.requireNonNull(queue.poll()).getName());
        assertEquals("3", Objects.requireNonNull(queue.poll()).getName());
        assertEquals("5", Objects.requireNonNull(queue.poll()).getName());
        assertNull(queue.poll());
    }

    @Test
    public void clearQueue() {
        BufferReducerAlgorithms.clearQueue(queue,toInsert);
        assertEquals("5", Objects.requireNonNull(queue.poll()).getName());
        assertNull(queue.poll());
    }
}