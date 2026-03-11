import java.util.*;

public class Assignment {

    private Map<String, Integer> userMap = new HashMap<>();

    private Map<String, Integer> attemptMap = new HashMap<>();

    public boolean checkAvailability(String username) {
        attemptMap.put(username,
                attemptMap.getOrDefault(username, 0) + 1);

        return !userMap.containsKey(username);
    }

    public void registerUser(String username, int userId) {
        userMap.put(username, userId);
    }

    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;

            if (!userMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        String dotVersion = username.replace("_", ".");
        if (!userMap.containsKey(dotVersion)) {
            suggestions.add(dotVersion);
        }

        return suggestions;
    }

    public String getMostAttempted() {

        String maxUser = "";
        int max = 0;

        for (Map.Entry<String, Integer> entry : attemptMap.entrySet()) {

            if (entry.getValue() > max) {
                max = entry.getValue();
                maxUser = entry.getKey();
            }
        }

        return maxUser + " (" + max + " attempts)";
    }
    public static void main(String[] args) {

        Assignment system = new Assignment();

        system.registerUser("john_doe", 101);
        system.registerUser("admin", 102);

        System.out.println(system.checkAvailability("john_doe"));
        System.out.println(system.checkAvailability("jane_smith"));

        System.out.println(system.suggestAlternatives("john_doe"));

        System.out.println(system.getMostAttempted());
    }
}