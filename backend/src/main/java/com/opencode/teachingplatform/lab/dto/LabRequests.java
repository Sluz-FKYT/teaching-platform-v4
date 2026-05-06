package com.opencode.teachingplatform.lab.dto;

import com.opencode.teachingplatform.common.enums.ScoreVisibilityMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class LabRequests {

    private LabRequests() {
    }

    public record CreateLabRequest(
            @NotBlank String title,
            String description,
            String experimentContent,
            Integer experimentType,
            @NotNull Long classId,
            String status,
            Long materialId,
            ScoreVisibilityMode scoreVisibilityMode,
            Boolean summaryRequired
    ) {
    }

    public record UpdateLabRequest(
            @NotBlank String title,
            String description,
            String experimentContent,
            Integer experimentType,
            @NotNull Long classId,
            String status,
            Long materialId,
            ScoreVisibilityMode scoreVisibilityMode,
            Boolean summaryRequired
    ) {
    }

    public record ChangeLabStatusRequest(
            @NotBlank String status
    ) {
    }

    public record TeacherLabReportQuery(
            String keyword,
            String status
    ) {
    }

    public record CreateStepRequest(
            @NotNull Integer stepNo,
            @NotBlank String title,
            @NotBlank String questionType,
            @NotBlank String content,
            String answerConfigJson,
            @NotNull @Min(0) @Max(100) Integer stepScore,
            boolean allowPaste
    ) {
    }

    public record UpdateStepRequest(
            @NotNull Integer stepNo,
            @NotBlank String title,
            @NotBlank String questionType,
            @NotBlank String content,
            String answerConfigJson,
            @NotNull @Min(0) @Max(100) Integer stepScore,
            boolean allowPaste
    ) {
    }

    public record SaveStepAnswerRequest(
            @NotBlank String answerText,
            String answerPayloadJson
    ) {
    }

    public record SubmitLabRequest(
            String summaryText
    ) {
    }

    public record GradeLabReportRequest(
            String teacherComment,
            @NotEmpty List<@Valid GradeItem> items
    ) {
    }

    public record GradeItem(
            @NotNull Long answerId,
            @NotNull Double score,
            String teacherComment
    ) {
    }

    public record ConfirmStepScoreRequest(
            @NotNull Long answerId,
            Double score,
            String teacherComment,
            boolean acceptSuggested
    ) {
    }

    public record SaveBlankAcceptedAnswersRequest(
            @NotNull Long experimentItemId,
            @NotNull List<String> acceptedAnswers
    ) {
    }
}
