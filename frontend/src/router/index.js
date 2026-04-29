import { createRouter, createWebHistory } from 'vue-router'

import AppLayout from '../components/AppLayout.vue'
import { sessionState } from '../stores/session'
import AdminDashboardView from '../views/AdminDashboardView.vue'
import AdminLogsView from '../views/AdminLogsView.vue'
import AdminReviewView from '../views/AdminReviewView.vue'
import AdminSettingsView from '../views/AdminSettingsView.vue'
import AdminStatsView from '../views/AdminStatsView.vue'
import AdminUsersView from '../views/AdminUsersView.vue'
import CategoryBrowseView from '../views/CategoryBrowseView.vue'
import DashboardView from '../views/DashboardView.vue'
import FavoritesView from '../views/FavoritesView.vue'
import LoginView from '../views/LoginView.vue'
import MistakesView from '../views/MistakesView.vue'
import ProfileView from '../views/ProfileView.vue'
import QuestionManageView from '../views/QuestionManageView.vue'
import QuestionPracticeView from '../views/QuestionPracticeView.vue'
import StudyStatsView from '../views/StudyStatsView.vue'

function homePathByRole(role) {
  return role === 'ADMIN' ? '/admin/dashboard' : '/dashboard'
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: () => homePathByRole(sessionState.user?.role)
        },
        { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { title: '首页', roles: ['USER'] } },
        { path: 'questions', name: 'questions', component: QuestionManageView, meta: { title: '真题管理', roles: ['USER'] } },
        { path: 'questions/:id', name: 'question-practice', component: QuestionPracticeView, meta: { title: '做题页面', roles: ['USER'] } },
        { path: 'categories', name: 'categories', component: CategoryBrowseView, meta: { title: '分类浏览', roles: ['USER'] } },
        { path: 'mistakes', name: 'mistakes', component: MistakesView, meta: { title: '错题本', roles: ['USER'] } },
        { path: 'favorites', name: 'favorites', component: FavoritesView, meta: { title: '收藏题库', roles: ['USER'] } },
        { path: 'study-stats', name: 'study-stats', component: StudyStatsView, meta: { title: '学习统计', roles: ['USER'] } },
        { path: 'profile', name: 'profile', component: ProfileView, meta: { title: '个人中心', roles: ['USER', 'ADMIN'] } },
        { path: 'admin/dashboard', name: 'admin-dashboard', component: AdminDashboardView, meta: { title: '管理首页', roles: ['ADMIN'] } },
        { path: 'admin/users', name: 'admin-users', component: AdminUsersView, meta: { title: '用户管理', roles: ['ADMIN'] } },
        { path: 'admin/reviews', name: 'admin-reviews', component: AdminReviewView, meta: { title: '题目审核', roles: ['ADMIN'] } },
        { path: 'admin/stats', name: 'admin-stats', component: AdminStatsView, meta: { title: '数据统计', roles: ['ADMIN'] } },
        { path: 'admin/settings', name: 'admin-settings', component: AdminSettingsView, meta: { title: '系统设置', roles: ['ADMIN'] } },
        { path: 'admin/logs', name: 'admin-logs', component: AdminLogsView, meta: { title: '操作日志', roles: ['ADMIN'] } }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.meta.public) {
    if (sessionState.user) {
      return homePathByRole(sessionState.user.role)
    }
    return true
  }

  if (to.meta.requiresAuth || to.matched.some((record) => record.meta.requiresAuth)) {
    if (!sessionState.user) {
      return '/login'
    }
  }

  const requiredRoles = to.meta.roles
  if (requiredRoles && !requiredRoles.includes(sessionState.user?.role)) {
    return homePathByRole(sessionState.user?.role)
  }

  return true
})

export default router
