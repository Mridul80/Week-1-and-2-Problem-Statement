import java.util.*;
import java.util.concurrent.*;

class PageEvent {
    String url;
    String userId;
    String source;

    public PageEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class RealTimeAnalytics {

    private ConcurrentHashMap<String, Integer> pageViews = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Integer> trafficSources = new ConcurrentHashMap<>();

    public void processEvent(PageEvent event) {

        pageViews.merge(event.url, 1, Integer::sum);

        uniqueVisitors.putIfAbsent(event.url, ConcurrentHashMap.newKeySet());
        uniqueVisitors.get(event.url).add(event.userId);

        trafficSources.merge(event.source, 1, Integer::sum);
    }

    public List<Map.Entry<String, Integer>> getTopPages() {

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {

            pq.offer(entry);

            if (pq.size() > 10) {
                pq.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>();

        while (!pq.isEmpty()) {
            result.add(pq.poll());
        }

        Collections.reverse(result);
        return result;
    }

    public void getDashboard() {

        System.out.println("\n===== DASHBOARD =====");

        System.out.println("\nTop Pages:");

        int rank = 1;
        for (Map.Entry<String, Integer> entry : getTopPages()) {

            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.get(url).size();

            System.out.println(rank + ". " + url + " - " + views +
                    " views (" + unique + " unique)");
            rank++;
        }

        System.out.println("\nTraffic Sources:");

        int total = trafficSources.values().stream().mapToInt(i -> i).sum();

        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {

            double percent = (entry.getValue() * 100.0) / total;

            System.out.println(entry.getKey() + ": " +
                    String.format("%.2f", percent) + "%");
        }
    }
}


public class Assignment {

    public static void main(String[] args) throws Exception {

        RealTimeAnalytics analytics = new RealTimeAnalytics();

        analytics.processEvent(new PageEvent("/article/breaking-news", "user_123", "google"));
        analytics.processEvent(new PageEvent("/article/breaking-news", "user_456", "facebook"));
        analytics.processEvent(new PageEvent("/sports/championship", "user_111", "google"));
        analytics.processEvent(new PageEvent("/sports/championship", "user_222", "direct"));
        analytics.processEvent(new PageEvent("/sports/championship", "user_111", "google"));

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            analytics.getDashboard();
        }, 0, 5, TimeUnit.SECONDS);
    }
}