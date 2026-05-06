<template>
  <div class="page lab-steps-console workbench-page workbench-page--locked">
    <section class="lab-steps-console__header">
      <div class="lab-steps-console__header-main">
        <button type="button" class="lab-steps-console__back-link" @click="router.back()">
          <span class="material-symbols-outlined">arrow_back</span>
          <span>返回实验列表</span>
        </button>

        <div>
          <p class="lab-steps-console__eyebrow">教师实验工作台 / 步骤配置</p>
          <h1 class="lab-steps-console__title">实验题项配置台</h1>
          <p class="lab-steps-console__subtitle">
            左侧切换实验题项，中间集中维护题干与结构化答案，右侧压缩展示分值、约束与保存时建题选项。
          </p>
        </div>
      </div>

      <div class="lab-steps-console__header-side">
        <div class="lab-steps-console__meta-row">
          <span class="lab-steps-console__meta-chip lab-steps-console__meta-chip--accent">{{ labSummaryTitle }}</span>
          <span class="lab-steps-console__meta-chip">{{ rows.length }} 个题项</span>
          <span class="lab-steps-console__meta-chip">总分 {{ totalScore }} 分</span>
          <span class="lab-steps-console__meta-chip">{{ completionSummary }}</span>
        </div>

        <div class="lab-steps-console__header-actions">
          <el-button @click="openCreatePanel">新增题项</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">
            {{ currentActionLabel }}
          </el-button>
        </div>
      </div>
    </section>

    <section class="lab-steps-console__workspace">
      <aside class="lab-steps-console__column lab-steps-console__column--steps">
        <section class="lab-steps-console__panel lab-steps-console__panel--full">
          <header class="lab-steps-console__panel-header">
            <div>
              <p class="lab-steps-console__section-kicker">步骤轨道</p>
              <h2 class="lab-steps-console__section-title">实验题项</h2>
            </div>
            <span class="lab-steps-console__summary-chip">{{ rows.length }}</span>
          </header>

          <div class="lab-steps-console__panel-body lab-steps-console__panel-body--steps" v-loading="loading">
            <div class="lab-steps-console__step-list">
              <button
                v-for="row in sortedRows"
                :key="row.id"
                type="button"
                class="lab-steps-console__step-card"
                :class="{ 'is-active': row.id === activeExistingStepId, 'is-ready': isStepConfigured(row) }"
                @click="selectStep(row.id)"
              >
                <div class="lab-steps-console__step-card-top">
                  <div class="lab-steps-console__step-index">{{ String(row.stepNo).padStart(2, '0') }}</div>
                  <div class="lab-steps-console__step-copy">
                    <div class="lab-steps-console__step-topline">
                      <span>{{ getLabQuestionTypeLabel(normalizeLabQuestionType(row.questionType)) }}</span>
                      <span>{{ row.stepScore }} 分</span>
                    </div>
                    <strong>{{ row.title }}</strong>
                    <p>{{ summarizeText(row.content, 40) || '未填写题项说明' }}</p>
                  </div>
                </div>

                <div class="lab-steps-console__step-card-bottom">
                  <span class="lab-steps-console__step-state" :class="{ 'is-ready': isStepConfigured(row) }">
                    {{ isStepConfigured(row) ? '已配置' : '待完善' }}
                  </span>
                  <button
                    type="button"
                    class="lab-steps-console__inline-delete"
                    title="删除题项"
                    @click.stop="remove(row.id)"
                  >
                    <span class="material-symbols-outlined">delete</span>
                  </button>
                </div>
              </button>

              <button type="button" class="lab-steps-console__add-step" @click="openCreatePanel">
                <span class="material-symbols-outlined">add_circle</span>
                <span>新增实验题项</span>
              </button>
            </div>
          </div>
        </section>
      </aside>

      <section class="lab-steps-console__column lab-steps-console__column--editor">
        <section class="lab-steps-console__panel lab-steps-console__panel--full">
          <header class="lab-steps-console__panel-header lab-steps-console__panel-header--editor">
            <div>
              <p class="lab-steps-console__section-kicker">主编辑区</p>
              <h2 class="lab-steps-console__section-title">{{ activeStepHeading }}</h2>
            </div>

            <div class="lab-steps-console__panel-meta">
              <span class="lab-steps-console__summary-chip">{{ currentQuestionTypeLabel }}</span>
              <span class="lab-steps-console__summary-chip">{{ allowPasteLabel }}</span>
              <span class="lab-steps-console__save-indicator" :class="{ 'is-ready': isReadyToSubmit }">
                {{ isReadyToSubmit ? '可保存' : '待完善' }}
              </span>
            </div>
          </header>

          <div class="lab-steps-console__panel-body lab-steps-console__panel-body--editor">
            <section class="question-dialog__group lab-steps-console__group lab-steps-console__group--editor-core">
              <div class="question-dialog__group-title">题项基础信息</div>
              <el-form label-position="top" class="lab-steps-console__form">
                <div class="question-dialog__grid question-dialog__grid--step-base">
                  <el-form-item label="题项序号">
                    <el-input-number v-model="form.stepNo" :min="1" :step="1" style="width: 100%" />
                  </el-form-item>
                  <el-form-item label="题项标题">
                    <el-input
                      v-model="form.title"
                      maxlength="100"
                      show-word-limit
                      placeholder="例如：事务隔离级别分析、接口填空、代码实现说明"
                    />
                  </el-form-item>
                </div>
              </el-form>
            </section>

            <section class="question-dialog__group lab-steps-console__group lab-steps-console__group--editor-fill">
              <div class="question-dialog__group-title question-dialog__group-title--row">
                <span>题干与答案结构</span>
                <div v-if="typedEditor.draft.kind === 'FILL'" class="question-dialog__group-actions">
                  <el-button type="primary" size="small" class="lab-steps-console__cta-button" @click="triggerBlankInsert">
                    插入填空位
                  </el-button>
                </div>
              </div>

              <el-form v-if="typedEditor.draft.kind === 'FILL'" label-position="top" class="lab-steps-console__form">
                <el-form-item label="题项说明 / 题干（支持插入填空位）">
                  <el-input
                    ref="fillBlankEditorPromptRef"
                    v-model="typedEditor.draft.prompt"
                    type="textarea"
                    :rows="11"
                    maxlength="2000"
                    show-word-limit
                    resize="vertical"
                    placeholder="填写实验步骤说明，并在需要的位置插入如【填空1】的填空位。"
                  />
                </el-form-item>
              </el-form>

              <el-form v-else label-position="top" class="lab-steps-console__form">
                <el-form-item label="题项说明 / 题干">
                  <el-input
                    v-model="typedEditor.draft.prompt"
                    type="textarea"
                    :rows="11"
                    maxlength="2000"
                    show-word-limit
                    resize="vertical"
                    placeholder="填写实验步骤要求、作答边界、提交格式、评分关注点与上下文。"
                  />
                </el-form-item>
              </el-form>

              <TypedEditorShell title="结构化答案编辑" :messages="typedValidationMessages">
                <FillBlankEditor
                  v-if="typedEditor.draft.kind === 'FILL'"
                  ref="fillBlankEditorRef"
                  :prompt="typedEditor.draft.prompt"
                  :blanks="typedEditor.draft.fillBlanks"
                  prompt-label="题项说明 / 题干（支持插入填空位）"
                  placeholder="填写实验步骤说明，并在需要的位置插入如【填空1】的填空位。"
                  :rows="11"
                  :hide-prompt="true"
                  @update:prompt="typedEditor.draft.prompt = $event"
                  @update-blank-answer="updateBlankAnswer"
                  @insert-token="insertBlankToken"
                  @remove-blank="removeBlank"
                />

                <ObjectiveEditor
                  v-else-if="typedEditor.draft.kind === 'SINGLE' || typedEditor.draft.kind === 'MULTI' || typedEditor.draft.kind === 'JUDGE'"
                  :kind="typedEditor.draft.kind"
                  :options="typedEditor.draft.objectiveOptions"
                  :selected-ids="typedEditor.draft.selectedObjectiveOptionIds"
                  :judge-answer="typedEditor.draft.judgeAnswer"
                  @add-option="typedEditor.addObjectiveOption"
                  @remove-option="typedEditor.removeObjectiveOption"
                  @toggle-answer="typedEditor.toggleObjectiveAnswer"
                  @update-option-content="updateObjectiveOptionContent"
                  @update:judge-answer="typedEditor.draft.judgeAnswer = $event"
                />

                <SubjectiveEditor
                  v-else-if="typedEditor.draft.kind === 'SHORT' || typedEditor.draft.kind === 'CODE'"
                  :kind="typedEditor.draft.kind"
                  :model-value="typedEditor.draft.subjectiveAnswer"
                  @update:model-value="updateSubjectiveAnswer"
                />
              </TypedEditorShell>
            </section>

            <section class="question-dialog__group lab-steps-console__group lab-steps-console__group--muted">
              <div class="question-dialog__group-title">当前保存检查</div>
              <div class="lab-steps-console__check-grid">
                <article class="lab-steps-console__check-card">
                  <span>题项标题</span>
                  <strong>{{ form.title.trim() ? '已填写' : '未填写' }}</strong>
                </article>
                <article class="lab-steps-console__check-card">
                  <span>题干字数</span>
                  <strong>{{ contentLength ? `${contentLength} 字` : '未填写' }}</strong>
                </article>
                <article class="lab-steps-console__check-card">
                  <span>答案结构</span>
                  <strong>{{ typedEditor.modeSummary.value }}</strong>
                </article>
                <article class="lab-steps-console__check-card">
                  <span>保存动作</span>
                  <strong>{{ activeExistingStepId ? '更新已有题项' : '创建新题项' }}</strong>
                </article>
              </div>
              <p class="lab-steps-console__hint">{{ completionHint }}</p>
            </section>
          </div>
        </section>
      </section>

      <aside class="lab-steps-console__column lab-steps-console__column--rail">
        <section class="lab-steps-console__panel lab-steps-console__panel--full">
          <header class="lab-steps-console__panel-header">
            <div>
              <p class="lab-steps-console__section-kicker">约束侧栏</p>
              <h2 class="lab-steps-console__section-title">分值与同步</h2>
            </div>
            <span class="lab-steps-console__summary-chip">支撑信息</span>
          </header>

          <div class="lab-steps-console__panel-body lab-steps-console__panel-body--rail">
            <section class="question-dialog__group lab-steps-console__group lab-steps-console__group--editor-fill">
              <div class="question-dialog__group-title">题型与执行约束</div>
              <el-form label-position="top" class="lab-steps-console__form">
                <div class="question-dialog__grid question-dialog__grid--rail">
                  <el-form-item label="题型">
                    <el-select v-model="form.questionType" placeholder="请选择题型" @change="handleQuestionTypeChange">
                      <el-option label="简答题" value="SHORT_ANSWER" />
                      <el-option label="填空题" value="FILL_BLANK" />
                      <el-option label="单选题" value="SINGLE_CHOICE" />
                      <el-option label="多选题" value="MULTIPLE_CHOICE" />
                      <el-option label="判断题" value="TRUE_FALSE" />
                      <el-option label="代码题" value="CODE" />
                    </el-select>
                  </el-form-item>

                  <el-form-item label="题项分值">
                    <el-input-number v-model="form.stepScore" :min="0" style="width: 100%" />
                  </el-form-item>
                </div>
              </el-form>

              <div class="lab-steps-console__switch-row">
                <div>
                  <strong>粘贴策略</strong>
                  <p>控制学生端是否允许直接粘贴文本或代码。</p>
                </div>
                <el-switch v-model="form.allowPaste" active-text="允许" inactive-text="限制" />
              </div>
            </section>

            <section v-if="typedEditor.draft.kind === 'SHORT'" class="question-dialog__group lab-steps-console__group">
              <div class="question-dialog__group-title question-dialog__group-title--row">
                <span>主观题约束</span>
                <el-button link type="primary" @click="addKeyword">新增关键词</el-button>
              </div>

              <div class="lab-steps-console__rule-list">
                <div v-for="(keyword, index) in subjectiveKeywords" :key="`keyword-${index}`" class="lab-steps-console__rule-row">
                  <el-input v-model="subjectiveKeywords[index]" placeholder="关键词，例如：事务、索引、隔离级别" />
                  <el-button link type="danger" @click="removeKeyword(index)">删除</el-button>
                </div>
              </div>

              <el-form label-position="top" class="lab-steps-console__form">
                <div class="question-dialog__grid question-dialog__grid--rail">
                  <el-form-item label="最小字数">
                    <el-input-number v-model="subjectiveSettings.minLength" :min="0" style="width: 100%" />
                  </el-form-item>
                  <el-form-item label="讲评模板">
                    <el-input
                      v-model="subjectiveSettings.commentTemplate"
                      type="textarea"
                      :rows="4"
                      maxlength="300"
                      show-word-limit
                      resize="vertical"
                      placeholder="可选：教师讲评模板或评分摘要。"
                    />
                  </el-form-item>
                </div>
              </el-form>
            </section>

            <section v-if="typedEditor.draft.kind === 'FILL'" class="question-dialog__group lab-steps-console__group">
              <div class="question-dialog__group-title">填空判分约束</div>
              <div class="lab-steps-console__switch-row lab-steps-console__switch-row--compact">
                <div>
                  <strong>匹配策略</strong>
                  <p>控制 accepted answers 是否忽略大小写。</p>
                </div>
                <el-switch v-model="fillSettings.ignoreCase" active-text="忽略大小写" inactive-text="区分大小写" />
              </div>

              <div class="lab-steps-console__blank-summary">
                共 {{ typedEditor.draft.fillBlanks.length }} 个填空位，答案仍由中栏共享 typed-editor 维护。
              </div>
            </section>

            <section v-if="typedEditor.draft.kind === 'CODE'" class="question-dialog__group lab-steps-console__group">
              <div class="question-dialog__group-title">代码题约束</div>
              <el-form label-position="top" class="lab-steps-console__form">
                <div class="question-dialog__grid question-dialog__grid--rail">
                  <el-form-item label="默认语言">
                    <el-select v-model="codeSettings.language" placeholder="请选择语言">
                      <el-option label="Java" value="JAVA" />
                      <el-option label="Python" value="PYTHON" />
                      <el-option label="C/C++" value="CPP" />
                      <el-option label="JavaScript" value="JAVASCRIPT" />
                      <el-option label="文本" value="TEXT" />
                    </el-select>
                  </el-form-item>
                </div>
              </el-form>
            </section>

            <section class="question-dialog__group lab-steps-console__group">
              <div class="question-dialog__group-title">保存时题库创建</div>
              <label class="lab-steps-console__sync-toggle">
                <el-checkbox v-model="questionSync.enabled">保存实验题项后，同时创建 1 条题库题目</el-checkbox>
                <p>仅在本次保存成功后创建，首次同步完成后与实验题项互不绑定，后续分别维护。</p>
              </label>

              <div v-if="questionSync.enabled" class="lab-steps-console__sync-fields">
                <el-form label-position="top" class="lab-steps-console__form">
                  <div class="question-dialog__grid question-dialog__grid--rail">
                    <el-form-item label="题库难度">
                      <el-select v-model="questionSync.difficulty" placeholder="请选择难度">
                        <el-option label="基础" value="EASY" />
                        <el-option label="进阶" value="MEDIUM" />
                        <el-option label="挑战" value="HARD" />
                      </el-select>
                    </el-form-item>

                    <el-form-item label="教师解析">
                      <el-input
                        v-model="questionSync.analysisText"
                        type="textarea"
                        :rows="4"
                        maxlength="500"
                        show-word-limit
                        resize="vertical"
                        placeholder="可选：保存到题库题目的教师解析、讲评或评分依据。"
                      />
                    </el-form-item>
                  </div>
                </el-form>

                <div class="lab-steps-console__sync-preview">
                  <span>保存顺序</span>
                  <strong>校验 → 保存实验题项 → 创建题库题目</strong>
                  <p>题库题型直接使用共享 typed-editor 草稿转换；代码题按现有题库兼容规则以简答题落库。</p>
                </div>
              </div>
            </section>
          </div>
        </section>
      </aside>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createQuestion } from '@/api/questions';
