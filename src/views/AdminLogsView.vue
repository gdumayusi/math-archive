<template>
  <div class="page-stack">
    <section class="page-header">
      <div>
        <div class="page-breadcrumb">管理后台 / 操作日志</div>
        <h1>操作日志</h1>
        <p>记录后台中的重要行为，便于追踪题目审核、配置变更和账号管理操作。</p>
      </div>
    </section>

    <section class="table-panel">
      <div class="panel-head">
        <h3>日志列表</h3>
      </div>
      <div class="filters-row filters-row--compact">
        <AppInput v-model="filters.keyword" placeholder="搜索操作类型、执行人或详情" />
        <select v-model="filters.module" class="app-select">
          <option value="">全部模块</option>
          <option v-for="item in moduleOptions" :key="item" :value="item">{{ item }}</option>
        </select>
        <AppButton variant="secondary" @click="fetchLogs">查询</AppButton>
        <AppButton variant="secondary" @click="resetFilters">重置</AppButton>
      </div>

      <div class="admin-table-wrap">
        <table class="simple-table">
          <thead>
            <tr>
              <th>执行人</th>
              <th>身份</th>
              <th>操作类型</th>
              <th>所属模块</th>
              <th>详情</th>
              <th>状态</th>
              <th>时间</th>
              <th>IP</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in logs" :key="item.id">
              <td>{{ item.operatorName }}</td>
              <td>{{ item.operatorRole }}</td>
              <td>{{ item.actionType }}</td>
              <td>{{ item.actionModule }}</td>
              <td>{{ item.actionDetail }}</td>
              <td>
                <span class="tag" :class="item.actionStatus === '成功' ? 'tag--green' : 'tag--red'">
                  {{ item.actionStatus }}
                </span>
              </td>
              <td>{{ item.operatedAt }}</td>
              <td>{{ item.ipAddress }}</td>
            </tr>
            <tr v-if="!logs.length">
              <td colspan="8">
                <div class="empty-inline">暂无日志数据</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import AppButton from '../components/AppButton.vue'
import AppInput from '../components/AppInput.vue'
import { api } from '../services/api'

const logs = ref([])
const filters = reactive({
  keyword: '',
  module: ''
})

const moduleOptions = computed(() => [...new Set(logs.value.map((item) => item.actionModule).filter(Boolean))])

onMounted(fetchLogs)

async function fetchLogs() {
  logs.value = await api.getAdminLogs({
    keyword: filters.keyword,
    module: filters.module
  })
}

function resetFilters() {
  filters.keyword = ''
  filters.module = ''
  fetchLogs()
}
</script>

<style scoped>
.admin-table-wrap {
  overflow-x: auto;
}

.empty-inline {
  padding: 24px 0;
  color: var(--muted);
  text-align: center;
}
</style>
