import java.util.*;
class DNSEntry {

    String domain;
    String ipAddress;
    long expiryTime;

    public DNSEntry(String domain, String ipAddress, long ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

class DNSCache {

    private final int capacity;

    private LinkedHashMap<String, DNSEntry> cache;

    private int hits = 0;
    private int misses = 0;

    public DNSCache(int capacity) {

        this.capacity = capacity;

        cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {

            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        startCleanupThread();
    }

    public synchronized String resolve(String domain) {

        long start = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null) {

            if (!entry.isExpired()) {
                hits++;

                long time = System.nanoTime() - start;

                System.out.println("Cache HIT → " + entry.ipAddress +
                        " (retrieved in " + time / 1_000_000.0 + " ms)");

                return entry.ipAddress;
            } else {
                cache.remove(domain);
                System.out.println("Cache EXPIRED → " + domain);
            }
        }

        misses++;

        String ip = queryUpstreamDNS(domain);

        cache.put(domain, new DNSEntry(domain, ip, 300));

        return ip;
    }

    private String queryUpstreamDNS(String domain) {

        System.out.println("Cache MISS → Querying upstream DNS for " + domain);

        try {
            Thread.sleep(100); // simulate 100ms delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String ip = "172.217.14." + new Random().nextInt(255);

        System.out.println(domain + " → " + ip + " (TTL: 300s)");

        return ip;
    }

    public void getCacheStats() {

        int total = hits + misses;

        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);

        System.out.println("\n--- Cache Statistics ---");
        System.out.println("Total Requests: " + total);
        System.out.println("Cache Hits: " + hits);
        System.out.println("Cache Misses: " + misses);
        System.out.println("Hit Rate: " + hitRate + "%");
    }

    private void startCleanupThread() {

        Thread cleaner = new Thread(() -> {

            while (true) {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (this) {

                    Iterator<Map.Entry<String, DNSEntry>> iterator = cache.entrySet().iterator();

                    while (iterator.hasNext()) {

                        Map.Entry<String, DNSEntry> entry = iterator.next();

                        if (entry.getValue().isExpired()) {
                            System.out.println("Removing expired entry → " + entry.getKey());
                            iterator.remove();
                        }
                    }
                }
            }
        });

        cleaner.setDaemon(true);
        cleaner.start();
    }
}

/* Main Class */
public class Assignment {

    public static void main(String[] args) throws Exception {

        DNSCache dnsCache = new DNSCache(5);

        dnsCache.resolve("google.com");
        dnsCache.resolve("google.com");
        dnsCache.resolve("facebook.com");
        dnsCache.resolve("google.com");
        dnsCache.resolve("youtube.com");

        Thread.sleep(2000);

        dnsCache.resolve("facebook.com");

        dnsCache.getCacheStats();
    }
}