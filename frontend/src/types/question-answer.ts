export interface UnifiedQuestionAnswerPayload {
  questionRefId: number
  questionType: string
  answerText?: string | null
  answerJson?: string | null
  selectedOptions?: string[]
  attachmentPath?: string | null
}