import { createTeacherLabStep, deleteTeacherLabStep, listTeacherLabs, listTeacherLabSteps, updateTeacherLabStep } from '@/api/labs';
import type { LabItem, LabStepItem } from '@/types/lab';
import FillBlankEditor from './components/typed-editor/FillBlankEditor.vue';
import ObjectiveEditor from './components/typed-editor/ObjectiveEditor.vue';
import SubjectiveEditor from './components/typed-editor/SubjectiveEditor.vue';
import TypedEditorShell from './components/typed-editor/TypedEditorShell.vue';
import { buildLabStepPayloadFromDraft, buildQuestionPayloadFromLabStepDraft, validateLabStepDraft } from './components/typed-editor/adapters/labStepAdapter';
import { useTypedEditor } from './components/typed-editor/useTypedEditor';
import {
  createEmptyLabStepEditorState,
  getLabQuestionTypeLabel,
  hydrateTypedEditorFromLabStep,
  mapLabQuestionTypeToTypedKind,
  normalizeLabQuestionType,
} from './components/typed-editor/hydrators/fromLabStep';

interface FillBlankEditorExpose {
  insertTokenAtCursor: () => void;
  focusAt: (position: number) => void;
}

interface ElTextareaLike {
  textarea?: HTMLTextAreaElement;
}

