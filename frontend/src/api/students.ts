import request from '@/utils/request';
import type { PageResult } from '@/types/common';
import type {
  CreateStudentPayload,
  ImportStudentsPayload,
  ImportStudentsResult,
  Student,
  StudentStatus,
  UpdateStudentPayload,
} from '@/types/student';

export const listStudents = (classId?: number) =>
  request.get<PageResult<Student>>('/students', { params: classId ? { classId } : undefined });

export const createStudent = (data: CreateStudentPayload) => request.post<Student>('/students', data);

export const updateStudent = (id: number, data: UpdateStudentPayload) => request.put<Student>(`/students/${id}`, data);

export const importStudents = (data: ImportStudentsPayload) => request.post<ImportStudentsResult>('/students/import', data);

export const changeStudentStatus = (id: number, status: StudentStatus) =>
  request.post<Student>(`/students/${id}/status`, { status });
