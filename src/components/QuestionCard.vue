<template>
  <article class="question-card">
    <div class="question-card__meta">
      <div class="question-card__badges">
        <AppBadge>{{ question.yearValue }}</AppBadge>
        <AppBadge :tone="difficultyTone">{{ question.difficultyLevel }}</AppBadge>
      </div>
      <span class="question-card__code">{{ question.archiveCode }}</span>
    </div>

    <div class="question-card__preview" role="button" tabindex="0" @click="$emit('preview', question)" @keydown.enter="$emit('preview', question)">
      <img :src="previewImage" :alt="question.archiveCode" />
      <div class="question-card__overlay">
        <AppButton variant="secondary" size="sm">放大查看</AppButton>
        <div class="question-card__actions">
          <button class="icon-chip" :class="{ 'icon-chip--active': question.favorited }" @click.stop="$emit('toggle-favorite', question)">
            <AppIcon name="bookmark" :size="15" />
          </button>
          <button class="icon-chip" :class="{ 'icon-chip--danger': question.mistake }" @click.stop="$emit('toggle-mistake', question)">
            <AppIcon name="alert" :size="15" />
          </button>
        </div>
      </div>
    </div>

    <div class="question-card__body">
      <h3>{{ question.subject }} / {{ question.questionType }}</h3>
      <MathContent class="question-card__stem" :content="question.stemText" empty-text="暂无题干" />
      <p>{{ question.chapterName }}</p>
      <span class="question-card__tags">{{ question.knowledgeTags }}</span>
      <div v-if="question.latestPracticeStatus" class="question-card__status">
        <span class="tag" :class="statusClass(question.latestPracticeStatus)">{{ statusLabel(question.latestPracticeStatus) }}</span>
        <span v-if="question.latestPracticedAt" class="question-card__status-time">{{ question.latestPracticedAt }}</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

import { buildPreviewSvg } from '../services/preview'
import AppBadge from './AppBadge.vue'
import AppButton from './AppButton.vue'
import AppIcon from './AppIcon.vue'
import MathContent from './MathContent.vue'

const props = defineProps({
  question: {
    type: Object,
    required: true
  }
})

defineEmits(['preview', 'toggle-favorite', 'toggle-mistake'])

const previewImage = computed(() => buildPreviewSvg(props.question))
const difficultyTone = computed(() => {
  if (props.question.difficultyLevel === '基础') return 'success'
  if (props.question.difficultyLevel === '强化') return 'info'
  return 'danger'
})

function statusLabel(status) {
  if (status === 'MASTERED') return '已掌握'
  if (status === 'REVIEW') return '待复习'
  if (status === 'MISTAKE') return '错题'
  return '已浏览'
}

function statusClass(status) {
  if (status === 'MASTERED') return 'tag--green'
  if (status === 'REVIEW') return 'tag--orange'
  if (status === 'MISTAKE') return 'tag--red'
  return 'tag--blue'
}
</script>

<style scoped>
.question-card__stem {
  margin: 10px 0 8px;
}

.question-card__status {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-top: 12px;
}

.question-card__status-time {
  color: var(--muted);
  font-size: 12px;
}
</style>
