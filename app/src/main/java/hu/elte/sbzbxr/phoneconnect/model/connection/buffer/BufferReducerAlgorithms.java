package hu.elte.sbzbxr.phoneconnect.model.connection.buffer;

import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BufferReducerAlgorithms {
    public static <T> void removeEvenIndices_old(BlockingQueue<T> queue, T toInsert) {
        int maxSize = queue.size();
        int index = 0;
        T tmp = queue.poll();
        while (tmp!=null && index < maxSize) {
            if (index % 2 == 0) queue.offer(tmp);
            index++;
            tmp = queue.poll();
        }
        queue.offer(toInsert);
        System.err.println("Bitmap queue overflown -> even indices removed");
    }

    public static <T> void removeEvenIndices(BlockingQueue<T> queue, T toInsert) {
        final int initialSize = queue.size();
        final AtomicInteger counter = new AtomicInteger(1);
        List<T> toDelete = queue.stream().map(e-> new AbstractMap.SimpleEntry<>(e, counter.getAndIncrement()))
                .filter(entry -> entry.getValue()%2==0)
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());

        queue.removeAll(toDelete);
        if(queue.size()+toDelete.size()==initialSize){
            queue.offer(toInsert);
        }

        System.err.println("Bitmap queue overflown -> even indices removed");
    }

    public static <T> void clearQueue(BlockingQueue<T> queue, T toInsert){
        queue.clear();
        System.err.println("Bitmap queue overflown -> cleared");
        queue.offer(toInsert);
    }
}
