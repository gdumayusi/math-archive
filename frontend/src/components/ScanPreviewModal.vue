<template>
  <Teleport to="body">
    <div v-if="open && question" class="preview-modal">
      <div class="preview-modal__scrim" @click="$emit('close')" />
      <div class="preview-modal__panel">
        <div class="preview-modal__toolbar">
          <div>
            <div class="preview-modal__title">原始档案视图</div>
            <div class="preview-modal__meta">{{ question.archiveCode }} / {{ question.paperName }}</div>
          </div>
          <div class="preview-modal__tools">
            <button class="icon-chip" @click="zoom = Math.max(0.75, zoom - 0.25)">
              <AppIcon name="zoomOut" :size="16" />
            </button>
            <span class="preview-modal__scale">{{ Math.round(zoom * 100) }}%</span>
            <button class="icon-chip" @click="zoom = Math.min(2.5, zoom + 0.25)">
              <AppIcon name="zoomIn" :size="16" />
            </button>
            <button class="icon-chip" @click="$emit('close')">
              <AppIcon name="close" :size="16" />
            </button>
          </div>
        </div>

        <div class="preview-modal__content">
          <button v-if="hasPrev" class="preview-modal__nav preview-modal__nav--left" @click="$emit('prev')">
            <AppIcon name="chevronLeft" :size="22" />
          </button>

          <div class="preview-modal__image-shell">
            <img :src="previewImage" :alt="question.archiveCode" :style="{ transform: `scale(${zoom})` }" />
          </div>

          <button v-if="hasNext" class="preview-modal__nav preview-modal__nav--right" @click="$emit('next')">
            <AppIcon name="chevronRight" :size="22" />
          </button>
        </div>

        <div class="preview-modal__footer">
          <div class="preview-modal__detail">
            <h4>{{ question.subject }} / {{ question.questionType }}</h4>
            <p class="preview-modal__tags">{{ question.chapterName }} / {{ question.knowledgeTags }}</p>
            <p v-if="question.latestPracticeStatus" class="preview-modal__status">
              当前状态：{{ statusLabel(question.latestPracticeStatus) }}
              <span v-if="question.latestPracticedAt"> · {{ question.latestPracticedAt }}</span>
            </p>
            <MathContent :content="question.stemText" empty-text="暂无题干内容" />
            <div v-if="question.answerText" class="preview-modal__math-block">
              <strong>参考答案：</strong>
              <MathContent :content="question.answerText" empty-text="暂无参考答案" />
            </div>
            <div v-if="question.analysisText" class="preview-modal__math-block">
              <strong>解析：</strong>
              <MathContent :content="question.analysisText" empty-text="暂无解析" />
            </div>
          </div>
          <div class="preview-modal__footer-actions">
            <AppButton :variant="question.latestPracticeStatus === 'REVIEW' ? 'primary' : 'secondary'" @click="$emit('record-review', question)">
              <span>{{ question.latestPracticeStatus === 'REVIEW' ? '已记录待复习' : '记录待复习' }}</span>
            </AppButton>
            <AppButton :variant="question.latestPracticeStatus === 'MASTERED' ? 'primary' : 'secondary'" @click="$emit('record-mastered', question)">
              <span>{{ question.latestPracticeStatus === 'MASTERED' ? '已标记掌握' : '记录已掌握' }}</span>
            </AppButton>
            <AppButton variant="secondary" @click="$emit('toggle-favorite', question)">
              <AppIcon name="bookmark" :size="14" />
              <span>{{ question.favorited ? '取消收藏' : '加入收藏' }}</span>
            </AppButton>
            <AppButton variant="danger" @click="$emit('toggle-mistake', question)">
              <AppIcon name="alert" :size="14" />
              <span>{{ question.mistake ? '移出错题' : '登记错题' }}</span>
            </AppButton>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

import { buildPreviewSvg } from '../services/preview'
import AppButton from './AppButton.vue'
import AppIcon from './AppIcon.vue'
import MathContent from './MathContent.vue'

const props = defineProps({
  open: Boolean,
  question: Object,
  hasPrev: Boolean,
  hasNext: Boolean
})

defineEmits(['close', 'prev', 'next', 'toggle-favorite', 'toggle-mistake', 'record-mastered', 'record-review'])

const zoom = ref(1)
const previewImage = computed(() => buildPreviewSvg(props.question))

function statusLabel(status) {
  if (status === 'MASTERED') return '已掌握'
  if (status === 'REVIEW') return '待复习'
  if (status === 'MISTAKE') return '错题'
  return '已浏览'
}

watch(
  () => props.open,
  (value) => {
    if (value) {
      zoom.value = 1
    }
  }
)
</script>

<style scoped>
.preview-modal__math-block {
  margin-top: 14px;
}

.preview-modal__math-block strong {
  display: inline-block;
  margin-bottom: 8px;
}

.preview-modal__status {
  margin: 8px 0 12px;
  color: var(--muted);
}
</style>
