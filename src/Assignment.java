import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Assignment {

    private Map<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();

    private Map<String, Queue<Integer>> waitingList = new ConcurrentHashMap<>();

    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new LinkedList<>());
    }

    public int checkStock(String productId) {
        return stockMap.get(productId).get();
    }

    public synchronized String purchaseItem(String productId, int userId) {

        AtomicInteger stock = stockMap.get(productId);

        if (stock.get() > 0) {
            int remaining = stock.decrementAndGet();
            return "Success! User " + userId +
                    " purchased item. Remaining stock: " + remaining;
        } else {
            Queue<Integer> queue = waitingList.get(productId);
            queue.add(userId);
            return "Stock unavailable. User " + userId +
                    " added to waiting list. Position: " + queue.size();
        }
    }

    public void showWaitingList(String productId) {
        System.out.println("Waiting List: " + waitingList.get(productId));
    }

    public static void main(String[] args) {

        Assignment system = new Assignment();

        system.addProduct("P101", 3);

        System.out.println(system.purchaseItem("P101", 1));
        System.out.println(system.purchaseItem("P101", 2));
        System.out.println(system.purchaseItem("P101", 3));
        System.out.println(system.purchaseItem("P101", 4));
        System.out.println(system.purchaseItem("P101", 5));

        system.showWaitingList("P101");

        System.out.println("Current Stock: " + system.checkStock("P101"));
    }
}