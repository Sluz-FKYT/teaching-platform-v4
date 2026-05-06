import request from '@/utils/request';
import type { QuestionItem, SaveQuestionPayload } from '@/types/question';

export const listQuestions = () => request.get<QuestionItem[]>('/questions');

export const createQuestion = (data: SaveQuestionPayload) => request.post<QuestionItem>('/questions', data);

export const updateQuestion = (id: number, data: SaveQuestionPayload) => request.put<QuestionItem>(`/questions/${id}`, data);

export const deleteQuestion = (id: number) => request.delete<{ deleted: boolean }>(`/questions/${id}`);
