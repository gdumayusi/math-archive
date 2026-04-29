<template>
  <div class="page-stack">
    <div class="page-header">
      <div>
        <h1>个人中心</h1>
        <p>完善个人资料、学习偏好与账号安全设置</p>
      </div>
      <button class="app-button app-button--secondary" type="button" @click="goToEditor">
        编辑资料
      </button>
    </div>

    <div v-if="loading" class="empty-state">个人信息加载中...</div>
    <template v-else-if="profile">
      <section class="table-panel profile-hero">
        <div class="profile-hero__main">
          <div class="profile-avatar-wrap">
            <div class="profile-avatar" :class="[avatarTheme.className, { 'profile-avatar--image': !!form.avatarImage }]">
              <img v-if="form.avatarImage" :src="form.avatarImage" alt="用户头像" />
              <template v-else>{{ initial }}</template>
            </div>
            <div class="profile-avatar-actions">
              <button class="app-button app-button--secondary" type="button" @click="pickAvatar">
                更换头像
              </button>
              <button
                class="app-button app-button--ghost"
                type="button"
                :disabled="!form.avatarImage"
                @click="removeAvatar"
              >
                移除头像
              </button>
            </div>
            <input
              ref="avatarInputRef"
              class="sr-only"
              type="file"
              accept="image/*"
              @change="handleAvatarChange"
            />
          </div>

          <div class="profile-hero__body">
            <div class="profile-hero__title">
              <div>
                <div class="profile-hero__name-row">
                  <h2>{{ profile.displayName }}</h2>
                  <span class="tag" :class="profile.role === 'ADMIN' ? 'tag--violet' : 'tag--blue'">
                    {{ profile.role === 'ADMIN' ? '管理员' : '普通用户' }}
                  </span>
                </div>
                <p class="profile-hero__bio">
                  {{ profile.bio || '还没有填写个人简介，补充你的备考方向和学习目标吧。' }}
                </p>
              </div>
              <button class="app-button app-button--primary" type="button" :disabled="savingProfile" @click="saveProfile">
                {{ savingProfile ? '保存中...' : '保存资料' }}
              </button>
            </div>

            <div class="profile-meta-grid">
              <div class="profile-meta-card">
                <span>用户名</span>
                <strong>{{ profile.username }}</strong>
              </div>
              <div class="profile-meta-card">
                <span>学号</span>
                <strong>{{ profile.studentNo || defaultStudentNo }}</strong>
              </div>
              <div class="profile-meta-card">
                <span>邮箱</span>
                <strong>{{ profile.email || '未绑定邮箱' }}</strong>
              </div>
              <div class="profile-meta-card">
                <span>注册时间</span>
                <strong>{{ profile.createdAt }}</strong>
              </div>
              <div class="profile-meta-card">
                <span>最近练习</span>
                <strong>{{ profile.lastPracticedAt }}</strong>
              </div>
              <div class="profile-meta-card">
                <span>资料完成度</span>
                <strong>{{ completenessRate }}%</strong>
              </div>
            </div>
          </div>
        </div>

        <div class="profile-hero__aside">
          <div class="hero-highlight">
            <span>当前学习状态</span>
            <strong>{{ learningState.title }}</strong>
            <p>{{ learningState.description }}</p>
          </div>
          <div class="hero-highlight">
            <span>偏好摘要</span>
            <strong>{{ preferenceSummary }}</strong>
            <p>{{ themeLabelMap[preferences.theme] }} · {{ fontSizeLabelMap[preferences.fontSize] }}</p>
          </div>
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

      <section class="profile-grid">
        <article class="table-panel list-card">
          <div class="panel-head">
            <h3>编辑资料</h3>
            <span class="panel-head__caption">支持上传任意图片作为头像</span>
          </div>
          <form class="profile-form" @submit.prevent="saveProfile">
            <div class="profile-form__avatar-card">
              <div class="profile-form__avatar-preview">
                <div class="profile-avatar profile-avatar--small" :class="[avatarTheme.className, { 'profile-avatar--image': !!form.avatarImage }]">
                  <img v-if="form.avatarImage" :src="form.avatarImage" alt="头像预览" />
                  <template v-else>{{ initial }}</template>
                </div>
                <div>
                  <strong>头像预览</strong>
                  <p>支持 JPG、PNG、WEBP、GIF 等常见图片格式，建议小于 1.4MB。</p>
                </div>
              </div>
              <div class="profile-form__avatar-buttons">
                <button class="app-button app-button--secondary" type="button" @click="pickAvatar">上传图片</button>
                <button class="app-button app-button--ghost" type="button" :disabled="!form.avatarImage" @click="removeAvatar">
                  清除头像
                </button>
              </div>
            </div>

            <div class="profile-form__grid">
              <label class="form-field">
                <span>显示名称</span>
                <input
                  ref="displayNameInputRef"
                  v-model.trim="form.displayName"
                  class="app-filter"
                  type="text"
                  maxlength="32"
                  placeholder="请输入显示名称"
                />
              </label>

              <label class="form-field">
                <span>邮箱</span>
                <input
                  v-model.trim="form.email"
                  class="app-filter"
                  type="email"
                  placeholder="请输入邮箱地址"
                />
              </label>

              <label class="form-field">
                <span>用户名</span>
                <input class="app-filter form-field__readonly" :value="profile.username" type="text" readonly />
              </label>

              <label class="form-field">
                <span>学号</span>
                <input class="app-filter form-field__readonly" :value="profile.studentNo || defaultStudentNo" type="text" readonly />
              </label>
            </div>

            <label class="form-field">
              <span>个人简介</span>
              <textarea
                v-model.trim="form.bio"
                class="app-textarea"
                rows="5"
                maxlength="255"
                placeholder="介绍一下你的备考阶段、擅长方向或学习目标"
              ></textarea>
            </label>

            <p v-if="profileError" class="inline-feedback inline-feedback--error">{{ profileError }}</p>
            <p v-else-if="profileMessage" class="inline-feedback inline-feedback--success">{{ profileMessage }}</p>

            <button class="app-button app-button--primary" :disabled="savingProfile">
              {{ savingProfile ? '保存中...' : '保存资料' }}
            </button>
          </form>
        </article>

        <article class="table-panel list-card">
          <div class="panel-head">
            <h3>学习偏好</h3>
            <span class="panel-head__caption">保存在当前浏览器</span>
          </div>
          <form class="profile-form" @submit.prevent="savePreferences">
            <label class="form-field">
              <span>默认难度</span>
              <select v-model="preferences.difficulty" class="app-select">
                <option value="all">全部</option>
                <option value="basic">基础巩固</option>
                <option value="improve">强化提升</option>
                <option value="challenge">冲刺突破</option>
              </select>
            </label>

            <label class="form-field">
              <span>每页显示数量</span>
              <select v-model="preferences.pageSize" class="app-select">
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="30">30</option>
                <option value="50">50</option>
              </select>
            </label>

            <label class="form-field">
              <span>默认排序方式</span>
              <select v-model="preferences.sortOrder" class="app-select">
                <option value="year-desc">年份降序</option>
                <option value="year-asc">年份升序</option>
                <option value="chapter">按章节</option>
                <option value="difficulty">按难度</option>
              </select>
            </label>

            <label class="form-field">
              <span>字体大小</span>
              <select v-model="preferences.fontSize" class="app-select">
                <option value="small">小</option>
                <option value="medium">中</option>
                <option value="large">大</option>
              </select>
            </label>

            <label class="form-field">
              <span>主题设置</span>
              <select v-model="preferences.theme" class="app-select">
                <option value="system">跟随系统</option>
                <option value="light">浅色</option>
                <option value="blue">清爽蓝</option>
              </select>
            </label>

            <div class="preference-preview">
              <div class="preference-item">
                <span>推荐浏览方式</span>
                <strong>{{ preferenceSummary }}</strong>
              </div>
              <div class="preference-item">
                <span>阅读体验</span>
                <strong>{{ themeLabelMap[preferences.theme] }} · {{ fontSizeLabelMap[preferences.fontSize] }}</strong>
              </div>
            </div>

            <p v-if="preferenceMessage" class="inline-feedback inline-feedback--success">{{ preferenceMessage }}</p>

            <button class="app-button app-button--primary" :disabled="savingPreferences">
              {{ savingPreferences ? '保存中...' : '保存偏好' }}
            </button>
          </form>
        </article>

        <article class="table-panel list-card">
          <div class="panel-head">
            <h3>最近活动</h3>
            <span class="panel-head__caption">{{ recentRecords.length }} 条记录</span>
          </div>
          <div v-if="recentRecords.length" class="recent-list">
            <div
              v-for="item in recentRecords"
              :key="`${item.questionId}-${item.practicedAt}`"
              class="recent-list__item recent-list__item--stack"
            >
              <div>
                <strong>{{ item.archiveCode }}</strong>
                <p>{{ item.subject }} / {{ item.chapterName }}</p>
              </div>
              <div class="recent-list__meta">
                <span class="tag" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
                <small>{{ item.practicedAt }}</small>
              </div>
            </div>
          </div>
          <div v-else class="empty-state empty-state--inner">
            暂无最近活动，去真题管理页开始练习吧。
          </div>
        </article>
      </section>

      <section class="profile-grid profile-grid--two">
        <article class="table-panel list-card">
          <div class="panel-head">
            <h3>学习进度概览</h3>
            <span class="panel-head__caption">按学科统计正确率</span>
          </div>
          <div v-if="subjectProgress.length" class="progress-grid">
            <div v-for="item in subjectProgress" :key="item.subject" class="progress-item">
              <div class="progress-item__top">
                <strong>{{ item.subject }}</strong>
                <span>{{ item.accuracyRate }}%</span>
              </div>
              <div class="progress-item__bar">
                <div class="progress-item__fill" :style="{ width: `${Math.max(item.accuracyRate, 8)}%` }"></div>
              </div>
              <p>共 {{ item.totalCount }} 题，已掌握 {{ item.masteredCount || 0 }} 题</p>
            </div>
          </div>
          <div v-else class="empty-state empty-state--inner">
            还没有学科进度数据，完成练习后这里会自动更新。
          </div>
        </article>

        <article class="table-panel list-card">
          <div class="panel-head">
            <h3>安全设置</h3>
            <span class="panel-head__caption">建议定期更新密码</span>
          </div>
          <form class="profile-form" @submit.prevent="savePassword">
            <label class="form-field">
              <span>当前密码</span>
              <input v-model="password.currentPassword" class="app-filter" type="password" placeholder="请输入当前密码" />
            </label>
            <label class="form-field">
              <span>新密码</span>
              <input v-model="password.newPassword" class="app-filter" type="password" placeholder="不少于 6 位" />
            </label>
            <label class="form-field">
              <span>确认新密码</span>
              <input v-model="password.confirmPassword" class="app-filter" type="password" placeholder="再次输入新密码" />
            </label>

            <p v-if="passwordError" class="inline-feedback inline-feedback--error">{{ passwordError }}</p>
            <p v-else-if="passwordMessage" class="inline-feedback inline-feedback--success">{{ passwordMessage }}</p>

            <button class="app-button app-button--primary" :disabled="savingPassword">
              {{ savingPassword ? '提交中...' : '修改密码' }}
            </button>
          </form>
        </article>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'

