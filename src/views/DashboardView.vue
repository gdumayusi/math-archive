<template>
  <div class="page-stack">
    <section class="hero-panel dashboard-hero">
      <div class="dashboard-hero__content">
        <h2>科学分类 · 高效学习 · 备考无忧</h2>
        <p>系统化管理考研数学真题，助力高效复习上岸</p>
        <div class="dashboard-search">
          <input v-model="keyword" class="app-filter" type="text" placeholder="搜索真题、题目或知识标签..." @keydown.enter="goSearch" />
          <button class="app-button app-button--primary" @click="goSearch">搜索</button>
        </div>
        <div class="dashboard-tags">
          <span>热门搜索：</span>
          <button v-for="item in hotSearches" :key="item" type="button" class="tag tag--blue" @click="quickSearch(item)">{{ item }}</button>
        </div>
      </div>
      <div class="dashboard-hero__art">
        <HeroIllustration variant="dashboard" />
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="card in cards" :key="card.label" class="metric-card">
        <div class="metric-card__icon" :class="card.iconClass">
          <AppIcon :name="card.icon" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ card.value }}</div>
          <div class="metric-card__label">{{ card.label }}</div>
          <div class="metric-card__sub">{{ card.sub }}</div>
        </div>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="table-panel">
        <div class="panel-head">
          <h3>真题按年份</h3>
        </div>
        <div class="bar-chart">
          <div v-for="item in yearBars" :key="item.year" class="bar-chart__item">
            <div class="bar-chart__column">
              <div class="bar-chart__fill" :style="{ height: `${item.height}%` }"></div>
            </div>
            <strong>{{ item.year }}</strong>
            <span>{{ item.count }}</span>
          </div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head">
          <h3>热门章节</h3>
          <button class="panel-head__link" type="button" @click="router.push({ name: 'categories' })">查看全部</button>
        </div>
        <div class="rank-list">
          <div v-for="(item, index) in hotChapters" :key="`${item.subject}-${item.label}`" class="rank-list__item">
            <span class="rank-list__index">{{ index + 1 }}</span>
            <div class="rank-list__body">
              <strong>{{ item.label }}</strong>
              <p>{{ item.subject }}</p>
            </div>
            <span>{{ item.count }} 题</span>
          </div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head">
          <h3>最近练习</h3>
          <button class="panel-head__link" type="button" @click="router.push({ name: 'study-stats' })">查看全部</button>
        </div>
        <div class="recent-list">
          <div v-for="item in recentRecords" :key="`${item.questionId}-${item.practicedAt}`" class="recent-list__item">
            <div>
              <strong>{{ item.archiveCode }}</strong>
              <p>{{ item.subject }} · {{ item.chapterName }}</p>
            </div>
            <div class="recent-list__right">
              <span class="tag" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
              <small>{{ item.practicedAt }}</small>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="table-panel dashboard-plan">
      <div class="panel-head">
        <h3>学习建议</h3>
      </div>
      <div class="plan-grid">
        <article class="plan-card">
          <strong>制定学习计划</strong>
          <p>根据分类结果和薄弱章节，优先安排高频模块复习。</p>
        </article>
        <article class="plan-card">
          <strong>分类突破</strong>
          <p>结合分类浏览页面，按数学一、数学二、数学三快速定位题组。</p>
        </article>
        <article class="plan-card">
          <strong>错题复盘</strong>
          <p>围绕错题本中的待复习记录进行针对性回看，减少重复失分。</p>
        </article>
        <article class="plan-card">
          <strong>模拟测试</strong>
          <p>通过真题管理页筛选近年整套试卷，按年份完成阶段性检测。</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppIcon from '../components/AppIcon.vue'
import HeroIllustration from '../components/HeroIllustration.vue'
import { api } from '../services/api'
import { sessionState } from '../stores/session'

const router = useRouter()
const keyword = ref('')
const questionPage = ref({ items: [], totalElements: 0 })
const taxonomy = ref([])
const studyStats = ref(null)
const favorites = ref([])
const mistakes = ref([])

const flatChapters = computed(() => (
  taxonomy.value.flatMap((subject) =>
    subject.children.map((chapter) => ({
      subject: subject.label,
      label: chapter.label,
      count: chapter.count
    }))
  )
))

const hotSearches = computed(() => flatChapters.value
  .slice()
  .sort((a, b) => b.count - a.count)
  .slice(0, 5)
  .map((item) => item.label))

