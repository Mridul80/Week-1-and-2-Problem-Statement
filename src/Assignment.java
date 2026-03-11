import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    LocalTime time;

    public Transaction(int id, int amount, String merchant, String account, String time) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.time = LocalTime.parse(time);
    }

    public String toString() {
        return "ID:" + id + " Amount:" + amount + " Merchant:" + merchant + " Account:" + account + " Time:" + time;
    }
}

public class Assignment {

    List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public List<List<Integer>> findTwoSum(int target) {

        Map<Integer, Transaction> map = new HashMap<>();
        List<List<Integer>> result = new ArrayList<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                result.add(Arrays.asList(map.get(complement).id, t.id));
            }

            map.put(t.amount, t);
        }

        return result;
    }

    public List<List<Integer>> findTwoSumWithinHour(int target) {

        Map<Integer, Transaction> map = new HashMap<>();
        List<List<Integer>> result = new ArrayList<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {

                Transaction prev = map.get(complement);

                long diff = Duration.between(prev.time, t.time).toMinutes();

                if (Math.abs(diff) <= 60) {
                    result.add(Arrays.asList(prev.id, t.id));
                }
            }

            map.put(t.amount, t);
        }

        return result;
    }

    public List<List<Integer>> findKSum(int k, int target) {

        List<List<Integer>> result = new ArrayList<>();
        backtrack(k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int k, int target, int start,
                           List<Transaction> current,
                           List<List<Integer>> result) {

        if (k == 0 && target == 0) {

            List<Integer> ids = new ArrayList<>();

            for (Transaction t : current)
                ids.add(t.id);

            result.add(ids);
            return;
        }

        if (k == 0 || start >= transactions.size())
            return;

        for (int i = start; i < transactions.size(); i++) {

            Transaction t = transactions.get(i);

            current.add(t);

            backtrack(k - 1, target - t.amount, i + 1, current, result);

            current.remove(current.size() - 1);
        }
    }

    public void detectDuplicates() {

        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {

            String key = t.amount + "-" + t.merchant;

            map.putIfAbsent(key, new ArrayList<>());
            map.get(key).add(t);
        }

        for (String key : map.keySet()) {

            List<Transaction> list = map.get(key);

            Set<String> accounts = new HashSet<>();

            for (Transaction t : list)
                accounts.add(t.account);

            if (accounts.size() > 1) {

                System.out.println("Duplicate Suspicious Transaction:");
                System.out.println("Amount-Merchant: " + key);
                System.out.println("Accounts: " + accounts);
            }
        }
    }

    public static void main(String[] args) {

        Assignment system = new Assignment();

        system.addTransaction(new Transaction(1, 500, "StoreA", "acc1", "10:00"));
        system.addTransaction(new Transaction(2, 300, "StoreB", "acc2", "10:15"));
        system.addTransaction(new Transaction(3, 200, "StoreC", "acc3", "10:30"));
        system.addTransaction(new Transaction(4, 500, "StoreA", "acc4", "10:40"));

        System.out.println("Two Sum Target 500:");
        System.out.println(system.findTwoSum(500));

        System.out.println("\nTwo Sum within 1 hour Target 500:");
        System.out.println(system.findTwoSumWithinHour(500));

        System.out.println("\nK Sum (k=3 target=1000):");
        System.out.println(system.findKSum(3, 1000));

        System.out.println("\nDuplicate Detection:");
        system.detectDuplicates();
    }
}