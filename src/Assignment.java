import java.util.concurrent.ConcurrentHashMap;

class TokenBucket {

    private double tokens;
    private final double maxTokens;
    private final double refillRate;
    private long lastRefillTime;

    public TokenBucket(double maxTokens, double refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    private void refill() {
        long currentTime = System.currentTimeMillis();
        double seconds = (currentTime - lastRefillTime) / 1000.0;

        double refillTokens = seconds * refillRate;
        tokens = Math.min(maxTokens, tokens + refillTokens);

        lastRefillTime = currentTime;
    }

    public synchronized boolean allowRequest() {
        refill();

        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }

        return false;
    }

    public synchronized double getRemainingTokens() {
        refill();
        return tokens;
    }

    public long getResetTimeSeconds() {
        return (long)((maxTokens - tokens) / refillRate);
    }
}

public class Assignment {

    private static final int MAX_REQUESTS = 1000;
    private static final int WINDOW_SECONDS = 3600;

    private static final double REFILL_RATE = (double) MAX_REQUESTS / WINDOW_SECONDS;

    private ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();


    public boolean checkRateLimit(String clientId) {

        TokenBucket bucket = clientBuckets.computeIfAbsent(
                clientId,
                id -> new TokenBucket(MAX_REQUESTS, REFILL_RATE)
        );

        boolean allowed = bucket.allowRequest();

        if (allowed) {
            System.out.println("Allowed (" + (int)bucket.getRemainingTokens() + " requests remaining)");
        } else {
            System.out.println("Denied (0 requests remaining, retry after "
                    + bucket.getResetTimeSeconds() + "s)");
        }

        return allowed;
    }


    public void getRateLimitStatus(String clientId) {

        TokenBucket bucket = clientBuckets.get(clientId);

        if (bucket == null) {
            System.out.println("{used: 0, limit: 1000, reset: 3600}");
            return;
        }

        double remaining = bucket.getRemainingTokens();
        int used = MAX_REQUESTS - (int)remaining;

        System.out.println("{used: " + used +
                ", limit: " + MAX_REQUESTS +
                ", reset: " + bucket.getResetTimeSeconds() + "}");
    }


    public static void main(String[] args) {

        Assignment limiter = new Assignment();

        String client = "abc123";

        for (int i = 0; i < 5; i++) {
            limiter.checkRateLimit(client);
        }

        limiter.getRateLimitStatus(client);
    }
}