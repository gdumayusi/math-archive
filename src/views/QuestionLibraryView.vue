<template>
  <div class="library-layout">
    <aside class="filter-panel">
      <div class="filter-panel__header">
        <span><AppIcon name="filter" :size="14" /> 维度筛选</span>
        <button @click="resetFilters">重置</button>
      </div>

      <div class="filter-section">
        <h4>所属科目</h4>
        <label v-for="item in subjectOptions" :key="item">
          <input v-model="filters.subjects" :value="item" type="checkbox" />
          <span>{{ item }}</span>
        </label>
      </div>

      <div class="filter-section">
        <h4>试卷类别</h4>
        <label v-for="item in paperOptions" :key="item.value">
          <input v-model="filters.paperNames" :value="item.value" type="checkbox" />
          <span>{{ item.label }}</span>
        </label>
      </div>

      <div class="filter-section">
        <h4>考卷年份</h4>
        <label v-for="item in yearOptions" :key="item">
          <input v-model="filters.years" :value="item" type="checkbox" />
          <span>{{ item }}</span>
        </label>
      </div>

      <div class="filter-section">
        <h4>题目类型</h4>
        <label v-for="item in typeOptions" :key="item">
          <input v-model="filters.questionTypes" :value="item" type="checkbox" />
          <span>{{ item }}</span>
        </label>
      </div>

      <div class="filter-section">
        <h4>难度评级</h4>
        <label v-for="item in difficultyOptions" :key="item">
          <input v-model="filters.difficulties" :value="item" type="checkbox" />
          <span>{{ item }}</span>
        </label>
      </div>
    </aside>

    <section class="library-main">
      <div class="toolbar">
        <p>当前条件检索到 <strong>{{ pageData.totalElements }}</strong> 条真题记录</p>
        <div class="toolbar__actions">
          <AppInput v-model="filters.keyword" placeholder="输入题号、知识点或卷别搜索" />
          <select v-model="filters.sort" class="app-select">
            <option value="latest">默认排序（按年份降序）</option>
            <option value="archiveCode">按题号升序</option>
            <option value="difficulty">按难度升序</option>
          </select>
          <AppButton variant="secondary" @click="fetchQuestions(0)">筛选</AppButton>
        </div>
      </div>

      <div v-if="loading" class="empty-state">题库加载中...</div>
      <div v-else-if="!pageData.items.length" class="empty-state">没有匹配到题目，试试放宽筛选条件。</div>
      <div v-else class="question-grid">
        <QuestionCard
          v-for="question in pageData.items"
          :key="question.id"
          :question="question"
          @preview="openPreview"
          @toggle-favorite="toggleFavorite"
          @toggle-mistake="toggleMistake"
        />
      </div>

      <div class="pagination">
        <AppButton variant="secondary" :disabled="pageData.page <= 0" @click="fetchQuestions(pageData.page - 1)">上一页</AppButton>
        <span>第 {{ pageData.page + 1 }} / {{ Math.max(pageData.totalPages, 1) }} 页</span>
        <AppButton variant="secondary" :disabled="pageData.page + 1 >= pageData.totalPages" @click="fetchQuestions(pageData.page + 1)">下一页</AppButton>
      </div>
    </section>

    <ScanPreviewModal
      :open="previewOpen"
      :question="previewQuestion"
      :has-prev="previewIndex > 0"
      :has-next="previewIndex < pageData.items.length - 1"
      @close="previewOpen = false"
      @prev="switchPreview(previewIndex - 1)"
      @next="switchPreview(previewIndex + 1)"
      @toggle-favorite="toggleFavorite"
      @toggle-mistake="toggleMistake"
      @record-mastered="recordMastered"
      @record-review="recordReview"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import AppButton from '../components/AppButton.vue'
import AppIcon from '../components/AppIcon.vue'
import AppInput from '../components/AppInput.vue'
import QuestionCard from '../components/QuestionCard.vue'
import ScanPreviewModal from '../components/ScanPreviewModal.vue'
import { api } from '../services/api'
import { removeFavoriteCache, removeMistakeCache, upsertFavoriteCache, upsertMistakeCache } from '../stores/library'
import { sessionState } from '../stores/session'

const route = useRoute()

const subjectOptions = ['高等数学', '线性代数', '概率论与数理统计']
const paperOptions = [
  { label: '数一', value: '数学一' },
  { label: '数二', value: '数学二' },
  { label: '数三', value: '数学三' }
]
const yearOptions = [2025, 2024, 2023, 2022, 2021, 2020, 2019, 2018]
const typeOptions = ['单项选择题', '填空题', '解答题', '证明题']
const difficultyOptions = ['基础', '强化', '冲刺']

