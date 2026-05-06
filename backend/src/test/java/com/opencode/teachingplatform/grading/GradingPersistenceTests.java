package com.opencode.teachingplatform.grading;

import com.opencode.teachingplatform.common.enums.ActivityStatus;
import com.opencode.teachingplatform.common.enums.ScoreVisibilityMode;
import com.opencode.teachingplatform.homework.entity.Homework;
import com.opencode.teachingplatform.homework.entity.HomeworkQuestion;
import com.opencode.teachingplatform.homework.repository.HomeworkQuestionRepository;
import com.opencode.teachingplatform.homework.repository.HomeworkRepository;
import com.opencode.teachingplatform.question.entity.QuestionAttachment;
import com.opencode.teachingplatform.question.entity.QuestionBank;
import com.opencode.teachingplatform.question.repository.QuestionBankRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class GradingPersistenceTests {

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private HomeworkQuestionRepository homeworkQuestionRepository;

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void homeworkQuestionSupportsInlineSnapshotWithoutQuestionBankId() {
        Homework homework = new Homework();
        homework.setTitle("内联题作业");
        homework.setDescription("用于验证快照题保存");
        homework.setClassId(1L);
        homework.setCreatedBy(1L);
        homework.setStatus(ActivityStatus.DRAFT);
        homework.setScoreVisibilityMode(ScoreVisibilityMode.MANUAL_RELEASE);
        Homework savedHomework = homeworkRepository.saveAndFlush(homework);

        HomeworkQuestion question = new HomeworkQuestion();
        question.setHomeworkId(savedHomework.getId());
        question.setQuestionId(null);
        question.setSortOrder(1);
        question.setQuestionScore(15D);
        question.setQuestionSnapshotJson("{\"stem\":\"请说明架构分层边界\"}");

        HomeworkQuestion savedQuestion = homeworkQuestionRepository.saveAndFlush(question);
        entityManager.clear();

        Homework reloadedHomework = homeworkRepository.findById(savedHomework.getId()).orElseThrow();
        HomeworkQuestion reloadedQuestion = homeworkQuestionRepository.findById(savedQuestion.getId()).orElseThrow();

        assertThat(reloadedHomework.getScoreVisibilityMode()).isEqualTo(ScoreVisibilityMode.MANUAL_RELEASE);
        assertThat(reloadedQuestion.getQuestionId()).isNull();
        assertThat(reloadedQuestion.getQuestionSnapshotJson()).isEqualTo("{\"stem\":\"请说明架构分层边界\"}");
    }

    @Test
    void questionBankAndAttachmentPersistNewGradingFields() {
        jdbcTemplate.execute("ALTER TABLE question_bank ALTER COLUMN id RESTART WITH 100");

        QuestionBank questionBank = new QuestionBank();
        questionBank.setCode("QB-GRADING-001");
        questionBank.setType("SUBJECTIVE");
        questionBank.setStem("解释统一评分模型的价值");
        questionBank.setDifficulty("MEDIUM");
        questionBank.setDefaultScore(10);
        questionBank.setAnswerJson("{\"keywords\":[\"一致性\"]}");
        questionBank.setScoringConfigJson("{\"mode\":\"RECOMMEND_ONLY\"}");
        questionBank.setCreatedBy(1L);
        QuestionBank savedQuestionBank = questionBankRepository.saveAndFlush(questionBank);

        QuestionAttachment attachment = new QuestionAttachment();
        attachment.setOwnerType("QUESTION_BANK");
        attachment.setOwnerId(savedQuestionBank.getId());
        attachment.setFileName("rubric.pdf");
        attachment.setFilePath("/uploads/questions/rubric.pdf");
        attachment.setMediaType("application/pdf");
        attachment.setSortOrder(1);
        entityManager.persist(attachment);
        entityManager.flush();
        Long attachmentId = attachment.getId();

        entityManager.clear();

        QuestionBank reloadedQuestionBank = questionBankRepository.findById(savedQuestionBank.getId()).orElseThrow();
        QuestionAttachment reloadedAttachment = entityManager.find(QuestionAttachment.class, attachmentId);

        assertThat(reloadedQuestionBank.getScoringConfigJson()).isEqualTo("{\"mode\":\"RECOMMEND_ONLY\"}");
        assertThat(reloadedAttachment).isNotNull();
        assertThat(reloadedAttachment.getOwnerType()).isEqualTo("QUESTION_BANK");
        assertThat(reloadedAttachment.getOwnerId()).isEqualTo(savedQuestionBank.getId());
        assertThat(reloadedAttachment.getFileName()).isEqualTo("rubric.pdf");
        assertThat(reloadedAttachment.getFilePath()).isEqualTo("/uploads/questions/rubric.pdf");
    }

}
