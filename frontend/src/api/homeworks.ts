import request from '@/utils/request';
import type {
  AddHomeworkQuestionFromBankRequest,
  AddInlineHomeworkQuestionRequest,
  ChangeHomeworkStatusRequest,
  CreateHomeworkRequest,
  GradeHomeworkAnswerRequest,
  GradeHomeworkRequest,
  GradeHomeworkResult,
  HomeworkListItem,
  HomeworkSubmissionItem,
  StudentHomeworkDetail,
  SubmitHomeworkRequest,
} from '@/types/homework';

export const listTeacherHomeworks = () => request.get<HomeworkListItem[]>('/teacher/homeworks');

export const createHomework = (data: CreateHomeworkRequest) => request.post<HomeworkListItem>('/teacher/homeworks', data);

export const updateHomework = (id: number | string, data: CreateHomeworkRequest) =>
  request.put<HomeworkListItem>(`/teacher/homeworks/${id}`, data);

export const changeHomeworkStatus = (id: number | string, data: ChangeHomeworkStatusRequest) =>
  request.post<HomeworkListItem>(`/teacher/homeworks/${id}/status`, data);

export const addHomeworkQuestionFromBank = (id: number | string, data: AddHomeworkQuestionFromBankRequest) =>
  request.post(`/teacher/homeworks/${id}/questions/bank`, data);

export const addInlineHomeworkQuestion = (id: number | string, data: AddInlineHomeworkQuestionRequest) =>
  request.post(`/teacher/homeworks/${id}/questions/inline`, data);

export const listHomeworkSubmissions = (homeworkId: number | string) =>
  request.get<HomeworkSubmissionItem[]>(`/teacher/homeworks/${homeworkId}/submissions`);

export const getHomeworkSubmissionDetail = (submissionId: number | string) =>
  request.get<HomeworkSubmissionItem>(`/teacher/homework-submissions/${submissionId}`);

export const gradeHomeworkSubmission = (submissionId: number | string, data: GradeHomeworkRequest) =>
  request.post<GradeHomeworkResult>(`/teacher/homework-submissions/${submissionId}/grade`, data);

export const releaseHomeworkScores = (id: number | string) =>
  request.post(`/teacher/homeworks/${id}/release-scores`);

export const gradeHomeworkAnswer = (answerId: number | string, data: GradeHomeworkAnswerRequest) =>
  request.post(`/teacher/homework-answers/${answerId}/grade`, data);

export const listStudentHomeworks = () => request.get<HomeworkListItem[]>('/student/homeworks');

export const getStudentHomeworkDetail = (id: number | string) =>
  request.get<StudentHomeworkDetail>(`/student/homeworks/${id}`);

export const submitHomework = (id: number | string, data: SubmitHomeworkRequest) =>
  request.post<StudentHomeworkDetail>(`/student/homeworks/${id}/submit`, data);
