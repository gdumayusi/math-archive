<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <div class="page-breadcrumb">首页 / 真题练习 / {{ question?.archiveCode || '题目详情' }}</div>
        <h1>做题页面</h1>
        <p>先独立作答，再点击解析查看答案与讲解。</p>
      </div>
      <button class="app-button app-button--secondary" type="button" @click="goBack">返回</button>
    </div>

    <div v-if="loading" class="empty-state">题目加载中...</div>
    <div v-else-if="!question" class="empty-state">未找到题目内容</div>
    <template v-else>
      <section class="metric-grid practice-metrics">
        <article class="metric-card practice-metric">
          <div class="metric-card__label">题号</div>
          <div class="metric-card__value">{{ question.archiveCode }}</div>
          <div class="metric-card__sub">{{ question.paperName }} / {{ question.yearValue }}</div>
        </article>
        <article class="metric-card practice-metric">
          <div class="metric-card__label">分类</div>
          <div class="metric-card__value">{{ question.subject }}</div>
          <div class="metric-card__sub">{{ question.chapterName }}</div>
        </article>
        <article class="metric-card practice-metric">
          <div class="metric-card__label">题型</div>
          <div class="metric-card__value">{{ question.questionType }}</div>
          <div class="metric-card__sub">
            难度：{{ question.difficultyLevel }}
            <span v-if="question.latestPracticeStatus"> · 当前状态：{{ statusLabel(question.latestPracticeStatus) }}</span>
          </div>
        </article>
      </section>

      <section class="table-panel practice-panel">
        <div class="panel-head">
          <div>
            <h3>题目内容</h3>
            <p class="practice-tags">{{ question.knowledgeTags }}</p>
          </div>
        </div>

        <div class="practice-content">
          <MathContent class="practice-stem" :content="question.stemText" empty-text="暂无题干" />

          <div v-if="choiceOptions.length" class="choice-list">
            <button
              v-for="option in choiceOptions"
              :key="option.key"
              class="choice-item"
              :class="{ 'choice-item--active': selectedOption === option.key }"
              type="button"
              @click="selectedOption = option.key"
            >
              <span class="choice-item__key">{{ option.key }}</span>
              <MathContent :content="option.text" empty-text="暂无选项" />
            </button>
          </div>

          <div class="practice-actions">
            <button class="app-button app-button--primary" type="button" @click="toggleAnalysis">
              {{ showAnalysis ? '收起解析' : '查看解析' }}
            </button>
            <button class="app-button app-button--secondary" type="button" @click="toggleFavorite">
              {{ question.favorited ? '取消收藏' : '加入收藏' }}
            </button>
            <button class="app-button app-button--secondary" type="button" @click="toggleMistake">
              {{ question.mistake ? '移出错题' : '加入错题' }}
            </button>
            <button
              class="app-button"
              :class="question.latestPracticeStatus === 'REVIEW' ? 'app-button--primary' : 'app-button--secondary'"
              type="button"
              @click="markReview"
            >
              {{ question.latestPracticeStatus === 'REVIEW' ? '已记录待复习' : '记录待复习' }}
            </button>
            <button
              class="app-button"
              :class="question.latestPracticeStatus === 'MASTERED' ? 'app-button--primary' : 'app-button--secondary'"
              type="button"
              @click="markMastered"
            >
              {{ question.latestPracticeStatus === 'MASTERED' ? '已标记掌握' : '标记已掌握' }}
            </button>
          </div>
          <p v-if="question.latestPracticedAt" class="practice-status-tip">
            最近一次学习记录：{{ statusLabel(question.latestPracticeStatus) }} · {{ question.latestPracticedAt }}
          </p>
        </div>
      </section>

      <section v-if="showAnalysis" class="table-panel practice-panel">
        <div class="panel-head">
          <h3>答案与解析</h3>
        </div>
        <div class="practice-content">
          <div class="analysis-block">
            <strong>参考答案</strong>
            <MathContent :content="question.answerText" empty-text="暂无参考答案" />
          </div>
          <div class="analysis-block">
            <strong>题目解析</strong>
            <MathContent :content="question.analysisText" empty-text="暂无解析" />
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import MathContent from '../components/MathContent.vue'
import { api } from '../services/api'
import { sessionState } from '../stores/session'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const question = ref(null)
const selectedOption = ref('')
const showAnalysis = ref(false)

