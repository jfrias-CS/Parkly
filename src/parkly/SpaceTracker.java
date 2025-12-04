package parkly;

/**
 * Manages the current occupancy and capacity of the parking facility.
 * Implemented as a thread-safe Singleton to ensure only one authoritative
 * instance exists on the server to prevent race conditions in counting spaces.
 */
public class SpaceTracker {
    
    // Static volatile instance variable
    // 'volatile' ensures visibility of changes across threads.
    private static volatile SpaceTracker instance;

    private final int capacity;
    private int currentCount;

    // Private Constructor: Prevents external instantiation.
    private SpaceTracker(int capacity) {
        this.capacity = capacity;
        this.currentCount = 0;
    }
    public static SpaceTracker getInstance(int capacity) {
        if (instance == null) {
            // Synchronize only the first time when the instance might be null
            synchronized (SpaceTracker.class) {
                if (instance == null) {
                    instance = new SpaceTracker(capacity);
                }
            }
        }
        // For subsequent calls, return the existing instance, ignoring the capacity parameter
        return instance;
    }

    // Synchronized to make sure calls are all in sequence

    public synchronized void increment() {
        if (currentCount < capacity) {
            currentCount++;
        }
    }

    public synchronized void decrement() {
        if (currentCount > 0) {
            currentCount--;
        }
    }

    // --- Getters (Thread-Safe using synchronized) ---

    public synchronized int getCurrentCount() {
        return currentCount;
    }

    public synchronized int getCapacity() {
        // Since capacity is final, synchronizing here is mostly for consistency.
        return capacity;
    }

    public synchronized int getAvailable() {
        return capacity - currentCount;
    }

    public synchronized boolean isFull() {
        return currentCount >= capacity;
    }
}