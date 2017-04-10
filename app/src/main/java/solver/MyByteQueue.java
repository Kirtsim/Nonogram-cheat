package solver;

/**
 *
 * Created by kirtsim on 14/02/2017.
 */

public class MyByteQueue {
    private int indexOfLast;
    private int indexOfFirst;
    private int size;
    private final byte[] bytes;

    public MyByteQueue(int capacity) {
        this.bytes = new byte[capacity];
        indexOfLast = indexOfFirst = size = 0;
    }

    public void add(byte number) {
        if (size == 0) {
            bytes[indexOfFirst] = number;
            size++;
        } else {
            final int nextIndex = (indexOfLast + 1) % bytes.length;
            if (nextIndex >= bytes.length)
                System.out.println();
            if (nextIndex != indexOfFirst) {
                bytes[nextIndex] = number;
                indexOfLast = nextIndex;
                size++;
            }
        }
    }

    byte pop() {
        if (size > 0) {
            size--;
            final int index = indexOfFirst;
            if (++indexOfFirst  == bytes.length)
                indexOfFirst = 0;
            return bytes[index];
        }
        return -1;
    }

    boolean isEmpty() {
        return size == 0;
    }

    public int getSize() {
        return this.size;
    }

    int capacity() {
        return bytes.length;
    }

}
