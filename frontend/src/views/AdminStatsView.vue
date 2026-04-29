<template>
  <div class="page-stack">
    <section class="page-header">
      <div>
        <div class="page-breadcrumb">管理后台 / 数据统计</div>
        <h1>数据统计</h1>
        <p>从用户增长、错题趋势、题型结构和知识点热度几个维度掌握平台运行情况。</p>
      </div>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--blue">
          <AppIcon name="fileText" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ stats.questionTotal || 0 }}</div>
          <div class="metric-card__label">题目总数</div>
          <div class="metric-card__sub">系统已收录题目</div>
        </div>
      </article>
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--green">
          <AppIcon name="users" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ stats.userTotal || 0 }}</div>
          <div class="metric-card__label">用户总数</div>
          <div class="metric-card__sub">前台注册用户规模</div>
        </div>
      </article>
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--orange">
          <AppIcon name="alert" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ stats.mistakeTotal || 0 }}</div>
          <div class="metric-card__label">错题累计</div>
          <div class="metric-card__sub">反映高频易错点</div>
        </div>
      </article>
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--violet">
          <AppIcon name="star" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ stats.favoriteTotal || 0 }}</div>
          <div class="metric-card__label">收藏累计</div>
          <div class="metric-card__sub">反映用户关注题目</div>
        </div>
      </article>
    </section>

    <section class="stats-grid">
      <article class="table-panel">
        <div class="panel-head">
          <h3>近七日用户新增</h3>
        </div>
        <div class="stat-chart">
          <div v-for="item in userTrend" :key="item.label" class="stat-chart__item">
            <div class="stat-chart__bar">
              <div class="stat-chart__fill stat-chart__fill--blue" :style="{ height: `${item.height}%` }"></div>
            </div>
            <strong>{{ item.label.slice(5) }}</strong>
            <span>{{ item.value }}</span>
          </div>
        </div>
      </article>

      <article class="table-panel">
        <div class="panel-head">
          <h3>近七日错题新增</h3>
        </div>
        <div class="stat-chart">
          <div v-for="item in mistakeTrend" :key="item.label" class="stat-chart__item">
            <div class="stat-chart__bar">
              <div class="stat-chart__fill stat-chart__fill--orange" :style="{ height: `${item.height}%` }"></div>
            </div>
            <strong>{{ item.label.slice(5) }}</strong>
            <span>{{ item.value }}</span>
          </div>
        </div>
      </article>
    </section>

    <section class="stats-grid stats-grid--triple">
      <article class="table-panel list-card">
        <div class="panel-head">
          <h3>题型分布</h3>
        </div>
        <div class="rank-list">
          <div v-for="item in stats.questionTypeDistribution || []" :key="item.label" class="rank-list__item">
            <div class="rank-list__body">
              <strong>{{ item.label }}</strong>
              <p>当前题型对应题目数量</p>
            </div>
            <span class="tag tag--blue">{{ item.value }}</span>
          </div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head">
          <h3>热门知识点 Top 10</h3>
        </div>
        <div class="rank-list">
          <div v-for="(item, index) in stats.hotKnowledgeTop || []" :key="item.label" class="rank-list__item">
            <span class="rank-list__index">{{ index + 1 }}</span>
            <div class="rank-list__body">
              <strong>{{ item.label }}</strong>
              <p>被系统收录次数</p>
            </div>
            <span>{{ item.value }}</span>
          </div>
        </div>
      </article>

      <article class="table-panel list-card">
        <div class="panel-head">
          <h3>高错题知识点 Top 10</h3>
        </div>
        <div class="rank-list">
          <div v-for="(item, index) in stats.highMistakeTop || []" :key="item.label" class="rank-list__item">
            <span class="rank-list__index">{{ index + 1 }}</span>
            <div class="rank-list__body">
              <strong>{{ item.label }}</strong>
              <p>被加入错题本次数</p>
            </div>
            <span>{{ item.value }}</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import AppIcon from '../components/AppIcon.vue'
import { api } from '../services/api'

const stats = ref({})

const userTrend = computed(() => toChartRows(stats.value.userTrend || []))
const mistakeTrend = computed(() => toChartRows(stats.value.mistakeTrend || []))

onMounted(async () => {
  stats.value = await api.getAdminStats()
})

function toChartRows(list) {
  const max = Math.max(...list.map((item) => item.value), 1)
  return list.map((item) => ({
    ...item,
    height: Math.max(12, Math.round((item.value / max) * 100))
  }))
}
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.stats-grid--triple {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.stat-chart {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 14px;
  align-items: end;
  min-height: 250px;
  padding: 12px 20px 20px;
}

.stat-chart__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-chart__bar {
  width: 100%;
  height: 165px;
  display: flex;
  align-items: flex-end;
  border-radius: 14px;
  background: #f1f5fd;
  overflow: hidden;
}

.stat-chart__fill {
  width: 100%;
  border-radius: 14px 14px 0 0;
}

.stat-chart__fill--blue {
  background: linear-gradient(180deg, #8ec2ff, #2f6df3);
}

.stat-chart__fill--orange {
  background: linear-gradient(180deg, #ffc38c, #ff9d2d);
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-top: 1px solid var(--line);
}

.rank-list__item:first-child {
  border-top: 0;
  padding-top: 0;
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

.rank-list__body {
  flex: 1;
}

.rank-list__body p {
  margin: 6px 0 0;
  color: var(--muted);
}

@media (max-width: 1200px) {
  .stats-grid,
  .stats-grid--triple {
    grid-template-columns: 1fr;
  }
}
</style>