const choiceOptions = computed(() => {
  if (!question.value) {
    return []
  }
  return [
    { key: 'A', text: question.value.optionA },
    { key: 'B', text: question.value.optionB },
    { key: 'C', text: question.value.optionC },
    { key: 'D', text: question.value.optionD }
  ].filter((item) => item.text)
})

onMounted(loadQuestion)

async function loadQuestion() {
  loading.value = true
  try {
    question.value = await api.getQuestionDetail(route.params.id, sessionState.user.userId)
    await recordPractice('VIEWED')
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push({ name: 'questions' })
}

function toggleAnalysis() {
  showAnalysis.value = !showAnalysis.value
}

async function toggleFavorite() {
  if (!question.value) {
    return
  }
  if (question.value.favorited) {
    await api.removeFavorite(sessionState.user.userId, question.value.id)
  } else {
    await api.addFavorite(sessionState.user.userId, question.value.id)
  }
  question.value = { ...question.value, favorited: !question.value.favorited }
}

async function toggleMistake() {
  if (!question.value) {
    return
  }
  if (question.value.mistake) {
    await api.removeMistake(sessionState.user.userId, question.value.id)
    question.value = { ...question.value, mistake: false }
    return
  }

  await api.addMistake(sessionState.user.userId, question.value.id, '从做题页面加入错题本')
  question.value = { ...question.value, mistake: true }
  await recordPractice('MISTAKE')
}

async function markReview() {
  await recordPractice('REVIEW')
}

async function markMastered() {
  await recordPractice('MASTERED')
}

async function recordPractice(status) {
  if (!question.value) {
    return
  }
  try {
    await api.recordPractice(sessionState.user.userId, {
      questionId: question.value.id,
      status,
      sourcePage: 'QUESTION_PRACTICE'
    })
    if (status === 'VIEWED') {
      return
    }
    question.value = {
      ...question.value,
      latestPracticeStatus: status,
      latestPracticedAt: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
    }
  } catch {
    // Ignore record failures so the solving page remains usable.
  }
}

function statusLabel(status) {
  if (status === 'MASTERED') return '已掌握'
  if (status === 'REVIEW') return '待复习'
  if (status === 'MISTAKE') return '错题'
  return '已浏览'
}
</script>

<style scoped>
.practice-metrics {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.practice-metric {
  flex-direction: column;
  align-items: flex-start;
}

.practice-panel {
  padding-bottom: 8px;
}

.practice-tags {
  margin: 6px 0 0;
  color: var(--muted);
}

.practice-content {
  padding: 0 20px 20px;
}

.practice-stem {
  font-size: 16px;
  line-height: 1.9;
}

.choice-list {
  display: grid;
  gap: 12px;
  margin-top: 20px;
}

.choice-item {
  width: 100%;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  align-items: flex-start;
  border: 1px solid var(--line-strong);
  background: #fff;
  border-radius: 14px;
  padding: 14px 16px;
  text-align: left;
}

.choice-item--active {
  border-color: rgba(47, 109, 243, 0.5);
  background: var(--primary-soft);
  box-shadow: 0 0 0 4px rgba(47, 109, 243, 0.08);
}

.choice-item__key {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #edf4ff;
  color: var(--primary);
  font-weight: 700;
}

.practice-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
}

.practice-status-tip {
  margin: 14px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.analysis-block + .analysis-block {
  margin-top: 20px;
}

.analysis-block strong {
  display: block;
  margin-bottom: 10px;
  font-size: 15px;
}

@media (max-width: 1200px) {
  .practice-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
