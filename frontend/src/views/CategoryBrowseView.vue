<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <div class="page-breadcrumb">首页 / 分类浏览</div>
        <h1>分类浏览</h1>
        <p>按知识体系和题型分类浏览真题资源</p>
      </div>
    </div>

    <section class="category-layout">
      <aside class="table-panel category-tree">
        <div class="panel-head">
          <h3>知识体系分类</h3>
        </div>
        <div class="tree-list">
          <div v-for="subject in treeData" :key="subject.key" class="tree-subject">
            <button class="tree-subject__title" type="button" @click="selectSubject(subject)">
              <strong>{{ subject.label }}</strong>
              <span>{{ subject.count }}</span>
            </button>
            <div v-for="chapter in subject.children" :key="chapter.key" class="tree-chapter-group">
              <button
                class="tree-chapter"
                :class="{ 'tree-chapter--active': selectedChapter?.key === chapter.key && !selectedTopic }"
                type="button"
                @click="selectChapter(subject, chapter)"
              >
                <span>{{ chapter.label }}</span>
                <span>{{ chapter.count }}</span>
              </button>
              <button
                v-for="topic in chapter.children"
                :key="topic.key"
                class="tree-topic"
                :class="{ 'tree-topic--active': selectedTopic?.key === topic.key }"
                type="button"
                @click="selectTopic(subject, chapter, topic)"
              >
                <span>{{ topic.label }}</span>
                <span>{{ topic.count }}</span>
              </button>
            </div>
          </div>
        </div>
      </aside>

      <section class="table-panel">
        <div class="panel-head">
          <div>
            <h3>当前分类：{{ currentTitle }}</h3>
            <p>{{ currentDesc }}</p>
          </div>
        </div>

        <div class="filters-row filters-row--compact">
          <select v-model="filters.year" class="app-filter">
            <option value="">全部年份</option>
            <option v-for="item in yearOptions" :key="item" :value="item">{{ item }}</option>
          </select>
          <select v-model="filters.paperName" class="app-filter">
            <option value="">全部院校</option>
            <option v-for="item in paperOptions" :key="item" :value="item">{{ item }}</option>
          </select>
          <select v-model="filters.difficulty" class="app-filter">
            <option value="">全部难度</option>
            <option v-for="item in difficultyOptions" :key="item" :value="item">{{ item }}</option>
          </select>
          <select v-model="filters.questionType" class="app-filter">
            <option value="">全部题型</option>
            <option v-for="item in typeOptions" :key="item" :value="item">{{ item }}</option>
          </select>
          <input v-model="filters.keyword" class="app-filter" type="text" placeholder="搜索此分类下的真题..." />
        </div>

        <div class="filters-row filters-row--compact category-actions">
          <div></div><div></div><div></div><div></div>
          <button class="app-button app-button--primary" @click="loadQuestions(0)">搜索</button>
        </div>

        <div v-if="loading" class="empty-state">分类数据加载中...</div>
        <template v-else>
          <table class="simple-table">
            <thead>
              <tr>
                <th>年份</th>
                <th>真题题目</th>
                <th>数学类别</th>
                <th>难度</th>
                <th>题型</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in pageData.items" :key="item.id">
                <td>{{ item.yearValue }}</td>
                <td class="question-cell">
                  <button class="question-link" type="button" @click="openPractice(item)">
                    <MathContent :content="item.stemText" empty-text="暂无题干" />
                    <small>{{ item.chapterName }} / {{ item.knowledgeTags }}</small>
                  </button>
                </td>
                <td>{{ item.paperName }}</td>
                <td><span class="tag" :class="difficultyClass(item.difficultyLevel)">{{ item.difficultyLevel }}</span></td>
                <td>{{ item.questionType }}</td>
                <td class="category-ops">
                  <button class="text-link" @click="toggleFavorite(item)">{{ item.favorited ? '已收藏' : '收藏' }}</button>
                  <button class="text-link" @click="openPractice(item)">练习</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="pagination-bar">
            <span>共 {{ pageData.totalElements }} 条</span>
            <button class="app-button app-button--secondary" :disabled="pageData.page <= 0" @click="loadQuestions(pageData.page - 1)">上一页</button>
            <span>{{ pageData.page + 1 }} / {{ Math.max(pageData.totalPages, 1) }}</span>
            <button class="app-button app-button--secondary" :disabled="pageData.page + 1 >= pageData.totalPages" @click="loadQuestions(pageData.page + 1)">下一页</button>
          </div>
        </template>
      </section>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import MathContent from '../components/MathContent.vue'