import AppIcon from '../components/AppIcon.vue'
import { api } from '../services/api'
import { patchSession, sessionState } from '../stores/session'
import { getAvatarTheme, getInitial } from '../utils/avatar'

const MAX_AVATAR_FILE_SIZE = 1.4 * 1024 * 1024

const loading = ref(false)
const savingProfile = ref(false)
const savingPassword = ref(false)
const savingPreferences = ref(false)
const profile = ref(null)
const stats = ref(null)
const avatarInputRef = ref(null)
const displayNameInputRef = ref(null)
const profileMessage = ref('')
const profileError = ref('')
const preferenceMessage = ref('')
const passwordMessage = ref('')
const passwordError = ref('')

const form = reactive({
  displayName: '',
  email: '',
  bio: '',
  avatarKey: sessionState.user?.avatarKey || 'classic',
  avatarImage: sessionState.user?.avatarImage || ''
})

const password = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const preferences = reactive({
  difficulty: 'all',
  pageSize: '10',
  sortOrder: 'year-desc',
  fontSize: 'medium',
  theme: 'system'
})

const difficultyLabelMap = {
  all: '全部难度',
  basic: '基础巩固',
  improve: '强化提升',
  challenge: '冲刺突破'
}

const sortOrderLabelMap = {
  'year-desc': '年份降序',
  'year-asc': '年份升序',
  chapter: '按章节',
  difficulty: '按难度'
}

