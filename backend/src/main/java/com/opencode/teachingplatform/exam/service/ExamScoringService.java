package com.opencode.teachingplatform.exam.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class ExamScoringService {

    private static final ObjectMapper mapper = new ObjectMapper();

    public double scoreObjectiveQuestion(String questionType, String expectedAnswerJson, String actualAnswerJson, double questionScore) {
        if (!isObjective(questionType)) {
            return 0.0;
        }
        if (actualAnswerJson == null || actualAnswerJson.isBlank()) {
            return 0.0;
        }

        try {
            List<String> expected = parseJsonArray(expectedAnswerJson);
            List<String> actual = parseJsonArray(actualAnswerJson);

            if (expected == null || expected.isEmpty() || actual == null || actual.isEmpty()) {
                return 0.0;
            }

            String type = questionType.toUpperCase();
            switch (type) {
                case "SINGLE":
                case "SINGLE_CHOICE":
                    return expected.get(0).equals(actual.get(0)) ? questionScore : 0.0;

                case "MULTI":
                case "MULTIPLE":
                case "MULTIPLE_CHOICE":
                    Set<String> expectedSet = new TreeSet<>(expected);
                    Set<String> actualSet = new TreeSet<>(actual);
                    return expectedSet.equals(actualSet) ? questionScore : 0.0;

                case "JUDGE":
                case "TRUE_FALSE":
                    return expected.get(0).equalsIgnoreCase(actual.get(0)) ? questionScore : 0.0;

                default:
                    return 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    public boolean isObjective(String questionType) {
        if (questionType == null) return false;
        String type = questionType.toUpperCase();
        return type.equals("SINGLE") || type.equals("SINGLE_CHOICE")
                || type.equals("MULTI") || type.equals("MULTIPLE") || type.equals("MULTIPLE_CHOICE")
                || type.equals("JUDGE") || type.equals("TRUE_FALSE")
                || type.equals("FILL") || type.equals("FILL_BLANK");
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