import { api } from '../services/api'
import { sessionState } from '../stores/session'

const router = useRouter()
const treeData = ref([])
const selectedSubject = ref(null)
const selectedChapter = ref(null)
const selectedTopic = ref(null)
const pageData = ref({ items: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })
const loading = ref(false)

const yearOptions = [2025, 2024, 2023, 2022, 2021, 2020, 2019, 2018]
const paperOptions = ['数学一', '数学二', '数学三']
const difficultyOptions = ['基础', '强化', '冲刺', '中等', '偏难', '偏易']
const typeOptions = ['单项选择题', '填空题', '解答题', '证明题']

const filters = reactive({
  year: '',
  paperName: '',
  difficulty: '',
  questionType: '',
  keyword: ''
})

const currentTitle = computed(() => {
  if (selectedTopic.value) {
    return `${selectedSubject.value.label} > ${selectedChapter.value.label} > ${selectedTopic.value.label}`
  }
  if (selectedChapter.value) {
    return `${selectedSubject.value.label} > ${selectedChapter.value.label}`
  }
  return selectedSubject.value?.label || '全部分类'
})

const currentDesc = computed(() => {
  if (selectedTopic.value) {
    return `当前知识点共 ${selectedTopic.value.count} 道关联真题，可按年份、难度和题型继续筛选。`
  }
  if (selectedChapter.value) {
    return `当前分类共 ${selectedChapter.value.count} 道真题，可按年份、难度和题型继续筛选。`
  }
  if (selectedSubject.value) {
    return `当前学科共 ${selectedSubject.value.count} 道真题。`
  }
  return '请选择左侧分类节点浏览对应真题。'
})

onMounted(async () => {
  treeData.value = await api.getQuestionTaxonomy()
  if (treeData.value.length) {
    selectSubject(treeData.value[0])
    if (treeData.value[0].children.length) {
      selectChapter(treeData.value[0], treeData.value[0].children[0])
      return
    }
  }
  loadQuestions(0)
})

function selectSubject(subject) {
  selectedSubject.value = subject
  selectedChapter.value = null
  selectedTopic.value = null
  loadQuestions(0)
}

function selectChapter(subject, chapter) {
  selectedSubject.value = subject
  selectedChapter.value = chapter
  selectedTopic.value = null
  loadQuestions(0)
}

function selectTopic(subject, chapter, topic) {
  selectedSubject.value = subject
  selectedChapter.value = chapter
  selectedTopic.value = topic
  loadQuestions(0)
}

async function loadQuestions(page = 0) {
  loading.value = true
  try {
    pageData.value = await api.getQuestions({
      userId: sessionState.user.userId,
      subjects: selectedSubject.value ? [selectedSubject.value.label] : [],
      chapterNames: selectedChapter.value ? [selectedChapter.value.label] : [],
      knowledgeTags: selectedTopic.value ? [selectedTopic.value.label] : [],
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

function difficultyClass(level = '') {
  if (level.includes('易') || level === '基础') return 'tag--green'
  if (level.includes('难') || level === '冲刺') return 'tag--red'
  return 'tag--blue'
}
</script>

<style scoped>
.category-layout {
  display: grid;
  grid-template-columns: 290px minmax(0, 1fr);
  gap: 16px;
}

.category-tree {
  padding-bottom: 18px;
}

.tree-list {
  padding: 0 14px 12px;
}

.tree-subject + .tree-subject {
  margin-top: 14px;
}

.tree-subject__title,
.tree-chapter {
  width: 100%;
  border: 0;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  color: #334765;
}

.tree-subject__title {
  font-weight: 700;
}

.tree-chapter {
  margin-top: 6px;
  padding-left: 24px;
}

.tree-topic {
  width: 100%;
  border: 0;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
  padding: 8px 12px 8px 38px;
  border-radius: 10px;
  color: #5f7595;
  font-size: 13px;
}

.tree-chapter--active {
  background: #edf4ff;
  color: var(--primary);
}

.tree-topic--active {
  background: #f4f8ff;
  color: var(--primary);
}

.category-actions {
  padding-top: 0;
}

.category-ops {
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

.question-cell small {
  display: block;
  margin-top: 8px;
  color: var(--muted);
}

@media (max-width: 1200px) {
  .category-layout {
    grid-template-columns: 1fr;
  }
}
</style>
