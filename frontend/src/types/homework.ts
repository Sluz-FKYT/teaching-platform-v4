import type { UnifiedQuestionAnswerPayload } from './question-answer';

export type HomeworkStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED' | string;

export type HomeworkSubmissionStatus = 'SAVED' | 'SUBMITTED' | 'GRADED' | string;

export type ScoreVisibilityMode = 'IMMEDIATE' | 'AFTER_SUBMIT' | 'AFTER_TEACHER_CONFIRM' | 'HIDDEN' | string;

export interface HomeworkQuestionAnswer {
  id: number;
  homeworkQuestionId: number;
  answerText?: string | null;
  answerJson?: string | null;
  attachmentPath?: string | null;
  score?: number | null;
  autoScore?: number | null;
  suggestedScore?: number | null;
  scoreSource?: string | null;
  judgeDetail?: string | null;
  teacherComment?: string | null;
  acceptedAutoScore?: boolean | null;
}

export interface HomeworkQuestionItem {
  id: number;
  questionId?: number | null;
  sortOrder: number;
  type: string;
  stem: string;
  options?: Array<{ key: string; label: string }> | string | null;
  score: number;
  answer?: HomeworkQuestionAnswer | null;
}

export interface HomeworkListItem {
  id: number;
  title: string;
  description: string;
  classId: number;
  className: string;
  status: HomeworkStatus;
  attachmentPath?: string | null;
  startAt?: string | null;
  dueAt?: string | null;
  scoreVisibilityMode?: ScoreVisibilityMode | null;
  submissionCount?: number;
  submittedCount?: number;
  gradedCount?: number;
  pendingCount?: number;
  submissionStatus?: HomeworkSubmissionStatus | null;
  totalScore?: number | null;
}

export interface StudentHomeworkDetail {
  id: number;
  title: string;
  description: string;
  classId: number;
  className: string;
  status: HomeworkStatus;
  attachmentPath?: string | null;
  answerFilePath?: string | null;
  attachmentAnswerPath?: string | null;
  hasSubmittedAttachment?: boolean;
  remainingMinutes?: number | null;
  startAt?: string | null;
  dueAt?: string | null;
  scoreVisibilityMode?: ScoreVisibilityMode | null;
  submissionStatus?: HomeworkSubmissionStatus | null;
  answerText?: string | null;
  questions?: HomeworkQuestionItem[];
  plagiarismRate?: number | null;
  totalScore?: number | null;
  teacherComment?: string | null;
}

export interface HomeworkSubmissionItem {
  id: number;
  homeworkId: number;
  homeworkTitle: string;
  studentId: number;
  studentName: string;
  studentUsername: string;
  submitStatus: HomeworkSubmissionStatus;
  answerText: string;
  answerFilePath?: string | null;
  plagiarismRate?: number | null;
  topMatchSummary?: string | null;
  totalScore?: number | null;
  teacherComment?: string | null;
  questions?: HomeworkQuestionItem[];
  classStudentCount?: number;
  submittedAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateHomeworkRequest {
  title: string;
  description: string;
  classId: number;
  status: HomeworkStatus;
  scoreVisibilityMode?: ScoreVisibilityMode | null;
  attachmentPath?: string | null;
  startAt?: string | null;
  dueAt?: string | null;
}

export interface GradeHomeworkRequest {
  totalScore: number;
  teacherComment: string;
}

export interface SubmitHomeworkRequest {
  answerText?: string;
  attachment?: string | null;
  attachmentPath?: string | null;
  answers?: SubmitHomeworkAnswerItem[];
}

export interface SubmitHomeworkAnswerItem extends Omit<UnifiedQuestionAnswerPayload, 'questionRefId'> {
  homeworkQuestionId: number;
}

export interface ChangeHomeworkStatusRequest {
  status: HomeworkStatus;
}

export interface GradeHomeworkResult {
  graded: boolean;
  totalScore: number;
}

export interface AddHomeworkQuestionFromBankRequest {
  questionId: number;
  sortOrder: number;
  questionScore: number;
}

export interface AddInlineHomeworkQuestionRequest {
  type: string;
  stem: string;
  questionScore: number;
  scoringConfigJson?: string | null;
  saveToQuestionBank?: boolean;
  sortOrder: number;
}

export interface GradeHomeworkAnswerRequest {
  score?: number;
  teacherComment?: string;
  acceptSuggested?: boolean;
}
