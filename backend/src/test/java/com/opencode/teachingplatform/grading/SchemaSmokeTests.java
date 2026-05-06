package com.opencode.teachingplatform.grading;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SchemaSmokeTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void gradingRedesignSchemaShouldExist() {
        assertThat(tableExists("HOMEWORK_QUESTION")).isTrue();
        assertThat(tableExists("HOMEWORK_ANSWER")).isTrue();
        assertThat(tableExists("QUESTION_ATTACHMENT")).isTrue();

        assertThat(columnsOf("HOMEWORK_QUESTION"))
                .contains("QUESTION_ID", "QUESTION_SNAPSHOT_JSON");
        assertThat(columnsOf("HOMEWORK_ANSWER"))
                .contains(
                        "HOMEWORK_QUESTION_ID",
                        "ANSWER_TEXT",
                        "AUTO_SCORE",
                        "SUGGESTED_SCORE",
                        "SCORE_SOURCE",
                        "JUDGE_DETAIL",
                        "ACCEPTED_AUTO_SCORE"
                );
        assertThat(columnsOf("QUESTION_ATTACHMENT"))
                .contains("OWNER_TYPE", "OWNER_ID", "FILE_NAME", "FILE_PATH", "SORT_ORDER");

        assertThat(columnsOf("QUESTION_BANK")).contains("SCORING_CONFIG_JSON");
        assertThat(columnsOf("HOMEWORK")).contains("SCORE_VISIBILITY_MODE");
        assertThat(columnsOf("HOMEWORK_SUBMISSION")).contains("GRADED_AT");
        assertThat(columnsOf("EXAM_QUESTION")).contains("QUESTION_SNAPSHOT_JSON");
        assertThat(columnsOf("EXAM_ANSWER"))
                .contains("AUTO_SCORE", "SUGGESTED_SCORE", "JUDGE_DETAIL", "ACCEPTED_AUTO_SCORE");
        assertThat(columnsOf("LAB_STEP")).contains("SCORING_CONFIG_JSON");
        assertThat(columnsOf("LAB_STEP_ANSWER"))
                .contains("SCORE", "AUTO_SCORE", "SUGGESTED_SCORE", "SCORE_SOURCE");
    }

    private boolean tableExists(String tableName) {
        try {
            jdbcTemplate.queryForList("SELECT 1 FROM " + tableName + " WHERE 1 = 0", Integer.class);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Set<String> columnsOf(String tableName) {
        Set<String> columns = new TreeSet<>();
        for (String columnName : jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_SCHEMA) = 'PUBLIC' AND UPPER(TABLE_NAME) = ?",
                String.class,
                tableName
        )) {
            columns.add(columnName.toUpperCase());
        }
        return columns;
    }
}
