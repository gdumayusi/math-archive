<template>
  <div class="app-shell">
    <aside class="app-sidebar">
      <div class="app-brand">
        <div class="app-brand__logo" :class="isAdmin ? 'app-brand__logo--admin' : 'app-brand__logo--brand'">
          <AppIcon :name="isAdmin ? 'adminShield' : 'brandBook'" :size="26" :stroke-width="1.6" />
        </div>
        <div class="app-brand__text">
          <strong>{{ brandTitle }}</strong>
          <span>{{ brandSubTitle }}</span>
        </div>
      </div>

      <nav class="app-nav">
        <button
          v-for="item in visibleMenu"
          :key="item.name"
          class="app-nav__item"
          :class="{ 'app-nav__item--active': route.name === item.name }"
          @click="router.push({ name: item.name })"
        >
          <AppIcon :name="item.icon" :size="20" />
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="app-sidebar__footer">
        <button
          v-if="isAdmin"
          class="app-settings"
          type="button"
          @click="router.push({ name: 'admin-settings' })"
        >
          <AppIcon name="settings" :size="19" />
          <span>系统设置</span>
        </button>
        <button class="app-settings" type="button" @click="logout">
          <AppIcon name="logout" :size="19" />
          <span>退出登录</span>
        </button>
      </div>
    </aside>

    <div class="app-main">
      <header class="app-topbar">
        <div class="app-topbar__left">
          <div>
            <div class="app-topbar__title">{{ currentTitle }}</div>
            <div class="app-topbar__subtitle">{{ currentSubtitle }}</div>
          </div>
        </div>

        <div class="app-topbar__search">
          <AppIcon name="search" :size="18" />
          <input
            v-model="globalKeyword"
            type="text"
            :placeholder="searchPlaceholder"
            @keydown.enter="goSearch"
          />
        </div>

        <div class="app-topbar__right">
          <button class="icon-button icon-button--badge" type="button">
            <AppIcon name="bell" :size="20" />
            <span class="icon-badge">{{ notifyCount }}</span>
          </button>
          <button class="app-user" type="button" @click="router.push({ name: 'profile' })">
            <div class="app-user__avatar" :class="[avatarTheme.className, { 'app-user__avatar--image': !!sessionState.user?.avatarImage }]">
              <img v-if="sessionState.user?.avatarImage" :src="sessionState.user.avatarImage" alt="头像" />
              <template v-else>{{ initial }}</template>
            </div>
            <div class="app-user__meta">
              <strong>{{ sessionState.user?.displayName || sessionState.user?.username }}</strong>
              <span>{{ isAdmin ? '管理员账户' : sessionState.user?.studentNo || '普通用户' }}</span>
            </div>
          </button>
        </div>
      </header>

      <main class="app-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'

import { clearSession, sessionState } from '../stores/session'
import { getAvatarTheme, getInitial } from '../utils/avatar'
import AppIcon from './AppIcon.vue'

const router = useRouter()
const route = useRoute()
const globalKeyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')

const userMenuItems = [
  { name: 'dashboard', label: '首页', icon: 'home', roles: ['USER'] },
  { name: 'questions', label: '真题管理', icon: 'fileText', roles: ['USER'] },
  { name: 'categories', label: '分类浏览', icon: 'folder', roles: ['USER'] },
  { name: 'mistakes', label: '错题本', icon: 'alert', roles: ['USER'] },
  { name: 'favorites', label: '收藏题库', icon: 'star', roles: ['USER'] },
  { name: 'study-stats', label: '学习统计', icon: 'barChart', roles: ['USER'] }
]

const adminMenuItems = [
  { name: 'admin-dashboard', label: '首页', icon: 'home', roles: ['ADMIN'] },
  { name: 'admin-users', label: '用户管理', icon: 'users', roles: ['ADMIN'] },
  { name: 'admin-reviews', label: '题目审核', icon: 'fileText', roles: ['ADMIN'] },
  { name: 'admin-stats', label: '数据统计', icon: 'barChart', roles: ['ADMIN'] },
  { name: 'admin-settings', label: '系统设置', icon: 'settings', roles: ['ADMIN'] },
  { name: 'admin-logs', label: '操作日志', icon: 'clock', roles: ['ADMIN'] }
]

const isAdmin = computed(() => sessionState.user?.role === 'ADMIN')
const visibleMenu = computed(() => (isAdmin.value ? adminMenuItems : userMenuItems))
const initial = computed(() => getInitial(sessionState.user?.displayName))
const avatarTheme = computed(() => getAvatarTheme(sessionState.user?.avatarKey))
const notifyCount = computed(() => (isAdmin.value ? 2 : 3))
const currentTitle = computed(() => route.meta.title || (isAdmin.value ? '管理后台' : '首页'))
const currentSubtitle = computed(() => (
  isAdmin.value
    ? '管理用户、审核题目并维护系统运行数据'
    : '按分类整理考研数学真题，衔接练习、收藏与错题复盘'
))
const brandTitle = computed(() => (isAdmin.value ? '考研数学后台管理' : '考研数学真题分类系统'))
const brandSubTitle = computed(() => (isAdmin.value ? 'Admin Console' : 'Question Archive'))
const searchPlaceholder = computed(() => (
  isAdmin.value ? '搜索题目编号、题干、投稿人...' : '搜索真题、题目或知识标签...'
))

watch(
  () => route.query.keyword,
  (value) => {
    globalKeyword.value = typeof value === 'string' ? value : ''
  }
)

function goSearch() {
  if (isAdmin.value) {
    router.push({
      name: 'admin-reviews',
      query: globalKeyword.value ? { keyword: globalKeyword.value } : {}
    })
    return
  }

  router.push({
    name: 'questions',
    query: globalKeyword.value ? { keyword: globalKeyword.value } : {}
  })
}

function logout() {
  clearSession()
  router.push('/login')
}
</script>

<style scoped>
.app-brand__text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.app-brand__logo--brand {
  background: #fff;
  border-color: #d7e4ff;
  box-shadow: 0 10px 20px rgba(47, 109, 243, 0.12);
}

.app-brand__logo--admin {
  color: #234fb8;
  background: linear-gradient(180deg, #f7faff, #e8f0ff);
  border-color: #cfddfb;
  box-shadow: 0 10px 20px rgba(35, 79, 184, 0.14);
}

.app-brand__text span {
  color: #7d8aa1;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.app-sidebar__footer {
  margin-top: auto;
}

.app-topbar__title {
  font-size: 20px;
  font-weight: 700;
  color: #203455;
}

.app-topbar__subtitle {
  margin-top: 4px;
  color: #7d8aa1;
  font-size: 13px;
}

.app-user__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
}

.app-user__meta strong {
  font-size: 14px;
}

.app-user__meta span {
  color: #7b89a0;
  font-size: 12px;
}

@media (max-width: 900px) {
  .app-topbar__left {
    justify-content: space-between;
  }

  .app-user__meta span {
    display: none;
  }
}
</style>