const route = useRoute();
const router = useRouter();
const labId = Number(route.params.id);

const loading = ref(false);
const submitting = ref(false);
const rows = ref<LabStepItem[]>([]);
const labs = ref<LabItem[]>([]);
const activeExistingStepId = ref<number | null>(null);
const fillBlankEditorRef = ref<FillBlankEditorExpose | null>(null);
const fillBlankEditorPromptRef = ref<ElTextareaLike | null>(null);

const typedEditor = useTypedEditor({
  validationContext: {
    promptLabel: '题项说明 / 题干',
    fillAnswerLabel: index => `请填写空 ${index} 的正确答案`,
    subjectiveRequiredLabel: {
      SHORT: '请填写参考答案或评分要点',
      CODE: '请填写代码题参考实现或评分要点',
    },
  },
});

const form = reactive({
  stepNo: 1,
  title: '',
  questionType: 'SHORT_ANSWER',
  stepScore: 10,
  allowPaste: true,
});

const subjectiveKeywords = ref<string[]>(['']);
const subjectiveSettings = reactive({
  minLength: 0,
  commentTemplate: '',
});
const fillSettings = reactive({
  ignoreCase: true,
});
const codeSettings = reactive({
  language: 'JAVA',
});
const questionSync = reactive({
  enabled: false,
  difficulty: 'EASY' as const,
  analysisText: '',
});

