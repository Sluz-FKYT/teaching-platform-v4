export type StudentStatus = 'ACTIVE' | 'DISABLED';

export interface Student {
  id: number;
  username: string;
  displayName: string;
  classId: number | null;
  status: StudentStatus;
}

export interface CreateStudentPayload {
  username: string;
  displayName: string;
  password: string;
  classId: number;
}

export interface UpdateStudentPayload {
  displayName: string;
  classId: number;
}

export interface ImportStudentRow {
  username: string;
  displayName: string;
  password: string;
}

export interface ImportStudentsPayload {
  classId: number;
  rows: ImportStudentRow[];
}

export interface ImportStudentsResult {
  successCount: number;
  failureCount: number;
  failures: Array<{
    row: number;
    reason: string;
  }>;
}