const fontSizeLabelMap = {
  small: '小字号',
  medium: '中字号',
  large: '大字号'
}

const themeLabelMap = {
  system: '跟随系统',
  light: '浅色主题',
  blue: '清爽蓝主题'
}

const initial = computed(() => getInitial(form.displayName || profile.value?.displayName || sessionState.user?.displayName))
const avatarTheme = computed(() => getAvatarTheme(form.avatarKey || profile.value?.avatarKey || sessionState.user?.avatarKey))
const defaultStudentNo = computed(() => `2023${profile.value?.userId || sessionState.user?.userId || ''}`)
const recentRecords = computed(() => stats.value?.recentRecords?.slice(0, 6) || [])
const subjectProgress = computed(() => stats.value?.subjectProgress || [])

const completenessRate = computed(() => {
  const checks = [
    !!form.displayName,
    !!form.email,
    !!form.bio,
    !!form.avatarImage
  ]
  const score = checks.filter(Boolean).length
  return Math.round((score / checks.length) * 100)
})

const masteryRate = computed(() => {
  const total = profile.value?.practiceCount || 0
  if (!total) {
    return 0
  }
  return Math.round(((profile.value?.masteredCount || 0) / total) * 100)
})

const learningState = computed(() => {
  if ((profile.value?.practiceCount || 0) === 0) {
    return {
      title: '待开始',
      description: '还没有练习记录，建议先从分类浏览或真题管理开始。'
    }
  }
  if (masteryRate.value >= 70) {
    return {
      title: '进展良好',
      description: '掌握率较高，可以继续查漏补缺并回顾错题。'
    }
  }
  if (masteryRate.value >= 35) {
    return {
      title: '稳步提升',
      description: '已经进入持续练习阶段，适合围绕薄弱章节集中突破。'
    }
  }
  return {
    title: '基础积累中',
    description: '建议优先完成最近年份真题，并及时收藏高频重点题目。'
  }
})

