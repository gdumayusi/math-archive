<template>
  <div class="page-stack">
    <section class="hero-panel admin-hero">
      <div class="admin-hero__content">
        <div class="page-breadcrumb admin-hero__crumb">管理后台 / 首页</div>
        <h1>考研数学真题分类管理系统</h1>
        <p>围绕用户、题目审核、系统配置和运行日志建立一体化后台工作台。</p>
        <div class="admin-hero__meta">
          <span class="tag tag--blue">今日新增用户 {{ overview.todayNewUsers || 0 }}</span>
          <span class="tag tag--orange">待审核题目 {{ overview.pendingQuestions || 0 }}</span>
        </div>
      </div>
      <div class="admin-hero__art">
        <HeroIllustration variant="admin" />
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

    <section class="admin-grid">
      <article class="table-panel">
        <div class="panel-head">
          <h3>近七日新增用户</h3>
        </div>
        <div class="mini-chart">
          <div v-for="item in userTrend" :key="item.label" class="mini-chart__item">
            <div class="mini-chart__bar">
              <div class="mini-chart__fill" :style="{ height: `${item.height}%` }"></div>
            </div>
            <strong>{{ item.label }}</strong>
            <span>{{ item.value }}</span>
          </div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head">
          <h3>待审核题目</h3>
          <button class="panel-head__link" type="button" @click="router.push({ name: 'admin-reviews' })">进入审核</button>
        </div>
        <div class="review-list">
          <div v-for="item in pendingReviews" :key="item.id" class="review-list__item">
            <div>
              <strong>{{ item.archiveCode }}</strong>
              <p>{{ item.chapterName }} / {{ item.knowledgeTags }}</p>
            </div>
            <div class="review-list__right">
              <span class="tag tag--orange">{{ item.reviewStatus }}</span>
              <small>{{ item.submittedAt }}</small>
            </div>
          </div>
          <div v-if="!pendingReviews.length" class="empty-inline">当前没有待审核题目</div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head">
          <h3>最近操作</h3>
          <button class="panel-head__link" type="button" @click="router.push({ name: 'admin-logs' })">查看全部</button>
        </div>
        <div class="log-list">
          <div v-for="item in recentLogs" :key="item.id" class="log-list__item">
            <div>
              <strong>{{ item.actionType }}</strong>
              <p>{{ item.actionDetail }}</p>
            </div>
            <div class="log-list__right">
              <span class="tag" :class="item.actionStatus === '成功' ? 'tag--green' : 'tag--red'">{{ item.actionStatus }}</span>
              <small>{{ item.operatedAt }}</small>
            </div>
          </div>
          <div v-if="!recentLogs.length" class="empty-inline">暂无操作日志</div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppIcon from '../components/AppIcon.vue'
import HeroIllustration from '../components/HeroIllustration.vue'
import { api } from '../services/api'

const router = useRouter()
const overview = ref({})
const stats = ref({ userTrend: [] })
const pendingReviews = ref([])
const recentLogs = ref([])

const cards = computed(() => [
  { label: '系统用户', value: overview.value.totalUsers || 0, sub: '含管理员与学生用户', icon: 'users', iconClass: 'metric-card__icon--blue' },
  { label: '启用用户', value: overview.value.activeUsers || 0, sub: '可以正常登录系统', icon: 'user', iconClass: 'metric-card__icon--green' },
  { label: '题目总数', value: overview.value.totalQuestions || 0, sub: '后台已接入真题题目', icon: 'fileText', iconClass: 'metric-card__icon--violet' },
  { label: '待审核题目', value: overview.value.pendingQuestions || 0, sub: '需要管理员处理', icon: 'alert', iconClass: 'metric-card__icon--orange' },
  { label: '收藏总量', value: overview.value.totalFavorites || 0, sub: '用户收藏行为累计', icon: 'star', iconClass: 'metric-card__icon--blue' }
])

const userTrend = computed(() => {
  const rows = stats.value.userTrend || []
  const max = Math.max(...rows.map((item) => item.value), 1)
  return rows.map((item) => ({
    label: item.label.slice(5),
    value: item.value,
    height: Math.max(12, Math.round((item.value / max) * 100))
  }))
})

onMounted(async () => {
  const [overviewData, statsData, reviewData, logData] = await Promise.all([
    api.getAdminOverview(),
    api.getAdminStats(),
    api.getAdminReviews({ status: 'PENDING', page: 0, size: 5 }),
    api.getAdminLogs()
  ])

  overview.value = overviewData
  stats.value = statsData
  pendingReviews.value = reviewData.items || []
  recentLogs.value = (logData || []).slice(0, 5)
})
</script>

<style scoped>
.admin-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  align-items: center;
  gap: 18px;
  background:
    radial-gradient(circle at 82% 78%, rgba(104, 157, 255, 0.16), transparent 30%),
    radial-gradient(circle at 72% 18%, rgba(255, 255, 255, 0.06), transparent 20%),
    linear-gradient(135deg, #16326f, #2452be 58%, #6ea0f5);
  color: #fff;
}

.admin-hero__content {
  min-width: 0;
}

.admin-hero h1 {
  margin: 14px 0 0;
  font-size: 30px;
}

.admin-hero p {
  margin: 14px 0 0;
  max-width: 700px;
  line-height: 1.8;
  opacity: 0.94;
}

.admin-hero__crumb {
  color: rgba(255, 255, 255, 0.82);
}

.admin-hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.admin-hero__art {
  min-height: 210px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  overflow: hidden;
}

.admin-hero__art :deep(.hero-illustration) {
  width: min(100%, 390px);
  margin-right: -20px;
}

.admin-grid {
  display: grid;
  grid-template-columns: 1.05fr 1fr 1fr;
  gap: 16px;
}

.mini-chart {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 14px;
  align-items: end;
  min-height: 250px;
  padding: 12px 20px 20px;
}

.mini-chart__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.mini-chart__bar {
  width: 100%;
  height: 165px;
  display: flex;
  align-items: flex-end;
  border-radius: 14px;
  background: #f1f5fd;
  overflow: hidden;
}

.mini-chart__fill {
  width: 100%;
  background: linear-gradient(180deg, #8ec2ff, #2f6df3);
  border-radius: 14px 14px 0 0;
}

.review-list,
.log-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-list__item,
.log-list__item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-top: 1px solid var(--line);
}

.review-list__item:first-child,
.log-list__item:first-child {
  border-top: 0;
  padding-top: 0;
}

.review-list__item p,
.log-list__item p,
.review-list__item small,
.log-list__item small {
  margin: 6px 0 0;
  color: var(--muted);
}

.review-list__right,
.log-list__right {
  text-align: right;
}

.empty-inline {
  color: var(--muted);
  padding: 24px 0 8px;
  text-align: center;
}

@media (max-width: 1200px) {
  .admin-grid {
    grid-template-columns: 1fr;
  }

  .admin-hero {
    grid-template-columns: 1fr;
  }

  .admin-hero__art {
    justify-content: center;
  }

  .admin-hero__art :deep(.hero-illustration) {
    margin-right: 0;
  }
}
</style>
