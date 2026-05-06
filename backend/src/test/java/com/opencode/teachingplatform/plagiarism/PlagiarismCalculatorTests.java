package com.opencode.teachingplatform.plagiarism;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;

class PlagiarismCalculatorTests {

    @Test
    void shouldCalculateJaccardSimilarity() {
        double similarity = similarity("hello world", "hello java world");
        Assertions.assertTrue(similarity > 0);
        Assertions.assertTrue(similarity < 100);
    }

    private double similarity(String a, String b) {
        Set<String> left = grams(a);
        Set<String> right = grams(b);
        Set<String> union = new HashSet<>(left);
        union.addAll(right);
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return union.isEmpty() ? 0D : intersection.size() * 100D / union.size();
    }

    private Set<String> grams(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replaceAll("\\s+", "")
                .toLowerCase();
        Set<String> grams = new HashSet<>();
        if (normalized.length() < 3) {
            grams.add(normalized);
            return grams;
        }
        for (int i = 0; i <= normalized.length() - 3; i++) {
            grams.add(normalized.substring(i, i + 3));
        }
        return grams;
    }
}