const preferenceSummary = computed(() => (
  `${difficultyLabelMap[preferences.difficulty]} · ${preferences.pageSize} 条/页 · ${sortOrderLabelMap[preferences.sortOrder]}`
))

const cards = computed(() => [
  {
    label: '做题总数',
    value: profile.value?.practiceCount || 0,
    sub: '累计练习记录',
    icon: 'fileText',
    iconClass: 'metric-card__icon--blue'
  },
  {
    label: '正确题数',
    value: profile.value?.masteredCount || 0,
    sub: `当前掌握率 ${masteryRate.value}%`,
    icon: 'barChart',
    iconClass: 'metric-card__icon--green'
  },
  {
    label: '收藏题目',
    value: profile.value?.favoriteCount || 0,
    sub: '重要题目已收藏',
    icon: 'star',
    iconClass: 'metric-card__icon--orange'
  },
  {
    label: '错题数量',
    value: profile.value?.mistakeCount || 0,
    sub: '待复习错题',
    icon: 'alert',
    iconClass: 'metric-card__icon--violet'
  },
  {
    label: '学习天数',
    value: stats.value?.activeDays || 0,
    sub: '持续学习中',
    icon: 'calendar',
    iconClass: 'metric-card__icon--blue'
  }
])

onMounted(async () => {
  await loadProfile()
  loadPreferences()
})

