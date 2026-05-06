package com.opencode.teachingplatform.plagiarism.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
/**
 * 本地查重算法服务。
 *
 * <p>当前实现采用轻量级的字符 3-gram + Jaccard 相似度方案，
 * 适合在课程作业场景下快速给出一个可解释的相似率与最高匹配来源。</p>
 */
public class LocalPlagiarismService {

    /**
     * 对源文本和候选文本集合做相似度比对，返回最高相似项。
     */
    public PlagiarismAnalysisResult analyze(String sourceText, List<CandidateText> candidates) {
        String normalizedSource = normalize(sourceText);
        if (normalizedSource.isBlank() || candidates.isEmpty()) {
            return new PlagiarismAnalysisResult(0D, "未找到可比对的历史文本", buildRawResultJson(0D, null, null));
        }

        Set<String> sourceGrams = toCharacterTrigrams(normalizedSource);
        CandidateMatch bestMatch = null;
        for (CandidateText candidate : candidates) {
            String normalizedCandidate = normalize(candidate.text());
            if (normalizedCandidate.isBlank()) {
                continue;
            }
            double similarityRate = calculateSimilarityRate(sourceGrams, toCharacterTrigrams(normalizedCandidate));
            if (bestMatch == null || similarityRate > bestMatch.similarityRate()) {
                bestMatch = new CandidateMatch(candidate.reference(), similarityRate);
            }
        }

        if (bestMatch == null) {
            return new PlagiarismAnalysisResult(0D, "未找到可比对的历史文本", buildRawResultJson(0D, null, null));
        }

        String summary = String.format(Locale.ROOT, "最高相似来源: %s (%.2f%%)", bestMatch.reference(), bestMatch.similarityRate());
        return new PlagiarismAnalysisResult(
                bestMatch.similarityRate(),
                summary,
                buildRawResultJson(bestMatch.similarityRate(), bestMatch.reference(), summary)
        );
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC);
        return normalized.toLowerCase(Locale.ROOT).replaceAll("\\s+", "").trim();
    }

    private Set<String> toCharacterTrigrams(String normalizedText) {
        Set<String> grams = new LinkedHashSet<>();
        if (normalizedText.isEmpty()) {
            return grams;
        }
        if (normalizedText.length() < 3) {
            grams.add(normalizedText);
            return grams;
        }
        for (int index = 0; index <= normalizedText.length() - 3; index++) {
            grams.add(normalizedText.substring(index, index + 3));
        }
        return grams;
    }

    private double calculateSimilarityRate(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0D;
        }
        int intersection = 0;
        for (String gram : left) {
            if (right.contains(gram)) {
                intersection++;
            }
        }
        int union = left.size() + right.size() - intersection;
        if (union == 0) {
            return 0D;
        }
        return roundToTwoDecimals(intersection * 100D / union);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String buildRawResultJson(Double similarityRate, String topMatchReference, String summary) {
        String escapedReference = topMatchReference == null ? "null" : '"' + escapeJson(topMatchReference) + '"';
        String escapedSummary = summary == null ? "null" : '"' + escapeJson(summary) + '"';
        return String.format(Locale.ROOT,
                "{\"algorithm\":\"jaccard\",\"algorithmVersion\":\"character-3gram\",\"similarityRate\":%.2f,\"jaccardSimilarityRate\":%.2f,\"topMatchReference\":%s,\"topMatchSummary\":%s}",
                similarityRate,
                similarityRate,
                escapedReference,
                escapedSummary
        );
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record CandidateText(String reference, String text) {
    }

    public record PlagiarismAnalysisResult(Double similarityRate, String topMatchSummary, String rawResultJson) {
    }

    private record CandidateMatch(String reference, Double similarityRate) {
    }
}
