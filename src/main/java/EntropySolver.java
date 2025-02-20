import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.log;
import static java.lang.Math.pow;

// https://gregcameron.com/infinite-wordle/

@Slf4j
public class EntropySolver {
    private static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));
    private static final Set<String> ACCEPTED = newHashSet();

    @Getter
    public enum Outcome {
        GREEN('g'),
        YELLOW('y'),
        GREY('x');

        private final char symbol;

        Outcome(char symbol) {
            this.symbol = symbol;
        }
    }

    /**
     * Main solver loop that implements the entropy-based Wordle solver.
     * Uses information theory to select the most optimal guess at each step.
     */
    public static void main(String[] args) {
        Set<String> solutions = newHashSet();
        processInput(solutions);

        // precomputed best at first step
        log.info("Best Word: {}", "soare");
        filterRemainingWords(solutions, "soare", getOutcome());
        log.info("{} - {}", solutions.size(), solutions.size() < 50 ? solutions : "");

        while (solutions.size() > 1) {
            Map<String, Double> entropyMap = newHashMap();
            ACCEPTED.forEach(potentialInput -> {
                Map<Integer, Integer> patterns = newHashMap();
                solutions.forEach(solution -> patterns.merge(calculatePattern(potentialInput, solution), 1, Integer::sum));
                entropyMap.put(potentialInput, getEntropy(patterns));
            });

            String bestWord = getBestWord(entropyMap);
            log.info("Best Word: {}", bestWord);
            filterRemainingWords(solutions, bestWord, getOutcome());
            log.info("{} - {}", solutions.size(), solutions.size() < 100 ? solutions : "");
        }
        log.info("Solution: {}", solutions.iterator().next());
    }

    /**
     * Returns the word with the highest entropy score from the given entropy map.
     * Higher entropy indicates a word that will provide more information about the solution.
     *
     * @param entropyMap Map of words to their entropy scores
     * @return The word with the highest entropy score
     */
    private static String getBestWord(Map<String, Double> entropyMap) {
        Map<String, Double> sortedMap = newLinkedHashMap();
        entropyMap.entrySet().stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        return sortedMap.entrySet().iterator().next().getKey();
    }

    /**
     * Calculates the entropy score for a given pattern distribution.
     * Uses the formula: -sum(p * log(p)) where p is the probability of each pattern.
     *
     * @param patterns Map of pattern values to their frequency
     * @return The entropy score
     */
    private static Double getEntropy(Map<Integer, Integer> patterns) {
        return patterns.values().stream()
                .mapToDouble(pattern -> pattern / (ACCEPTED.size() * 1.0))
                .map(probability -> probability * log(1 / probability))
                .sum();
    }

    /**
     * Calculates a unique pattern value for two words using base-3 encoding.
     * Each position contributes:
     * - 2 * (3^pos) for exact match (green)
     * - 1 * (3^pos) for letter exists (yellow)
     * - 0 for no match (grey)
     *
     * @param guess  The guess word
     * @param target The target word
     * @return A unique integer representing the pattern
     */
    static int calculatePattern(String guess, String target) {
        int pattern = 0;
        for (int i = 0; i < guess.length(); i++) {
            int pow = (int) pow(3, (guess.length() - i - 1));
            // Green
            if (guess.charAt(i) == target.charAt(i)) {
                pattern += (2 * pow);
            } else if (target.contains(String.valueOf(guess.charAt(i)))) {
                pattern += pow;
            }
        }
        return pattern;
    }

    private static void filterRemainingWords(Set<String> solutions, String wordUsed, String outcome) {
        solutions.removeIf(word -> !isWordStillValid(word, wordUsed, outcome));
    }

    /**
     * Validates if a word matches the given outcome pattern based on the used word.
     */
    private static boolean isWordStillValid(String currentWord, String wordUsed, String outcome) {
        for (int i = 0; i < currentWord.length(); i++) {
            char used = wordUsed.charAt(i);
            char current = currentWord.charAt(i);
            boolean currentContainsUsed = currentWord.contains(String.valueOf(used));

            Outcome currentOutcome = Outcome.valueOf(outcome.charAt(i) + "");

            switch (currentOutcome) {
                case GREY -> {
                    if (currentContainsUsed && countOccurrences(wordUsed, used) == 1) {
                        return false;
                    }
                }
                case YELLOW -> {
                    if (!currentContainsUsed || current == used) {
                        return false;
                    }
                }
                case GREEN -> {
                    if (current != used) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Counts the number of occurrences of a character in a string.
     */
    private static int countOccurrences(String str, char ch) {
        return (int) str.chars().filter(c -> c == ch).count();
    }

    @SneakyThrows
    private static String getOutcome() {
        return READER.readLine();
    }

    private static void processInput(Set<String> solutions) {
        fileStream("wordle_solutions.txt").filter(word -> word.length() == 5)
                .forEach(solutions::add);
        fileStream("wordle.txt").filter(word -> word.length() == 5)
                .forEach(ACCEPTED::add);
    }

    public static Stream<String> fileStream(String fileName) {
        try {
            return Files.lines(Paths.get("src\\main\\resources", fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return newArrayList("").stream();
        }
    }
}