async function loadProfile() {
  loading.value = true
  try {
    const userId = sessionState.user?.userId
    if (!userId) {
      return
    }
    const [profileData, statsData] = await Promise.all([
      api.getProfile(userId),
      api.getStudyStats(userId)
    ])
    profile.value = profileData
    stats.value = statsData
    syncForm(profileData)
  } finally {
    loading.value = false
  }
}

function syncForm(profileData) {
  form.displayName = profileData.displayName || ''
  form.email = profileData.email || ''
  form.bio = profileData.bio || ''
  form.avatarKey = profileData.avatarKey || sessionState.user?.avatarKey || 'classic'
  form.avatarImage = profileData.avatarImage || ''
}

function loadPreferences() {
  try {
    const raw = localStorage.getItem(preferenceStorageKey())
    if (!raw) {
      return
    }
    const saved = JSON.parse(raw)
    preferences.difficulty = saved.difficulty || preferences.difficulty
    preferences.pageSize = saved.pageSize || preferences.pageSize
    preferences.sortOrder = saved.sortOrder || preferences.sortOrder
    preferences.fontSize = saved.fontSize || preferences.fontSize
    preferences.theme = saved.theme || preferences.theme
  } catch {
    // Ignore broken local preference data and keep defaults.
  }
}

function preferenceStorageKey() {
  return `math-archive-profile-preferences:${sessionState.user?.userId || 'guest'}`
}

function goToEditor() {
  const editor = displayNameInputRef.value
  if (!editor) {
    return
  }
  editor.scrollIntoView({ behavior: 'smooth', block: 'center' })
  nextTick(() => editor.focus())
}

function pickAvatar() {
  avatarInputRef.value?.click()
}

async function handleAvatarChange(event) {
  const [file] = event.target.files || []
  profileError.value = ''
  profileMessage.value = ''

  if (!file) {
    return
  }
  if (!file.type.startsWith('image/')) {
    profileError.value = '请选择图片文件作为头像。'
    event.target.value = ''
    return
  }
  if (file.size > MAX_AVATAR_FILE_SIZE) {
    profileError.value = '图片过大，请选择 1.4MB 以内的头像图片。'
    event.target.value = ''
    return
  }

  form.avatarImage = await readFileAsDataUrl(file)
  event.target.value = ''
}

function removeAvatar() {
  form.avatarImage = ''
  profileMessage.value = ''
  profileError.value = ''
}

async function saveProfile() {
  profileError.value = ''
  profileMessage.value = ''

  if (!form.displayName.trim()) {
    profileError.value = '显示名称不能为空。'
    return
  }

  savingProfile.value = true
  try {
    const updated = await api.updateProfile(sessionState.user.userId, {
      displayName: form.displayName,
      email: form.email,
      bio: form.bio,
      avatarKey: form.avatarKey,
      avatarImage: form.avatarImage || null
    })
    profile.value = updated
    syncForm(updated)
    patchSession({
      displayName: updated.displayName,
      avatarKey: updated.avatarKey,
      avatarImage: updated.avatarImage,
      studentNo: updated.studentNo
    })
    profileMessage.value = '个人资料已保存。'
  } catch (error) {
    profileError.value = error.message || '保存资料失败，请稍后重试。'
  } finally {
    savingProfile.value = false
  }
}

function savePreferences() {
  preferenceMessage.value = ''
  savingPreferences.value = true

  try {
    localStorage.setItem(preferenceStorageKey(), JSON.stringify({ ...preferences }))
    preferenceMessage.value = '学习偏好已保存到当前浏览器。'
  } finally {
    savingPreferences.value = false
  }
}