const filters = reactive({
  keyword: '',
  subjects: [],
  paperNames: [],
  years: [],
  questionTypes: [],
  difficulties: [],
  sort: 'latest'
})

const loading = ref(false)
const pageData = ref({
  items: [],
  totalElements: 0,
  totalPages: 0,
  page: 0,
  size: 9
})

const previewOpen = ref(false)
const previewIndex = ref(-1)
const previewQuestion = ref(null)

watch(
  () => route.query.keyword,
  (value) => {
    filters.keyword = typeof value === 'string' ? value : ''
    fetchQuestions(0)
  }
)

onMounted(() => {
  filters.keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  fetchQuestions(0)
})

async function fetchQuestions(page = 0) {
  loading.value = true
  try {
    pageData.value = await api.getQuestions({
      userId: sessionState.user.userId,
      keyword: filters.keyword,
      subjects: filters.subjects,
      paperNames: filters.paperNames,
      years: filters.years,
      questionTypes: filters.questionTypes,
      difficulties: filters.difficulties,
      page,
      size: pageData.value.size,
      sort: filters.sort
    })
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.subjects = []
  filters.paperNames = []
  filters.years = []
  filters.questionTypes = []
  filters.difficulties = []
  filters.sort = 'latest'
  fetchQuestions(0)
}

async function openPreview(question) {
  previewIndex.value = pageData.value.items.findIndex((item) => item.id === question.id)
  previewQuestion.value = await api.getQuestionDetail(question.id, sessionState.user.userId)
  previewOpen.value = true
  await recordPractice(question.id, 'VIEWED', 'LIBRARY')
}

async function switchPreview(nextIndex) {
  if (nextIndex < 0 || nextIndex >= pageData.value.items.length) {
    return
  }
  previewIndex.value = nextIndex
  const nextQuestion = pageData.value.items[nextIndex]
  previewQuestion.value = await api.getQuestionDetail(nextQuestion.id, sessionState.user.userId)
  await recordPractice(nextQuestion.id, 'VIEWED', 'LIBRARY')
}

async function toggleFavorite(question) {
  const favorited = previewQuestion.value?.id === question.id ? previewQuestion.value.favorited : question.favorited
  if (favorited) {
    await api.removeFavorite(sessionState.user.userId, question.id)
    patchQuestionState(question.id, { favorited: false })
    removeFavoriteCache(sessionState.user.userId, question.id)
  } else {
    await api.addFavorite(sessionState.user.userId, question.id)
    patchQuestionState(question.id, { favorited: true })
    const source = previewQuestion.value?.id === question.id
      ? { ...previewQuestion.value, favorited: true }
      : { ...question, favorited: true }
    upsertFavoriteCache(sessionState.user.userId, source)
  }
}

async function toggleMistake(question) {
  const mistake = previewQuestion.value?.id === question.id ? previewQuestion.value.mistake : question.mistake
  if (mistake) {
    await api.removeMistake(sessionState.user.userId, question.id)
    patchQuestionState(question.id, { mistake: false })
    removeMistakeCache(sessionState.user.userId, question.id)
  } else {
    await api.addMistake(sessionState.user.userId, question.id, '由题库检索页加入错题本')
    await recordPractice(question.id, 'MISTAKE', 'LIBRARY')
    patchQuestionState(question.id, { mistake: true })
    const source = previewQuestion.value?.id === question.id
      ? { ...previewQuestion.value, mistake: true }
      : { ...question, mistake: true }
    upsertMistakeCache(sessionState.user.userId, source)
  }
}

async function recordMastered(question) {
  await recordPractice(question.id, 'MASTERED', 'LIBRARY')
}

async function recordReview(question) {
  await recordPractice(question.id, 'REVIEW', 'LIBRARY')
}

async function recordPractice(questionId, status, sourcePage) {
  try {
    await api.recordPractice(sessionState.user.userId, { questionId, status, sourcePage })
    if (status === 'VIEWED') {
      return
    }
    patchQuestionState(questionId, {
      latestPracticeStatus: status,
      latestPracticedAt: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
    })
  } catch {
    // Ignore transient record errors to avoid blocking the main flow.
  }
}

function patchQuestionState(questionId, patch) {
  pageData.value.items = pageData.value.items.map((item) => (item.id === questionId ? { ...item, ...patch } : item))
  if (previewQuestion.value?.id === questionId) {
    previewQuestion.value = { ...previewQuestion.value, ...patch }
  }
}
</script>
