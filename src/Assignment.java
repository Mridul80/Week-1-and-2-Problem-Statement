import java.util.*;
import java.io.*;

class PlagiarismDetector {

    private Map<String, Set<String>> ngramIndex = new HashMap<>();

    private Map<String, List<String>> documentNgrams = new HashMap<>();

    private int N = 5;

    private List<String> generateNgrams(String text) {

        List<String> ngrams = new ArrayList<>();

        String[] words = text.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {

            StringBuilder gram = new StringBuilder();

            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }

            ngrams.add(gram.toString().trim());
        }

        return ngrams;
    }

    public void addDocument(String docId, String text) {

        List<String> ngrams = generateNgrams(text);

        documentNgrams.put(docId, ngrams);

        for (String gram : ngrams) {

            ngramIndex.putIfAbsent(gram, new HashSet<>());
            ngramIndex.get(gram).add(docId);
        }

        System.out.println(docId + " indexed with " + ngrams.size() + " n-grams.");
    }

    public void analyzeDocument(String docId) {

        List<String> ngrams = documentNgrams.get(docId);

        if (ngrams == null) {
            System.out.println("Document not found.");
            return;
        }

        Map<String, Integer> matchCount = new HashMap<>();

        for (String gram : ngrams) {

            Set<String> docs = ngramIndex.get(gram);

            if (docs != null) {

                for (String otherDoc : docs) {

                    if (!otherDoc.equals(docId)) {
                        matchCount.put(otherDoc,
                                matchCount.getOrDefault(otherDoc, 0) + 1);
                    }
                }
            }
        }

        System.out.println("\nAnalyzing: " + docId);
        System.out.println("Extracted " + ngrams.size() + " n-grams\n");

        for (String otherDoc : matchCount.keySet()) {

            int matches = matchCount.get(otherDoc);

            double similarity = (matches * 100.0) / ngrams.size();

            System.out.println("Found " + matches +
                    " matching n-grams with \"" + otherDoc + "\"");

            System.out.printf("Similarity: %.2f%% ", similarity);

            if (similarity > 60)
                System.out.println("(PLAGIARISM DETECTED)");
            else if (similarity > 10)
                System.out.println("(Suspicious)");
            else
                System.out.println("(Low similarity)");

            System.out.println();
        }
    }

    public void benchmarkSearch(String targetGram) {

        long start = System.nanoTime();

        boolean found = ngramIndex.containsKey(targetGram);

        long end = System.nanoTime();

        System.out.println("\nHash Search Time: " + (end - start) + " ns");

        start = System.nanoTime();

        boolean linearFound = false;

        for (String gram : ngramIndex.keySet()) {
            if (gram.equals(targetGram)) {
                linearFound = true;
                break;
            }
        }

        end = System.nanoTime();

        System.out.println("Linear Search Time: " + (end - start) + " ns");
    }
}

public class Assignment {

    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        String essay1 =
                "Machine learning is a field of computer science that focuses on building systems that learn from data.";

        String essay2 =
                "Machine learning is a field of computer science that focuses on building intelligent systems.";

        String essay3 =
                "Artificial intelligence and machine learning are transforming modern technology.";

        detector.addDocument("essay_089.txt", essay1);
        detector.addDocument("essay_092.txt", essay2);
        detector.addDocument("essay_123.txt", essay3);

        detector.analyzeDocument("essay_123.txt");

        detector.benchmarkSearch("machine learning is a field");
    }
}