<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <div class="page-breadcrumb">首页 / 真题管理</div>
        <h1>真题管理</h1>
        <p>管理和维护考研数学真题资源</p>
      </div>
    </div>

    <section class="metric-grid">
      <article v-for="card in summaryCards" :key="card.label" class="metric-card">
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

    <section class="table-panel">
      <div class="filters-row">
        <select v-model="filters.year" class="app-filter">
          <option value="">全部年份</option>
          <option v-for="item in yearOptions" :key="item" :value="item">{{ item }}</option>
        </select>
        <select v-model="filters.paperName" class="app-filter">
          <option value="">全部数学类别</option>
          <option v-for="item in paperOptions" :key="item" :value="item">{{ item }}</option>
        </select>
        <select v-model="filters.questionType" class="app-filter">
          <option value="">全部题型</option>
          <option v-for="item in typeOptions" :key="item" :value="item">{{ item }}</option>
        </select>
        <select v-model="filters.difficulty" class="app-filter">
          <option value="">全部难度</option>
          <option v-for="item in difficultyOptions" :key="item" :value="item">{{ item }}</option>
        </select>
        <input v-model="filters.keyword" class="app-filter" type="text" placeholder="输入标题或知识点搜索" />
        <div class="table-actions">
          <button class="app-button app-button--primary" @click="loadData(0)">搜索</button>
          <button class="app-button app-button--secondary" @click="resetFilters">重置</button>
        </div>
      </div>

      <div v-if="loading" class="empty-state">真题数据加载中...</div>
      <template v-else>
        <table class="simple-table">
          <thead>
            <tr>
              <th>年份</th>
              <th>数学类别</th>
              <th>所属科目</th>
              <th>题目</th>
              <th>题型</th>
              <th>难度</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in pageData.items" :key="item.id">
              <td>{{ item.yearValue }}</td>
              <td>{{ item.paperName }}</td>
              <td>{{ item.subject }}</td>
              <td class="question-cell">
                <button class="question-link" type="button" @click="openPractice(item)">
                  <MathContent :content="item.stemText" empty-text="暂无题干" />
                  <small>{{ item.chapterName }} / {{ item.knowledgeTags }}</small>
                </button>
              </td>
              <td>{{ item.questionType }}</td>
              <td><span class="tag" :class="difficultyClass(item.difficultyLevel)">{{ item.difficultyLevel }}</span></td>
              <td>
                <div class="table-actions">
                  <button class="text-link" @click="openPractice(item)">做题</button>
                  <button class="text-link" @click="toggleFavorite(item)">{{ item.favorited ? '取消收藏' : '收藏' }}</button>
                  <button class="text-link" @click="toggleMistake(item)">{{ item.mistake ? '移出错题' : '加入错题' }}</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="pagination-bar">
          <span>共 {{ pageData.totalElements }} 条</span>
          <button class="app-button app-button--secondary" :disabled="pageData.page <= 0" @click="loadData(pageData.page - 1)">上一页</button>
          <span>{{ pageData.page + 1 }} / {{ Math.max(pageData.totalPages, 1) }}</span>
          <button class="app-button app-button--secondary" :disabled="pageData.page + 1 >= pageData.totalPages" @click="loadData(pageData.page + 1)">下一页</button>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AppIcon from '../components/AppIcon.vue'
import MathContent from '../components/MathContent.vue'
import { api } from '../services/api'
import { sessionState } from '../stores/session'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const pageData = ref({ items: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const yearOptions = [2025, 2024, 2023, 2022, 2021, 2020, 2019, 2018]
const paperOptions = ['数学一', '数学二', '数学三']
const typeOptions = ['单项选择题', '填空题', '解答题', '证明题']
const difficultyOptions = ['基础', '强化', '冲刺', '中等', '偏难', '偏易']

const filters = reactive({
  keyword: '',
  year: '',
  paperName: '',
  questionType: '',
  difficulty: ''
})

const summaryCards = computed(() => [
  { label: '真题总数', value: pageData.value.totalElements, sub: '系统收录真题数量', icon: 'fileText', iconClass: 'metric-card__icon--blue' },
  { label: '数学分类', value: paperOptions.length, sub: '数学一、二、三分类', icon: 'folder', iconClass: 'metric-card__icon--green' },
  { label: '知识点标签', value: new Set(pageData.value.items.flatMap((item) => (item.knowledgeTags || '').split(/[，,]/).map((tag) => tag.trim()).filter(Boolean))).size, sub: '当前页知识标签数', icon: 'tag', iconClass: 'metric-card__icon--violet' },
  { label: '我的错题', value: pageData.value.items.filter((item) => item.mistake).length, sub: '当前页错题数量', icon: 'alert', iconClass: 'metric-card__icon--orange' },
  { label: '我的收藏', value: pageData.value.items.filter((item) => item.favorited).length, sub: '当前页收藏数量', icon: 'star', iconClass: 'metric-card__icon--blue' }
])

onMounted(() => {
  filters.keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  loadData(0)
})

async function loadData(page = 0) {
  loading.value = true
  try {
    pageData.value = await api.getQuestions({
      userId: sessionState.user.userId,
      keyword: filters.keyword,
      years: filters.year ? [filters.year] : [],
      paperNames: filters.paperName ? [filters.paperName] : [],
      questionTypes: filters.questionType ? [filters.questionType] : [],
      difficulties: filters.difficulty ? [filters.difficulty] : [],
      page,
      size: pageData.value.size
    })
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.year = ''
  filters.paperName = ''
  filters.questionType = ''
  filters.difficulty = ''
  loadData(0)
}

function openPractice(item) {
  router.push({ name: 'question-practice', params: { id: item.id } })
}

async function toggleFavorite(item) {
  if (item.favorited) {
    await api.removeFavorite(sessionState.user.userId, item.id)
  } else {
    await api.addFavorite(sessionState.user.userId, item.id)
  }
  item.favorited = !item.favorited
}

async function toggleMistake(item) {
  if (item.mistake) {
    await api.removeMistake(sessionState.user.userId, item.id)
  } else {
    await api.addMistake(sessionState.user.userId, item.id, '从真题管理页加入错题本')
  }
  item.mistake = !item.mistake
}

function difficultyClass(level = '') {
  if (level.includes('易') || level === '基础') return 'tag--green'
  if (level.includes('难') || level === '冲刺') return 'tag--red'
  return 'tag--blue'
}
</script>

<style scoped>
.table-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.text-link {
  border: 0;
  background: transparent;
  color: var(--primary);
  font-weight: 600;
}

.question-cell {
  min-width: 320px;
}

.question-link {
  width: 100%;
  border: 0;
  background: transparent;
  padding: 0;
  text-align: left;
  color: inherit;
}

.question-link:hover {
  color: var(--primary);
}

.question-link small {
  display: block;
  margin-top: 8px;
  color: var(--muted);
}
</style>
