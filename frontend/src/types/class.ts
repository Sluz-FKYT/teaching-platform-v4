export type ClassStatus = 'ACTIVE' | 'ARCHIVED';

export interface TeachingClass {
  id: number;
  name: string;
  code: string;
  teacherUserId: number;
  status: ClassStatus;
  studentCount: number;
}

export interface CreateClassPayload {
  name: string;
  code: string;
}

export interface UpdateClassPayload {
  name: string;
  status: ClassStatus;
}