const labSummaryTitle = computed(() => labs.value.find(item => item.id === labId)?.title?.trim() || `实验 #${labId}`);
const sortedRows = computed(() => [...rows.value].sort((left, right) => left.stepNo - right.stepNo));
const totalScore = computed(() => rows.value.reduce((sum, row) => sum + (Number(row.stepScore) || 0), 0));
const currentQuestionTypeLabel = computed(() => getLabQuestionTypeLabel(normalizeLabQuestionType(form.questionType)));
const allowPasteLabel = computed(() => (form.allowPaste ? '允许粘贴' : '限制粘贴'));
const typedValidationMessages = computed(() => validateLabStepDraft(form.questionType, typedEditor.draft, form.title));
const contentLength = computed(() => typedEditor.draft.prompt.trim().length);
const isReadyToSubmit = computed(() => typedValidationMessages.value.length === 0);
const completionSummary = computed(() => (isReadyToSubmit.value ? '可保存' : '待完善'));
const currentActionLabel = computed(() => (activeExistingStepId.value ? '保存当前题项' : '创建当前题项'));
const activeStepHeading = computed(() => activeExistingStepId.value ? `题项 ${form.stepNo} · ${form.title.trim() || '未命名题项'}` : `新增题项草稿 · 序号 ${form.stepNo}`);
const completionHint = computed(() => {
  if (isReadyToSubmit.value) {
    return activeExistingStepId.value ? '当前题项结构已完整，保存后会保留当前选中状态。' : '当前草稿已达到创建条件，可直接保存为新的实验题项。';
  }
  return typedValidationMessages.value[0] ?? '请继续补齐当前题项的必填信息。';
});

