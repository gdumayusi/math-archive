<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <div class="page-breadcrumb">首页 / 收藏题库</div>
        <h1>收藏题库</h1>
        <p>收藏的重要题目，方便随时复习</p>
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

    <section class="table-panel">
      <div class="panel-head">
        <h3>已收藏题目（{{ visibleQuestions.length }}）</h3>
      </div>

      <div class="filters-row filters-row--compact">
        <select v-model="filters.paperName" class="app-filter">
          <option value="">全部试卷</option>
          <option v-for="item in paperOptions" :key="item" :value="item">{{ item }}</option>
        </select>
        <input v-model="filters.keyword" class="app-filter" type="text" placeholder="搜索题目、章节或知识标签..." />
        <div></div>
        <button class="app-button app-button--secondary" @click="resetFilters">重置</button>
      </div>

      <table class="simple-table">
        <thead>
          <tr>
            <th>题目</th>
            <th>知识标签</th>
            <th>来源 / 年份</th>
            <th>题型</th>
            <th>收藏时间</th>
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
            <td>{{ item.createdDate }}</td>
            <td class="ops">
              <button class="text-link" @click="removeQuestion(item)">取消收藏</button>
              <button class="text-link" @click="addToMistake(item)">加入错题</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import AppIcon from '../components/AppIcon.vue'
import MathContent from '../components/MathContent.vue'
import { api } from '../services/api'
import { sessionState } from '../stores/session'

const questions = ref([])

const paperOptions = ['数学一', '数学二', '数学三']
const filters = reactive({
  paperName: '',
  keyword: ''
})

const cards = computed(() => [
  { label: '收藏总数', value: questions.value.length, sub: '当前题目收藏数量', icon: 'star', iconClass: 'metric-card__icon--blue' },
  { label: '覆盖试卷', value: new Set(questions.value.map((item) => item.paperName)).size, sub: '不同试卷来源', icon: 'fileText', iconClass: 'metric-card__icon--orange' },
  { label: '覆盖年份', value: new Set(questions.value.map((item) => item.yearValue)).size, sub: '已收藏真题年份', icon: 'calendar', iconClass: 'metric-card__icon--green' },
  { label: '最近收藏', value: questions.value[0]?.createdDate || '--', sub: questions.value[0]?.knowledgeTags || '暂无收藏记录', icon: 'clock', iconClass: 'metric-card__icon--violet' }
])

const visibleQuestions = computed(() => {
  let list = questions.value
  if (filters.paperName) {
    list = list.filter((item) => item.paperName === filters.paperName)
  }
  if (filters.keyword) {
    list = list.filter((item) => `${item.stemText}${item.knowledgeTags}${item.chapterName}`.includes(filters.keyword))
  }
  return list
})

onMounted(async () => {
  questions.value = await api.getFavorites(sessionState.user.userId)
})

function resetFilters() {
  filters.paperName = ''
  filters.keyword = ''
}

async function removeQuestion(item) {
  await api.removeFavorite(sessionState.user.userId, item.id)
  questions.value = questions.value.filter((row) => row.id !== item.id)
}

async function addToMistake(item) {
  await api.addMistake(sessionState.user.userId, item.id, '从收藏夹加入错题本')
}
</script>

<style scoped>
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
</style>
