export type QuestionType = 'SINGLE' | 'MULTI' | 'JUDGE' | 'FILL' | 'SHORT' | 'CODE';
export type QuestionDifficulty = 'EASY' | 'MEDIUM' | 'HARD';

export interface QuestionItem {
  id: number;
  code: string;
  type: QuestionType | string;
  stem: string;
  difficulty: QuestionDifficulty | string;
  defaultScore: number;
  optionsJson: string | null;
  answerJson: string;
  analysisText: string;
}

export interface SaveQuestionPayload {
  code: string;
  type: string;
  stem: string;
  difficulty: string;
  defaultScore: number;
  optionsJson: string | null;
  answerJson: string;
  analysisText: string;
}
