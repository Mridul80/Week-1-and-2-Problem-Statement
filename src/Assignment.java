import java.util.*;

class VideoData {
    String videoId;
    String content;

    public VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

/* LRU Cache using LinkedHashMap */
class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // access-order = true
        this.capacity = capacity;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

public class Assignment {

    /* Cache Sizes */
    private static final int L1_SIZE = 10000;
    private static final int L2_SIZE = 100000;

    /* Promotion Threshold */
    private static final int PROMOTION_THRESHOLD = 3;

    /* Caches */
    private LRUCache<String, VideoData> l1Cache = new LRUCache<>(L1_SIZE);
    private LRUCache<String, VideoData> l2Cache = new LRUCache<>(L2_SIZE);

    /* Database simulation */
    private Map<String, VideoData> database = new HashMap<>();

    /* Access counter */
    private Map<String, Integer> accessCount = new HashMap<>();

    /* Statistics */
    private int l1Hits = 0;
    private int l2Hits = 0;
    private int l3Hits = 0;

    /* Constructor */
    public Assignment() {
        // simulate database
        for (int i = 1; i <= 200000; i++) {
            String id = "video_" + i;
            database.put(id, new VideoData(id, "VideoContent_" + i));
        }
    }

    /* Get Video */
    public VideoData getVideo(String videoId) {

        long start = System.currentTimeMillis();

        /* L1 Cache */
        if (l1Cache.containsKey(videoId)) {
            l1Hits++;
            simulateLatency(1);
            System.out.println("L1 Cache HIT");
            return l1Cache.get(videoId);
        }

        System.out.println("L1 Cache MISS");

        /* L2 Cache */
        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            simulateLatency(5);
            System.out.println("L2 Cache HIT");

            VideoData video = l2Cache.get(videoId);
            increaseAccess(videoId);

            /* Promotion to L1 */
            if (accessCount.get(videoId) > PROMOTION_THRESHOLD) {
                promoteToL1(videoId, video);
            }

            return video;
        }

        System.out.println("L2 Cache MISS");

        /* L3 Database */
        if (database.containsKey(videoId)) {
            l3Hits++;
            simulateLatency(150);
            System.out.println("L3 Database HIT");

            VideoData video = database.get(videoId);

            l2Cache.put(videoId, video);
            increaseAccess(videoId);

            return video;
        }

        System.out.println("Video not found");
        return null;
    }

    /* Promote video from L2 to L1 */
    private void promoteToL1(String videoId, VideoData video) {
        l1Cache.put(videoId, video);
        System.out.println("Promoted " + videoId + " to L1 Cache");
    }

    /* Increase access count */
    private void increaseAccess(String videoId) {
        accessCount.put(videoId, accessCount.getOrDefault(videoId, 0) + 1);
    }

    /* Cache invalidation */
    public void updateVideo(String videoId, String newContent) {

        VideoData video = new VideoData(videoId, newContent);

        database.put(videoId, video);

        l1Cache.remove(videoId);
        l2Cache.remove(videoId);

        accessCount.remove(videoId);

        System.out.println("Cache invalidated for " + videoId);
    }

    /* Statistics */
    public void getStatistics() {

        int total = l1Hits + l2Hits + l3Hits;

        double l1Rate = (l1Hits * 100.0) / total;
        double l2Rate = (l2Hits * 100.0) / total;
        double l3Rate = (l3Hits * 100.0) / total;

        System.out.println("\nCache Statistics:");
        System.out.println("L1 Hits: " + l1Hits + " Hit Rate: " + l1Rate + "%");
        System.out.println("L2 Hits: " + l2Hits + " Hit Rate: " + l2Rate + "%");
        System.out.println("L3 Hits: " + l3Hits + " Hit Rate: " + l3Rate + "%");
    }

    /* Simulate latency */
    private void simulateLatency(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /* Main Test */
    public static void main(String[] args) {

        Assignment cache = new Assignment();

        cache.getVideo("video_123");
        cache.getVideo("video_123");
        cache.getVideo("video_999");
        cache.getVideo("video_123");
        cache.getVideo("video_123");

        cache.updateVideo("video_123", "UpdatedContent");

        cache.getVideo("video_123");

        cache.getStatistics();
    }
}