async function savePassword() {
  passwordError.value = ''
  passwordMessage.value = ''

  if (!password.currentPassword || !password.newPassword || !password.confirmPassword) {
    passwordError.value = '请完整填写密码信息。'
    return
  }
  if (password.newPassword.length < 6) {
    passwordError.value = '新密码不能少于 6 位。'
    return
  }
  if (password.newPassword !== password.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致。'
    return
  }

  savingPassword.value = true
  try {
    await api.changePassword(sessionState.user.userId, password)
    password.currentPassword = ''
    password.newPassword = ''
    password.confirmPassword = ''
    passwordMessage.value = '密码修改成功。'
  } catch (error) {
    passwordError.value = error.message || '密码修改失败，请稍后重试。'
  } finally {
    savingPassword.value = false
  }
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

function readFileAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('头像读取失败，请重新选择图片。'))
    reader.readAsDataURL(file)
  })
}
</script>

<style scoped>
.profile-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
  padding: 24px;
}

.profile-hero__main {
  display: flex;
  gap: 20px;
  min-width: 0;
}

.profile-avatar-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.profile-avatar {
  width: 104px;
  height: 104px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  overflow: hidden;
  font-size: 30px;
  font-weight: 700;
  flex-shrink: 0;
}

.profile-avatar--small {
  width: 72px;
  height: 72px;
  font-size: 22px;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-avatar--image {
  background: #dbe5ef;
}

.profile-avatar-actions,
.profile-form__avatar-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.profile-hero__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-hero__title {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.profile-hero__name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.profile-hero__name-row h2 {
  margin: 0;
  font-size: 30px;
}

.profile-hero__bio {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.profile-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.profile-meta-card,
.hero-highlight,
.profile-form__avatar-card {
  border: 1px solid var(--line);
  background: var(--surface-soft);
  border-radius: 16px;
}

.profile-meta-card {
  padding: 14px 16px;
}

.profile-meta-card span,
.hero-highlight span,
.panel-head__caption,
.form-field span {
  color: var(--muted);
  font-size: 13px;
}

.profile-meta-card strong {
  display: block;
  margin-top: 8px;
  font-size: 15px;
}

.profile-hero__aside {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-highlight {
  padding: 18px;
}

.hero-highlight strong {
  display: block;
  margin-top: 10px;
  font-size: 20px;
}

.hero-highlight p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.profile-grid--two {
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 0.8fr);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 0 20px 20px;
}

.profile-form__avatar-card {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
}

.profile-form__avatar-preview {
  display: flex;
  gap: 14px;
  align-items: center;
}

.profile-form__avatar-preview p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.profile-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-field__readonly {
  background: #f7f9fd;
  color: #60718d;
}

.preference-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--line);
}

.preference-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.recent-list__item--stack {
  align-items: flex-start;
}

.recent-list__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.progress-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  padding: 0 20px 20px;
}

.progress-item {
  padding: 16px;
  border-radius: 16px;
  background: var(--surface-soft);
  border: 1px solid var(--line);
}

.progress-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.progress-item__bar {
  margin-top: 10px;
  height: 10px;
  border-radius: 999px;
  background: #edf2fa;
  overflow: hidden;
}

.progress-item__fill {
  height: 100%;
  background: linear-gradient(90deg, #5cbf8e, #2f6df3);
}

.progress-item p {
  margin: 10px 0 0;
  color: var(--muted);
}

.inline-feedback {
  margin: 0;
  font-size: 14px;
}

.inline-feedback--error {
  color: var(--danger);
}

.inline-feedback--success {
  color: var(--success);
}

.app-button--ghost {
  background: transparent;
  color: #566985;
  border-color: var(--line);
}

.empty-state--inner {
  min-height: 220px;
  display: grid;
  place-items: center;
  padding: 20px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 1400px) {
  .profile-hero,
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-hero__main {
    flex-direction: column;
  }

  .profile-meta-grid,
  .profile-grid--two,
  .progress-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .profile-hero {
    padding: 20px;
  }

  .profile-hero__title,
  .profile-form__avatar-card,
  .profile-form__avatar-preview,
  .preference-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-meta-grid,
  .profile-form__grid,
  .progress-grid {
    grid-template-columns: 1fr;
  }

  .recent-list__meta {
    align-items: flex-start;
  }
}
</style>
