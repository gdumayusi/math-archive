<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <div class="page-breadcrumb">首页 / 错题本</div>
        <h1>错题本</h1>
        <p>错题整理与复习巩固</p>
      </div>
    </div>

    <section class="metric-grid">
      <article v-for="card in cards" :key="card.label" class="metric-card">
        <div class="metric-card__icon" :class="card.iconClass">
          <AppIcon :name="card.icon" :size="24" />
        </div>
        <div>
          <div class="metric-card__label">{{ card.label }}</div>
          <div class="metric-card__value">{{ card.value }}</div>
          <div class="metric-card__sub">{{ card.sub }}</div>
        </div>
      </article>
    </section>

    <section class="mistake-charts">
      <article class="table-panel list-card">
        <div class="panel-head"><h3>错题趋势</h3></div>
        <div class="line-bars">
          <div v-for="item in timeline" :key="item.date" class="line-bars__item">
            <div class="line-bars__bar" :style="{ height: `${Math.max(item.height, 12)}%` }"></div>
            <span>{{ item.date.slice(5) }}</span>
          </div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head"><h3>错题分布（按知识点）</h3></div>
        <div class="rank-list">
          <div v-for="item in weakPoints" :key="item.knowledgeTags" class="rank-list__item">
            <div class="rank-list__body">
              <strong>{{ item.chapterName }}</strong>
              <p>{{ item.knowledgeTags }}</p>
            </div>
            <span>{{ item.mistakeCount }} 题</span>
          </div>
        </div>
      </article>
    </section>

    <section class="table-panel">
      <div class="panel-head">
        <h3>错题列表</h3>
      </div>
      <div class="filters-row filters-row--compact">
        <input v-model="keyword" class="app-filter" type="text" placeholder="搜索题目、知识点..." />
        <div></div><div></div><div></div>
        <button class="app-button app-button--primary">搜索</button>
      </div>
      <table class="simple-table">
        <thead>
          <tr>
            <th>题目</th>
            <th>知识点</th>
            <th>来源 / 年份</th>
            <th>题型</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in visibleQuestions" :key="item.id">
            <td class="question-cell">
              <MathContent :content="item.stemText" empty-text="暂无题干" />
            </td>
            <td>{{ item.knowledgeTags }}</td>
            <td>{{ item.paperName }} / {{ item.yearValue }}</td>
            <td>{{ item.questionType }}</td>
            <td><span class="tag" :class="statusClass(item.latestPracticeStatus || 'MISTAKE')">{{ statusLabel(item.latestPracticeStatus || 'MISTAKE') }}</span></td>
            <td class="ops">
              <button class="text-link" @click="removeMistake(item)">移除</button>
              <button class="text-link" @click="markReviewed(item)">{{ item.latestPracticeStatus === 'MASTERED' ? '再次复习' : '复习' }}</button>
              <button class="text-link" @click="markMastered(item)">{{ item.latestPracticeStatus === 'MASTERED' ? '已掌握' : '标记掌握' }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppIcon from '../components/AppIcon.vue'
import MathContent from '../components/MathContent.vue'
import { api } from '../services/api'
import { sessionState } from '../stores/session'

const questions = ref([])
const stats = ref(null)
const keyword = ref('')
const router = useRouter()

const pendingCount = computed(() => questions.value.filter((item) => ['REVIEW', 'MISTAKE', null, undefined, 'VIEWED'].includes(item.latestPracticeStatus)).length)
const masteredCount = computed(() => questions.value.filter((item) => item.latestPracticeStatus === 'MASTERED').length)

const cards = computed(() => [
  { label: '错题总数', value: questions.value.length, sub: '待复习错题数量', icon: 'alert', iconClass: 'metric-card__icon--orange' },
  { label: '涉及知识点', value: new Set(questions.value.map((item) => item.chapterName)).size, sub: '错题覆盖章节数', icon: 'tag', iconClass: 'metric-card__icon--blue' },
  { label: '正确率', value: `${stats.value?.accuracyRate || 0}%`, sub: '根据练习记录统计', icon: 'barChart', iconClass: 'metric-card__icon--violet' },
  { label: '已掌握错题', value: masteredCount.value, sub: '当前已完成掌握', icon: 'calendar', iconClass: 'metric-card__icon--green' },
  { label: '待复习错题', value: pendingCount.value, sub: '建议优先处理', icon: 'clock', iconClass: 'metric-card__icon--red' }
])

const timeline = computed(() => {
  const raw = stats.value?.timeline || []
  const max = Math.max(...raw.map((item) => item.mistakeCount), 1)
  return raw.map((item) => ({ ...item, height: Math.round((item.mistakeCount / max) * 100) }))
})

const weakPoints = computed(() => (stats.value?.weakPoints || []).slice(0, 5))
const visibleQuestions = computed(() => questions.value.filter((item) => `${item.stemText}${item.knowledgeTags}`.includes(keyword.value)))

onMounted(async () => {
  await loadData()
})

async function loadData() {
  const userId = sessionState.user.userId
  const [mistakeData, statData] = await Promise.all([
    api.getMistakes(userId),
    api.getStudyStats(userId)
  ])
  questions.value = mistakeData
  stats.value = statData
}

async function removeMistake(item) {
  await api.removeMistake(sessionState.user.userId, item.id)
  questions.value = questions.value.filter((row) => row.id !== item.id)
  await loadStatsOnly()
}

async function markReviewed(item) {
  await api.recordPractice(sessionState.user.userId, {
    questionId: item.id,
    status: 'REVIEW',
    sourcePage: 'MISTAKES'
  })
  patchQuestionState(item.id, 'REVIEW')
  await loadStatsOnly()
  router.push({ name: 'question-practice', params: { id: item.id } })
}

async function markMastered(item) {
  await api.recordPractice(sessionState.user.userId, {
    questionId: item.id,
    status: 'MASTERED',
    sourcePage: 'MISTAKES'
  })
  patchQuestionState(item.id, 'MASTERED')
  await loadStatsOnly()
}

async function loadStatsOnly() {
  stats.value = await api.getStudyStats(sessionState.user.userId)
}

function patchQuestionState(questionId, status) {
  questions.value = questions.value.map((item) => (
    item.id === questionId
      ? {
          ...item,
          latestPracticeStatus: status,
          latestPracticedAt: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        }
      : item
  ))
}

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
.mistake-charts {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 16px;
}

.line-bars {
  min-height: 220px;
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 14px;
  align-items: end;
}

.line-bars__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.line-bars__bar {
  width: 100%;
  border-radius: 12px 12px 0 0;
  background: linear-gradient(180deg, #6fa6ff, #2f6df3);
}

.ops {
  display: flex;
  gap: 10px;
}

.text-link {
  border: 0;
  background: transparent;
  color: var(--primary);
  font-weight: 600;
}

.question-cell {
  min-width: 360px;
}

@media (max-width: 1200px) {
  .mistake-charts {
    grid-template-columns: 1fr;
  }
}
</style>
