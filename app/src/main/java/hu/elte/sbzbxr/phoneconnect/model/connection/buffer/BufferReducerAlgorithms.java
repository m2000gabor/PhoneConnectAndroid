package hu.elte.sbzbxr.phoneconnect.model.connection.buffer;

import java.util.concurrent.BlockingQueue;

public class BufferReducerAlgorithms {
    public static <T> void removeEvenIndices(BlockingQueue<T> queue, T toInsert) {
        T tmp = queue.poll();
        int index = 0;
        int maxSize = queue.size();
        while (index < maxSize) {
            if (tmp == null) {
                index++;
                continue;
            }
            if (index % 2 == 0) queue.offer(tmp);
            index++;
            tmp = queue.poll();
        }
        queue.offer(toInsert);
        System.err.println("Bitmap queue overflown -> even indices removed");
    }

    public static <T> void clearQueue(BlockingQueue<T> queue, T toInsert){
        queue.clear();
        System.err.println("Bitmap queue overflown -> cleared");
        queue.offer(toInsert);
    }
}
