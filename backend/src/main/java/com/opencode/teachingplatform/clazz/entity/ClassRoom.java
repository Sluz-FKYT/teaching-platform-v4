package com.opencode.teachingplatform.clazz.entity;

import com.opencode.teachingplatform.common.enums.ClassStatus;
import com.opencode.teachingplatform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "class_room")
public class ClassRoom extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "teacher_user_id", nullable = false)
    private Long teacherUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ClassStatus status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getTeacherUserId() { return teacherUserId; }
    public void setTeacherUserId(Long teacherUserId) { this.teacherUserId = teacherUserId; }
    public ClassStatus getStatus() { return status; }
    public void setStatus(ClassStatus status) { this.status = status; }
}
