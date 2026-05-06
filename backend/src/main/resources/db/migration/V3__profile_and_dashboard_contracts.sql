ALTER TABLE sys_user ADD COLUMN email VARCHAR(128);
ALTER TABLE sys_user ADD COLUMN phone VARCHAR(32);
ALTER TABLE sys_user ADD COLUMN office_hours VARCHAR(255);
ALTER TABLE sys_user ADD COLUMN bio VARCHAR(1000);

UPDATE sys_user
SET email = 'arthur.smith@university.edu',
    phone = '+86 13800000001',
    office_hours = '周一、周三 14:00-16:00',
    bio = '负责教学一体化平台课程示范教学与实验指导。'
WHERE id = 1;

UPDATE sys_user
SET email = '20260001@university.edu',
    phone = '+86 13900000001',
    office_hours = '辅导员咨询请前往课程群预约',
    bio = '软件工程 2026-1 班学生，当前聚焦实验、作业与考试任务闭环。'
WHERE id = 2;