const summarizeText = (text: string, limit: number) => {
  const normalized = text.trim();
  return normalized.length > limit ? `${normalized.slice(0, limit)}…` : normalized;
};

const applyHydratedState = (row?: LabStepItem) => {
  const hydrated = row ? hydrateTypedEditorFromLabStep(row) : createEmptyLabStepEditorState(form.questionType);
  typedEditor.reset(hydrated.draft.kind);
  typedEditor.draft.prompt = hydrated.draft.prompt;
  typedEditor.draft.objectiveOptions = hydrated.draft.objectiveOptions.map(option => ({ ...option }));
  typedEditor.draft.selectedObjectiveOptionIds = [...hydrated.draft.selectedObjectiveOptionIds];
  typedEditor.draft.judgeAnswer = hydrated.draft.judgeAnswer;
  typedEditor.draft.fillBlanks = hydrated.draft.fillBlanks.map(blank => ({ ...blank }));
  typedEditor.draft.subjectiveAnswer = hydrated.draft.subjectiveAnswer;
  subjectiveKeywords.value = hydrated.draft.kind === 'SHORT'
    ? (hydrated.draft.subjectiveAnswer.trim() ? hydrated.draft.subjectiveAnswer.split(/\r?\n/).map(item => item.trim()).filter(Boolean) : [''])
    : [''];
  subjectiveSettings.minLength = hydrated.subjectiveSettings.minLength;
  subjectiveSettings.commentTemplate = hydrated.subjectiveSettings.commentTemplate;
  fillSettings.ignoreCase = hydrated.fillSettings.ignoreCase;
  codeSettings.language = hydrated.codeSettings.language;
};

const resetQuestionSync = () => {
  questionSync.enabled = false;
  questionSync.difficulty = 'EASY';
  questionSync.analysisText = '';
};

const resetForm = () => {
  activeExistingStepId.value = null;
  form.stepNo = rows.value.length + 1;
  form.title = '';
  form.questionType = 'SHORT_ANSWER';
  form.stepScore = 10;
  form.allowPaste = true;
  applyHydratedState();
  resetQuestionSync();
};

const populateFormFromRow = (row: LabStepItem) => {
  activeExistingStepId.value = row.id;
  form.stepNo = row.stepNo;
  form.title = row.title;
  form.questionType = normalizeLabQuestionType(row.questionType);
  form.stepScore = row.stepScore;
  form.allowPaste = row.allowPaste;
  applyHydratedState(row);
  resetQuestionSync();
};

const fetchLabContext = async () => {
  try {
    labs.value = await listTeacherLabs();
  } catch {
    labs.value = [];
  }
};

const fetchData = async (preferredStepId?: number | null) => {
  loading.value = true;
  try {
    const result = await listTeacherLabSteps(labId);
    rows.value = result;

    if (!result.length) {
      resetForm();
      return;
    }

    const targetId = preferredStepId ?? activeExistingStepId.value ?? result[0]?.id ?? null;
    const targetRow = result.find(item => item.id === targetId) ?? [...result].sort((left, right) => left.stepNo - right.stepNo)[0];
    if (targetRow) {
      populateFormFromRow(targetRow);
    }
  } finally {
    loading.value = false;
  }
};

const openCreatePanel = () => {
  resetForm();
};

const selectStep = (stepId: number) => {
  const row = rows.value.find(item => item.id === stepId);
  if (row) {
    populateFormFromRow(row);
  }
};

const handleQuestionTypeChange = () => {
  applyHydratedState();
};

const updateObjectiveOptionContent = ({ id, content }: { id: string; content: string }) => {
  typedEditor.draft.objectiveOptions = typedEditor.draft.objectiveOptions.map(option => option.id === id ? { ...option, content } : option);
};

const updateBlankAnswer = ({ index, answersText }: { index: number; answersText: string }) => {
  typedEditor.draft.fillBlanks = typedEditor.draft.fillBlanks.map(blank => blank.index === index ? { ...blank, answersText } : blank);
};

