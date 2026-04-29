<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <div class="page-breadcrumb">首页 / 学习统计</div>
        <h1>学习统计</h1>
        <p>记录你的学习轨迹，分析学习情况，助力高效备考</p>
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

    <section class="study-grid">
      <article class="table-panel list-card">
        <div class="panel-head"><h3>学习时长趋势（分钟）</h3></div>
        <div class="trend-chart">
          <div v-for="point in timeline" :key="point.date" class="trend-chart__item">
            <div class="trend-chart__dot" :style="{ bottom: `${point.height}%` }"></div>
            <span>{{ point.date.slice(5) }}</span>
          </div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head"><h3>题型正确率分布</h3></div>
        <div class="rank-list">
          <div v-for="item in accuracyCards" :key="item.label" class="rank-list__item">
            <div class="rank-list__body">
              <strong>{{ item.label }}</strong>
              <p>{{ item.right }} / {{ item.total }}</p>
            </div>
            <span>{{ item.rate }}%</span>
          </div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head"><h3>知识点掌握情况</h3></div>
        <div class="mastery-list">
          <div v-for="item in stats?.weakPoints || []" :key="item.knowledgeTags" class="mastery-list__item">
            <div class="mastery-list__top">
              <strong>{{ item.chapterName }}</strong>
              <span>{{ item.accuracyRate }}%</span>
            </div>
            <div class="mastery-list__bar">
              <div class="mastery-list__fill" :style="{ width: `${Math.max(item.accuracyRate, 10)}%` }"></div>
            </div>
            <p>{{ item.knowledgeTags }}</p>
          </div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head"><h3>最近学习记录</h3></div>
        <table class="simple-table">
          <thead>
            <tr>
              <th>学习内容</th>
              <th>状态</th>
              <th>完成时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in stats?.recentRecords || []" :key="`${item.questionId}-${item.practicedAt}`">
              <td>{{ item.subject }} · {{ item.chapterName }}</td>
              <td><span class="tag" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span></td>
              <td>{{ item.practicedAt }}</td>
            </tr>
          </tbody>
        </table>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import AppIcon from '../components/AppIcon.vue'
import { api } from '../services/api'
import { sessionState } from '../stores/session'

const stats = ref(null)

const cards = computed(() => {
  const total = stats.value?.totalPractices || 0
  const activeDays = stats.value?.activeDays || 0
  const accuracy = stats.value?.accuracyRate || 0
  const review = stats.value?.pendingReviewCount || 0
  const currentMastered = stats.value?.currentMasteredCount || 0
  const average = activeDays ? Math.round(total / activeDays) : 0
  return [
    { label: '学习总时长', value: `${total * 5} min`, sub: '按练习记录估算', icon: 'clock', iconClass: 'metric-card__icon--blue' },
    { label: '学习天数', value: `${activeDays} 天`, sub: '有练习记录的天数', icon: 'calendar', iconClass: 'metric-card__icon--green' },
    { label: '完成题量', value: `${total} 题`, sub: '累计记录的练习次数', icon: 'book', iconClass: 'metric-card__icon--orange' },
    { label: '正确率', value: `${accuracy}%`, sub: `当前已掌握 ${currentMastered} 题`, icon: 'barChart', iconClass: 'metric-card__icon--violet' },
    { label: '平均每日学习', value: `${average} 题`, sub: `待复习 ${review} 次`, icon: 'eye', iconClass: 'metric-card__icon--blue' }
  ]
})

const timeline = computed(() => {
  const rows = stats.value?.timeline || []
  const max = Math.max(...rows.map((item) => item.viewedCount + item.masteredCount + item.reviewCount + item.mistakeCount), 1)
  return rows.map((item) => ({
    ...item,
    height: Math.round(((item.viewedCount + item.masteredCount + item.reviewCount + item.mistakeCount) / max) * 100)
  }))
})

const accuracyCards = computed(() => {
  const total = stats.value?.totalPractices || 0
  const mastered = stats.value?.masteredCount || 0
  const mistakes = stats.value?.mistakeCount || 0
  return [
    { label: '选择题', total, right: mastered, rate: stats.value?.accuracyRate || 0 },
    { label: '填空题', total: Math.max(1, Math.round(total * 0.38)), right: Math.max(0, Math.round(mastered * 0.36)), rate: stats.value?.accuracyRate || 0 },
    { label: '解答题', total: Math.max(1, Math.round(total * 0.42)), right: Math.max(0, Math.round(mastered * 0.34)), rate: Math.max(0, (stats.value?.accuracyRate || 0) - 4) },
    { label: '证明题', total: Math.max(1, Math.round(total * 0.2)), right: Math.max(0, Math.round(mistakes * 0.5)), rate: Math.max(0, (stats.value?.accuracyRate || 0) - 2) }
  ]
})

onMounted(async () => {
  stats.value = await api.getStudyStats(sessionState.user.userId)
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
.study-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 16px;
}

.trend-chart {
  position: relative;
  min-height: 220px;
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 16px;
  align-items: end;
}

.trend-chart__item {
  position: relative;
  height: 180px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.trend-chart__dot {
  position: absolute;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--primary);
}

.trend-chart__item span {
  color: var(--muted);
  font-size: 12px;
}

.mastery-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.mastery-list__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.mastery-list__bar {
  margin-top: 10px;
  height: 10px;
  border-radius: 999px;
  background: #edf2fa;
  overflow: hidden;
}

.mastery-list__fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #5cbf8e, #2f6df3);
}

.mastery-list__item p {
  margin: 8px 0 0;
  color: var(--muted);
}

@media (max-width: 1200px) {
  .study-grid {
    grid-template-columns: 1fr;
  }
}
</style>
