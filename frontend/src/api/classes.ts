import request from '@/utils/request';
import type { PageResult } from '@/types/common';
import type { CreateClassPayload, TeachingClass, UpdateClassPayload } from '@/types/class';

export const listClasses = () => request.get<PageResult<TeachingClass>>('/classes');

export const createClass = (data: CreateClassPayload) => request.post<TeachingClass>('/classes', data);

export const updateClass = (id: number, data: UpdateClassPayload) => request.put<TeachingClass>(`/classes/${id}`, data);

export const deleteClass = (id: number) => request.delete<{ deleted: boolean }>(`/classes/${id}`);