const removeBlank = (index: number) => {
  typedEditor.removeBlank(index);
};

const triggerBlankInsert = () => {
  const textarea = fillBlankEditorPromptRef.value?.textarea;
  if (!textarea) {
    fillBlankEditorRef.value?.insertTokenAtCursor();
    return;
  }

  insertBlankToken({
    start: textarea.selectionStart ?? typedEditor.draft.prompt.length,
    end: textarea.selectionEnd ?? typedEditor.draft.prompt.length,
  });
};

const insertBlankToken = async (selection?: { start: number; end: number }) => {
  const inserted = typedEditor.insertBlankToken(selection);
  if (inserted) {
    await nextTick();
    const textarea = fillBlankEditorPromptRef.value?.textarea;
    textarea?.focus();
    textarea?.setSelectionRange(inserted.nextCursor, inserted.nextCursor);
  }
};

const addKeyword = () => {
  subjectiveKeywords.value.push('');
};

const removeKeyword = (index: number) => {
  subjectiveKeywords.value.splice(index, 1);
  if (!subjectiveKeywords.value.length) {
    subjectiveKeywords.value = [''];
  }
};

const syncKeywordsFromSubjectiveAnswer = (value: string) => {
  if (typedEditor.draft.kind !== 'SHORT') {
    return;
  }

  const nextKeywords = value
    .split(/\r?\n/)
    .map(item => item.trim())
    .filter(Boolean);

  subjectiveKeywords.value = nextKeywords.length ? nextKeywords : [''];
};

const updateSubjectiveAnswer = (value: string) => {
  typedEditor.draft.subjectiveAnswer = value;
  syncKeywordsFromSubjectiveAnswer(value);
};

const syncSubjectiveAnswers = () => {
  if (typedEditor.draft.kind !== 'SHORT') {
    return;
  }
  typedEditor.draft.subjectiveAnswer = subjectiveKeywords.value.map(item => item.trim()).filter(Boolean).join('\n');
};

watch(
  subjectiveKeywords,
  () => {
    if (typedEditor.draft.kind === 'SHORT') {
      syncSubjectiveAnswers();
    }
  },
  { deep: true },
);

const buildPayload = () => {
  syncSubjectiveAnswers();
  return buildLabStepPayloadFromDraft({
    stepNo: form.stepNo,
    title: form.title.trim(),
    questionType: form.questionType,
    stepScore: form.stepScore,
    allowPaste: form.allowPaste,
  }, typedEditor.draft, {
    subjective: { ...subjectiveSettings },
    fill: { ...fillSettings },
    code: { ...codeSettings },
  });
};

const isStepConfigured = (row: LabStepItem) => {
  const hydrated = hydrateTypedEditorFromLabStep(row);
  return validateLabStepDraft(row.questionType, hydrated.draft, row.title).length === 0;
};

const submitForm = async () => {
  syncSubjectiveAnswers();
  if (typedValidationMessages.value.length) {
    ElMessage.warning(typedValidationMessages.value[0]);
    return;
  }

  submitting.value = true;
  try {
    const wasEditing = activeExistingStepId.value !== null;
    const payload = buildPayload();
    const savedStep = activeExistingStepId.value
      ? await updateTeacherLabStep(activeExistingStepId.value, payload)
      : await createTeacherLabStep(labId, payload);

    let partialMessage = '';
    if (questionSync.enabled) {
      try {
        const questionPayload = buildQuestionPayloadFromLabStepDraft(
          labId,
          savedStep.stepNo,
          typedEditor.draft,
          { difficulty: questionSync.difficulty, analysisText: questionSync.analysisText },
          Math.max(1, Number(form.stepScore) || 1),
        );
        const createdQuestion = await createQuestion(questionPayload);
        await fetchData(savedStep.id);
        ElMessage.success(`${wasEditing ? '题项保存成功' : '题项创建成功'}，并已创建题库题目 ${createdQuestion.code}`);
        return;
      } catch (error) {
        partialMessage = error instanceof Error && error.message ? error.message : '题库题目创建失败';
      }
    }

    await fetchData(savedStep.id);
    if (partialMessage) {
      ElMessage.warning(`${wasEditing ? '题项已保存' : '题项已创建'}，但题库题目创建失败：${partialMessage}。本次仅完成实验题项保存，后续请分别维护。`);
      return;
    }
    ElMessage.success(wasEditing ? '题项保存成功' : '题项创建成功');
  } finally {
    submitting.value = false;
  }
};

const remove = async (stepId: number) => {
  await ElMessageBox.confirm('删除该题项后不可恢复，是否继续？', '删除确认', { type: 'warning' });
  await deleteTeacherLabStep(stepId);
  ElMessage.success('题项删除成功');

  const remaining = rows.value.filter(row => row.id !== stepId);
  const nextPreferredId = remaining.find(row => row.id === activeExistingStepId.value)?.id ?? remaining[0]?.id ?? null;
  await fetchData(nextPreferredId);
};

