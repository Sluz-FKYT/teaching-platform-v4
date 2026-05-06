export type QuestionSourceType = 'BANK' | 'INLINE'

export interface ConfiguredQuestionItem {
  localId: string
  questionBankId?: number | null
  sourceType: QuestionSourceType
  questionType: string
  stem: string
  sortOrder: number
  score: number
  options?: Array<{ key: string; label: string }> | null
  answerJson?: string | null
  scoringConfigJson?: string | null
}
