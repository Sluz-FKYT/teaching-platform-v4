import request from '@/utils/request';
import type { StudentScoreOverview } from '@/types/analysis';

export const getMaterials = () => request.get<any[]>('/materials');
export const getLabs = () => request.get<any[]>('/labs');
export const getLabSteps = (id: string | number) => request.get<any[]>(`/labs/${id}/steps`);
export const saveLabStepAnswer = (stepId: string | number, data: any) => request.post(`/lab-step-answers/${stepId}`, data);
export const submitLab = (id: string | number) => request.post(`/labs/${id}/submit`);
export const getHomeworks = () => request.get<any[]>('/homeworks');
export const getHomeworkDetail = (id: string | number) => request.get<any>(`/homeworks/${id}`);
export const submitHomework = (id: string | number, data: any) => request.post(`/homeworks/${id}/submit`, data);
export { listExams as getExams, getExamDetail, startExam, submitExam } from './exams';
export const getScores = () => request.get<StudentScoreOverview>('/analysis/my-score-overview');
export const getStudentDashboard = () => request.get<any>('/analysis/student-dashboard');
