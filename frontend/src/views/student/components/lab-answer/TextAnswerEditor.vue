<template>
  <el-input
    :model-value="currentValue"
    type="textarea"
    :rows="rows"
    :maxlength="maxlength"
    show-word-limit
    :disabled="disabled"
    :placeholder="placeholder"
    @update:model-value="updateValue"
  />
</template>

<script setup lang="ts">
import type { LabAnswerDraft } from './types';

const props = withDefaults(defineProps<{
  modelValue: LabAnswerDraft | null;
  disabled?: boolean;
  placeholder?: string;
  rows?: number;
  maxlength?: number;
}>(), {
  disabled: false,
  placeholder: '',
  rows: 8,
  maxlength: 5000,
});

const emit = defineEmits<{
  'update:modelValue': [value: LabAnswerDraft];
}>();

const currentValue = props.modelValue?.kind === 'text' ? props.modelValue.text : '';

const updateValue = (value: string | number) => {
  emit('update:modelValue', {
    kind: 'text',
    text: String(value ?? ''),
  });
};
</script>