const cards = computed(() => [
  { label: '真题总数', value: questionPage.value.totalElements || 0, sub: '覆盖历年考研数学真题', icon: 'fileText', iconClass: 'metric-card__icon--blue' },
  { label: '学科分类', value: taxonomy.value.length, sub: '按学科归档整理', icon: 'folder', iconClass: 'metric-card__icon--green' },
  { label: '覆盖章节', value: flatChapters.value.length, sub: '可直接进入分类浏览', icon: 'tag', iconClass: 'metric-card__icon--violet' },
  { label: '我的错题', value: mistakes.value.length, sub: '待复习巩固', icon: 'alert', iconClass: 'metric-card__icon--orange' },
  { label: '我的收藏', value: favorites.value.length, sub: '重要题目已收藏', icon: 'star', iconClass: 'metric-card__icon--blue' }
])

const yearBars = computed(() => {
  const yearMap = new Map()
  questionPage.value.items.forEach((item) => {
    yearMap.set(item.yearValue, (yearMap.get(item.yearValue) || 0) + 1)
  })
  const rows = [...yearMap.entries()]
    .sort((a, b) => b[0] - a[0])
    .slice(0, 8)
    .map(([year, count]) => ({ year, count }))
  const max = Math.max(...rows.map((item) => item.count), 1)
  return rows.map((item) => ({ ...item, height: Math.max(18, Math.round((item.count / max) * 100)) }))
})

const hotChapters = computed(() => flatChapters.value
  .slice()
  .sort((a, b) => b.count - a.count || a.subject.localeCompare(b.subject) || a.label.localeCompare(b.label))
  .slice(0, 5))

const recentRecords = computed(() => studyStats.value?.recentRecords?.slice(0, 4) || [])

onMounted(async () => {
  const userId = sessionState.user.userId
  const [questionData, treeData, studyData, favoriteData, mistakeData] = await Promise.all([
    api.getQuestions({ userId, page: 0, size: 120 }),
    api.getQuestionTaxonomy(),
    api.getStudyStats(userId),
    api.getFavorites(userId),
    api.getMistakes(userId)
  ])
  questionPage.value = questionData
  taxonomy.value = treeData
  studyStats.value = studyData
  favorites.value = favoriteData
  mistakes.value = mistakeData
})

function goSearch() {
  router.push({ name: 'questions', query: keyword.value ? { keyword: keyword.value } : {} })
}

function quickSearch(value) {
  keyword.value = value
  goSearch()
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
.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  background:
    radial-gradient(circle at 82% 82%, rgba(145, 190, 255, 0.26), transparent 28%),
    radial-gradient(circle at 74% 22%, rgba(255, 255, 255, 0.12), transparent 22%),
    linear-gradient(135deg, #2f6df3, #4b8cff);
  color: #fff;
}

.dashboard-hero__content h2 {
  margin: 0;
  font-size: 28px;
}

.dashboard-hero__content p {
  margin: 14px 0 0;
  opacity: 0.92;
}

.dashboard-search {
  margin-top: 28px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 96px;
  gap: 12px;
}

.dashboard-search .app-filter {
  border-color: rgba(255, 255, 255, 0.7);
}

.dashboard-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 18px;
}

.dashboard-tags .tag {
  border: 0;
}

.dashboard-hero__art {
  position: relative;
  min-height: 210px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  overflow: hidden;
}

.dashboard-hero__art :deep(.hero-illustration) {
  width: min(100%, 360px);
  margin-right: -28px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1.1fr 1fr 1fr;
  gap: 16px;
}

.bar-chart {
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 16px;
  align-items: end;
  min-height: 260px;
  padding: 8px 20px 20px;
}

.bar-chart__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bar-chart__column {
  width: 100%;
  height: 170px;
  display: flex;
  align-items: flex-end;
  border-radius: 12px;
  background: #f2f6fd;
  overflow: hidden;
}

.bar-chart__fill {
  width: 100%;
  border-radius: 12px 12px 0 0;
  background: linear-gradient(180deg, #68a3ff, #2f6df3);
}

.rank-list,
.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-list__item,
.recent-list__item {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: space-between;
}

.rank-list__index {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #edf4ff;
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
}

.rank-list__body,
.recent-list__item > div:first-child {
  flex: 1;
}

.rank-list__body p,
.recent-list__item p,
.recent-list__item small {
  margin: 6px 0 0;
  color: var(--muted);
}

.recent-list__right {
  text-align: right;
}

.dashboard-plan {
  padding: 20px;
}

.plan-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.plan-card {
  padding: 16px;
  border-radius: 16px;
  background: #f6f9ff;
}

.plan-card p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

@media (max-width: 1200px) {
  .dashboard-grid,
  .plan-grid,
  .dashboard-hero {
    grid-template-columns: 1fr;
  }

  .dashboard-hero__art {
    justify-content: center;
  }

  .dashboard-hero__art :deep(.hero-illustration) {
    margin-right: 0;
  }
}
</style>
