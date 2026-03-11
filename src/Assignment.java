import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    PriorityQueue<String> topQueries =
            new PriorityQueue<>((a, b) -> AutocompleteSystem.freq.get(a) - AutocompleteSystem.freq.get(b));
}

class AutocompleteSystem {

    static Map<String, Integer> freq = new HashMap<>();
    TrieNode root = new TrieNode();
    int TOP_K = 10;

    public void addQuery(String query) {
        freq.put(query, freq.getOrDefault(query, 0) + 1);

        TrieNode node = root;

        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            node.topQueries.remove(query);
            node.topQueries.offer(query);

            if (node.topQueries.size() > TOP_K)
                node.topQueries.poll();
        }
    }

    public List<String> search(String prefix) {

        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c))
                return new ArrayList<>();

            node = node.children.get(c);
        }

        List<String> result = new ArrayList<>(node.topQueries);

        result.sort((a, b) -> freq.get(b) - freq.get(a));

        return result;
    }

    public void updateFrequency(String query) {
        addQuery(query);
    }

    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        system.addQuery("java tutorial");
        system.addQuery("javascript");
        system.addQuery("java download");
        system.addQuery("java 21 features");
        system.addQuery("java interview questions");

        System.out.println(system.search("jav"));

        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println(system.search("java"));
    }
}