onMounted(async () => {
  await Promise.all([fetchLabContext(), fetchData()]);
});
</script>

<style scoped>
.lab-steps-console {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;
}

.lab-steps-console__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  padding: 18px 20px;
  border: 1px solid #dbe4f0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.06);
  flex-shrink: 0;
}

.lab-steps-console__header-main,
.lab-steps-console__header-side {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lab-steps-console__header-side {
  align-items: flex-end;
  min-width: 320px;
}

.lab-steps-console__back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: fit-content;
  padding: 0;
  border: none;
  background: transparent;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
}

.lab-steps-console__eyebrow,
.lab-steps-console__section-kicker {
  margin: 0 0 8px;
  color: #0f766e;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.lab-steps-console__title {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  line-height: 1.1;
  font-weight: 800;
}

.lab-steps-console__subtitle {
  margin: 8px 0 0;
  max-width: 780px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.lab-steps-console__meta-row,
.lab-steps-console__header-actions,
.lab-steps-console__panel-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.lab-steps-console__header-actions {
  justify-content: flex-end;
}

.lab-steps-console__meta-chip,
.lab-steps-console__summary-chip,
.lab-steps-console__save-indicator {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.lab-steps-console__meta-chip,
.lab-steps-console__summary-chip {
  background: #f0fdfa;
  color: #0f766e;
}

.lab-steps-console__meta-chip--accent {
  background: #0f766e;
  color: #fff;
}

.lab-steps-console__save-indicator {
  background: #fff7ed;
  color: #c2410c;
}

.lab-steps-console__save-indicator.is-ready {
  background: #ecfdf5;
  color: #047857;
}

.lab-steps-console__workspace {
  flex: 1;
  min-height: 0;
  height: 100%;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr) 320px;
  gap: 16px;
  overflow: visible;
}

.lab-steps-console__column {
  min-width: 0;
  min-height: 0;
  display: flex;
  overflow: visible;
}

.lab-steps-console__panel {
  border: 1px solid #dbe4f0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.06);
}

.lab-steps-console__panel--full {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.lab-steps-console__panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-bottom: 1px solid #e2e8f0;
}

.lab-steps-console__panel-header--editor {
  align-items: flex-start;
}

.lab-steps-console__section-title {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 800;
}

.lab-steps-console__panel-body {
  min-height: 0;
  overflow: auto;
}

.lab-steps-console__panel-body--steps {
  padding: 14px;
}

.lab-steps-console__panel-body--editor,
.lab-steps-console__panel-body--rail {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px;
}

.lab-steps-console__panel-body--editor {
  flex: 1;
  min-height: 0;
  align-items: stretch;
  padding-right: 16px;
  overflow: auto;
}

.lab-steps-console__panel-body--rail,
.lab-steps-console__panel-body--steps {
  padding-right: 10px;
}

.lab-steps-console__step-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.lab-steps-console__step-card,
.lab-steps-console__add-step {
  width: 100%;
  border: 1px solid #dbe4f0;
  border-radius: 18px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.lab-steps-console__step-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  text-align: left;
}

.lab-steps-console__step-card:hover,
.lab-steps-console__add-step:hover {
  border-color: #99f6e4;
  box-shadow: 0 12px 24px rgba(20, 184, 166, 0.12);
  transform: translateY(-1px);
}

.lab-steps-console__step-card.is-active {
  border-color: #14b8a6;
  background: #f0fdfa;
}

.lab-steps-console__step-card-top,
.lab-steps-console__step-card-bottom {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  justify-content: space-between;
}

.lab-steps-console__step-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: #e2e8f0;
  color: #0f172a;
  font-size: 12px;
  font-weight: 800;
  flex-shrink: 0;
}

.lab-steps-console__step-card.is-active .lab-steps-console__step-index {
  background: #0f766e;
  color: #fff;
}

.lab-steps-console__step-copy {
  min-width: 0;
  flex: 1;
}

.lab-steps-console__step-topline {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 6px;
  color: #0f766e;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.lab-steps-console__step-copy strong {
  display: block;
  color: #0f172a;
  font-size: 14px;
  line-height: 1.45;
}

.lab-steps-console__step-copy p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.lab-steps-console__step-state {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: #fff7ed;
  color: #c2410c;
  font-size: 11px;
  font-weight: 700;
}

.lab-steps-console__step-state.is-ready {
  background: #ecfdf5;
  color: #047857;
}

.lab-steps-console__inline-delete {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 10px;
  background: #f8fafc;
  color: #ef4444;
  cursor: pointer;
}

.lab-steps-console__add-step {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  border-style: dashed;
  color: #0f766e;
  font-size: 13px;
  font-weight: 700;
}

.lab-steps-console__group--muted {
  background: #f8fafc;
}

.lab-steps-console__check-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.lab-steps-console__check-card {
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
}

