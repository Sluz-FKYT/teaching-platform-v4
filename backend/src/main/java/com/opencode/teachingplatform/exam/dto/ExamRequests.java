package com.opencode.teachingplatform.exam.dto;

import com.opencode.teachingplatform.common.enums.ScoreVisibilityMode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public final class ExamRequests {

    private ExamRequests() {}

    public record ExamQuestionItem(
            Long questionId,
            String sourceType,
            String questionType,
            String stem,
            @NotNull Integer sortOrder,
            @NotNull Double questionScore,
            String optionsJson,
            String answerJson,
            String scoringConfigJson
    ) {
        public ExamQuestionItem(@NotNull Long questionId,
                                @NotNull Integer sortOrder,
                                @NotNull Double questionScore) {
            this(questionId, "BANK", null, null, sortOrder, questionScore, null, null, null);
        }

        public String resolvedSourceType() {
            return sourceType == null || sourceType.isBlank()
                    ? (questionId != null ? "BANK" : "INLINE")
                    : sourceType;
        }

        @AssertTrue(message = "BANK 题目必须提供 questionId，INLINE 题目必须提供 questionType 和 stem")
        public boolean isValidConfig() {
            String resolved = resolvedSourceType();
            if ("INLINE".equalsIgnoreCase(resolved)) {
                return questionType != null && !questionType.isBlank()
                        && stem != null && !stem.isBlank();
            }
            return questionId != null;
        }
    }

    public record CreateExamRequest(
            @NotBlank String title,
            String description,
            @NotNull Long classId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            @NotNull Integer durationMinutes,
            @NotBlank String status,
            ScoreVisibilityMode scoreVisibilityMode,
            List<ExamQuestionItem> questions
    ) {}

    public record UpdateExamRequest(
            @NotBlank String title,
            String description,
            @NotNull Long classId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            @NotNull Integer durationMinutes,
            ScoreVisibilityMode scoreVisibilityMode,
            List<ExamQuestionItem> questions
    ) {}

    public record ChangeExamStatusRequest(
            @NotBlank String status
    ) {}

    public record SubmitAnswerItem(
            @NotNull Long questionId,
            List<String> selectedOptions,
            String answerText,
            String answerJson,
            String attachmentPath
    ) {}

    public record SubmitExamRequest(
            List<SubmitAnswerItem> answers
    ) {}

    public record GradeExamAnswerItem(
            @NotNull Long questionId,
            @NotNull Double score,
            String teacherComment
    ) {}

    public record GradeExamSubmissionRequest(
            @NotEmpty List<GradeExamAnswerItem> answers
    ) {}

    public record ConfirmExamAnswerRequest(
            @NotNull Long questionId,
            String teacherComment,
            Double score,
            boolean acceptSuggested
    ) {}
}
