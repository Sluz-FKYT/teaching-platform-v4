import type { SaveQuestionPayload } from '@/types/question';
import { TYPED_EDITOR_JUDGE_CHOICES } from '../constants';
import { splitTypedEditorAnswerLines } from '../hydrators/fromQuestion';
import type { TypedEditorDraft, TypedEditorKind, TypedEditorSerializationResult } from '../types';

export type PersistedQuestionType = Exclude<TypedEditorKind, 'CODE'> | 'CODE';

export const serializeQuestionType = (type: TypedEditorKind): PersistedQuestionType => (type === 'CODE' ? 'SHORT' : type);

export const serializeQuestionDraft = (draft: TypedEditorDraft): TypedEditorSerializationResult => {
  if (draft.kind === 'SINGLE' || draft.kind === 'MULTI') {
    const serializedOptions = draft.objectiveOptions.map(option => ({
      key: option.key,
      content: option.content.trim(),
    }));
    const serializedAnswers = draft.objectiveOptions
      .filter(option => draft.selectedObjectiveOptionIds.includes(option.id))
      .map(option => option.key);

    return {
      optionsJson: JSON.stringify(serializedOptions),
      answerJson: JSON.stringify(serializedAnswers),
    };
  }

  if (draft.kind === 'JUDGE') {
    return {
      optionsJson: JSON.stringify(
        TYPED_EDITOR_JUDGE_CHOICES.map(choice => ({
          key: choice.value,
          content: choice.label,
        })),
      ),
      answerJson: JSON.stringify(draft.judgeAnswer ? [draft.judgeAnswer] : []),
    };
  }

  if (draft.kind === 'FILL') {
    return {
      optionsJson: null,
      answerJson: JSON.stringify(
        draft.fillBlanks.map(blank => ({
          index: blank.index,
          answers: splitTypedEditorAnswerLines(blank.answersText),
        })),
      ),
    };
  }

  return {
    optionsJson: null,
    answerJson: JSON.stringify(splitTypedEditorAnswerLines(draft.subjectiveAnswer)),
  };
};

interface BuildQuestionPayloadOptions {
  code: string;
  difficulty: string;
  defaultScore: number;
  analysisText: string;
}

export const buildQuestionPayloadFromDraft = (
  draft: TypedEditorDraft,
  options: BuildQuestionPayloadOptions,
): SaveQuestionPayload => {
  const serialized = serializeQuestionDraft(draft);

  return {
    code: options.code,
    type: serializeQuestionType(draft.kind),
    stem: draft.prompt.trim(),
    difficulty: options.difficulty,
    defaultScore: options.defaultScore,
    optionsJson: serialized.optionsJson,
    answerJson: serialized.answerJson,
    analysisText: options.analysisText.trim(),
  };
};