.lab-steps-console__check-card span {
  display: block;
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.lab-steps-console__check-card strong {
  display: block;
  margin-top: 6px;
  color: #0f172a;
  font-size: 14px;
  line-height: 1.45;
}

.lab-steps-console__hint,
.lab-steps-console__blank-summary,
.lab-steps-console__switch-row p,
.lab-steps-console__sync-toggle p,
.lab-steps-console__sync-preview p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.lab-steps-console__switch-row,
.lab-steps-console__sync-preview {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #f8fafc;
}

.lab-steps-console__switch-row--compact {
  margin-bottom: 10px;
}

.lab-steps-console__rule-list,
.lab-steps-console__sync-fields {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.lab-steps-console__rule-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
}

.lab-steps-console__sync-toggle {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.question-dialog__group,
.question-dialog__validation {
  border: 1px solid #e2e8f0;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.96);
}

.question-dialog__group {
  padding: 16px;
}

.lab-steps-console__group--editor-core {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 0 0 auto;
  min-height: 0;
}

.lab-steps-console__group--editor-core > .lab-steps-console__form {
  flex: 0 0 auto;
}

.lab-steps-console__group--editor-fill {
  display: flex;
  flex-direction: column;
  flex: 0 0 auto;
  min-height: 0;
}

.lab-steps-console__group--editor-fill > .lab-steps-console__form,
.lab-steps-console__group--editor-fill > .question-dialog__group-title--row {
  flex: 0 0 auto;
}

.lab-steps-console__group--editor-fill > .typed-editor-shell {
  flex: 0 0 auto;
  min-height: 0;
}

.question-dialog__group-title {
  margin-bottom: 12px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 800;
}

.question-dialog__group-title--row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.question-dialog__group-actions {
  display: flex;
  gap: 8px;
}

.lab-steps-console__cta-button :deep(span) {
  display: inline-flex;
  align-items: center;
}

.lab-steps-console__cta-button :deep(.el-button) {
  font-weight: 700;
}

.question-dialog__group-actions :deep(.el-button--primary) {
  --el-button-bg-color: #0f766e;
  --el-button-border-color: #0f766e;
  --el-button-hover-bg-color: #0d9488;
  --el-button-hover-border-color: #0d9488;
  --el-button-active-bg-color: #115e59;
  --el-button-active-border-color: #115e59;
  min-height: 34px;
  padding-inline: 14px;
  border-radius: 999px;
  box-shadow: 0 10px 20px rgba(15, 118, 110, 0.18);
  font-weight: 700;
}

.question-dialog__group-actions :deep(.el-button--primary:hover) {
  transform: translateY(-1px);
}

.lab-steps-console__form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-dialog__grid {
  display: grid;
  gap: 14px;
}

.question-dialog__grid--step-base {
  grid-template-columns: 180px minmax(0, 1fr);
}

.question-dialog__grid--rail {
  grid-template-columns: 1fr;
}

.lab-steps-console__form :deep(.el-form-item) {
  margin-bottom: 0;
}

.lab-steps-console__form :deep(.el-form-item__label) {
  margin-bottom: 8px;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.lab-steps-console__form :deep(.el-input__wrapper),
.lab-steps-console__form :deep(.el-select__wrapper),
.lab-steps-console__form :deep(.el-textarea__inner),
.lab-steps-console__form :deep(.el-input-number) {
  border-radius: 14px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  background: #f8fafc;
}

.question-dialog__validation {
  padding: 14px 16px;
  border-color: #fed7aa;
  background: #fff7ed;
}

.question-dialog__validation-title {
  color: #9a3412;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

@media (max-width: 1280px) {
  .lab-steps-console__workspace {
    grid-template-columns: 260px minmax(0, 1fr) 280px;
  }
}

@media (max-width: 1100px) {
  .lab-steps-console__header-side {
    min-width: 0;
  }
}

@media (max-width: 820px) {
  .lab-steps-console__workspace {
    grid-template-columns: 180px minmax(0, 1fr);
    gap: 12px;
  }

  .lab-steps-console__column--rail {
    grid-column: 1 / -1;
  }

  .lab-steps-console__panel-body--editor,
  .lab-steps-console__panel-body--rail,
  .lab-steps-console__panel-body--steps {
    padding: 12px;
  }

  .lab-steps-console__panel-header {
    padding: 14px 16px;
  }

  .question-dialog__group {
    padding: 14px;
  }
}

@media (max-width: 560px) {
  .lab-steps-console,
  .lab-steps-console__workspace {
    overflow: visible;
    height: auto;
  }

  .lab-steps-console__header,
  .lab-steps-console__workspace,
  .question-dialog__group-title--row,
  .question-dialog__editor-toolbar {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .lab-steps-console__header-side {
    min-width: 0;
    align-items: stretch;
  }

  .question-dialog__grid--step-base,
  .lab-steps-console__check-grid {
    grid-template-columns: 1fr;
  }
}
</style>
