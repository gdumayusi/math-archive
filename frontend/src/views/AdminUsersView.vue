<template>
  <div class="page-stack">
    <section class="page-header">
      <div>
        <div class="page-breadcrumb">管理后台 / 用户管理</div>
        <h1>用户管理</h1>
        <p>统一管理账号状态、角色权限、联系方式和学习活跃度。</p>
      </div>
      <AppButton @click="openCreate">
        <AppIcon name="users" :size="16" />
        <span>新增用户</span>
      </AppButton>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--blue">
          <AppIcon name="users" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ summary.total }}</div>
          <div class="metric-card__label">用户总数</div>
          <div class="metric-card__sub">当前列表统计</div>
        </div>
      </article>
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--green">
          <AppIcon name="user" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ summary.active }}</div>
          <div class="metric-card__label">启用用户</div>
          <div class="metric-card__sub">可正常登录与练习</div>
        </div>
      </article>
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--orange">
          <AppIcon name="alert" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ summary.disabled }}</div>
          <div class="metric-card__label">停用用户</div>
          <div class="metric-card__sub">已禁止系统访问</div>
        </div>
      </article>
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--violet">
          <AppIcon name="layout" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ summary.admins }}</div>
          <div class="metric-card__label">管理员</div>
          <div class="metric-card__sub">拥有后台管理权限</div>
        </div>
      </article>
    </section>

    <section class="table-panel">
      <div class="panel-head">
        <h3>账号列表</h3>
      </div>
      <div class="filters-row filters-row--compact">
        <AppInput v-model="filters.keyword" placeholder="搜索用户名、姓名、邮箱或手机号" />
        <select v-model="filters.role" class="app-select">
          <option value="">全部角色</option>
          <option value="USER">普通用户</option>
          <option value="ADMIN">管理员</option>
        </select>
        <select v-model="filters.active" class="app-select">
          <option value="">全部状态</option>
          <option value="true">启用</option>
          <option value="false">停用</option>
        </select>
        <AppButton variant="secondary" @click="fetchUsers">查询</AppButton>
        <AppButton variant="secondary" @click="resetFilters">重置</AppButton>
      </div>

      <div class="admin-table-wrap">
        <table class="simple-table">
          <thead>
            <tr>
              <th>账号</th>
              <th>姓名</th>
              <th>角色</th>
              <th>联系方式</th>
              <th>状态</th>
              <th>练习记录</th>
              <th>创建时间</th>
              <th>最近登录</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>
                <strong>{{ user.username }}</strong>
              </td>
              <td>{{ user.displayName }}</td>
              <td>
                <span class="tag" :class="user.role === 'ADMIN' ? 'tag--violet' : 'tag--blue'">
                  {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </span>
              </td>
              <td>
                <div class="cell-stack">
                  <span>{{ user.phone || '未填写手机号' }}</span>
                  <small>{{ user.email || '未填写邮箱' }}</small>
                </div>
              </td>
              <td>
                <span class="tag" :class="user.active ? 'tag--green' : 'tag--red'">
                  {{ user.active ? '启用中' : '已停用' }}
                </span>
              </td>
              <td>{{ user.practiceCount }}</td>
              <td>{{ user.createdAt }}</td>
              <td>{{ user.lastLoginAt }}</td>
              <td>
                <div class="table-actions">
                  <button class="icon-chip" @click="openEdit(user)">
                    <AppIcon name="edit" :size="15" />
                  </button>
                  <button class="icon-chip icon-chip--danger" @click="removeUser(user)">
                    <AppIcon name="trash" :size="15" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!users.length">
              <td colspan="9">
                <div class="empty-inline">暂无符合条件的用户数据</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <Teleport to="body">
      <div v-if="formVisible" class="drawer">
        <div class="drawer__scrim" @click="closeForm"></div>
        <div class="drawer__panel">
          <div class="drawer__header">
            <div>
              <h3>{{ editingId ? '编辑用户信息' : '新增用户账号' }}</h3>
              <p>可以维护账号、角色、联系方式与登录状态。</p>
            </div>
            <button class="icon-chip" @click="closeForm">
              <AppIcon name="close" :size="16" />
            </button>
          </div>

          <form class="drawer-form" @submit.prevent="submitForm">
            <div class="form-grid">
              <label>
                <span>用户名</span>
                <AppInput v-model="form.username" placeholder="例如 student_02" />
              </label>
              <label>
                <span>姓名</span>
                <AppInput v-model="form.displayName" placeholder="请输入真实姓名" />
              </label>
              <label>
                <span>手机号</span>
                <AppInput v-model="form.phone" placeholder="请输入手机号" />
              </label>
              <label>
                <span>邮箱</span>
                <AppInput v-model="form.email" placeholder="name@example.com" />
              </label>
              <label>
                <span>角色</span>
                <select v-model="form.role" class="app-select">
                  <option value="USER">普通用户</option>
                  <option value="ADMIN">管理员</option>
                </select>
              </label>
              <label>
                <span>状态</span>
                <select v-model="form.active" class="app-select">
                  <option :value="true">启用</option>
                  <option :value="false">停用</option>
                </select>
              </label>
            </div>

            <label>
              <span>{{ editingId ? '重置密码（可选）' : '初始密码' }}</span>
              <AppInput v-model="form.password" type="password" placeholder="请输入密码" />
            </label>

            <p v-if="formError" class="form-error">{{ formError }}</p>

            <div class="drawer__footer">
              <AppButton variant="secondary" @click="closeForm">取消</AppButton>
              <AppButton type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存用户' }}</AppButton>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import AppButton from '../components/AppButton.vue'
import AppIcon from '../components/AppIcon.vue'
import AppInput from '../components/AppInput.vue'
import { api } from '../services/api'

const users = ref([])
const formVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formError = ref('')

const filters = reactive({
  keyword: '',
  role: '',
  active: ''
})

const form = reactive(defaultForm())

const summary = computed(() => ({
  total: users.value.length,
  active: users.value.filter((item) => item.active).length,
  disabled: users.value.filter((item) => !item.active).length,
  admins: users.value.filter((item) => item.role === 'ADMIN').length
}))

onMounted(fetchUsers)

function defaultForm() {
  return {
    username: '',
    displayName: '',
    email: '',
    phone: '',
    role: 'USER',
    active: true,
    password: ''
  }
}

async function fetchUsers() {
  users.value = await api.getUsers({
    keyword: filters.keyword,
    role: filters.role,
    active: filters.active === '' ? undefined : filters.active === 'true'
  })
}

function resetFilters() {
  filters.keyword = ''
  filters.role = ''
  filters.active = ''
  fetchUsers()
}

function resetFormState() {
  Object.assign(form, defaultForm())
  editingId.value = null
  formError.value = ''
}

function openCreate() {
  resetFormState()
  formVisible.value = true
}

function openEdit(user) {
  resetFormState()
  editingId.value = user.id
  Object.assign(form, {
    username: user.username,
    displayName: user.displayName,
    email: user.email || '',
    phone: user.phone || '',
    role: user.role,
    active: user.active,
    password: ''
  })
  formVisible.value = true
}

function closeForm() {
  formVisible.value = false
  resetFormState()
}

async function submitForm() {
  saving.value = true
  formError.value = ''

  try {
    const payload = { ...form }
    if (!payload.password) {
      delete payload.password
    }

    if (editingId.value) {
      await api.updateUser(editingId.value, payload)
    } else {
      await api.createUser(payload)
    }
    closeForm()
    await fetchUsers()
  } catch (error) {
    formError.value = error.message
  } finally {
    saving.value = false
  }
}

async function removeUser(user) {
  if (!window.confirm(`确认删除用户 ${user.username} 吗？`)) {
    return
  }
  await api.deleteUser(user.id)
  await fetchUsers()
}
</script>

<style scoped>
.admin-table-wrap {
  overflow-x: auto;
}

.cell-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cell-stack small {
  color: var(--muted);
}

.empty-inline {
  padding: 24px 0;
  color: var(--muted);
  text-align: center;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-chip {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 1px solid var(--line-strong);
  border-radius: 10px;
  background: #fff;
  color: #456181;
}

.icon-chip--danger {
  color: var(--danger);
}

.drawer {
  position: fixed;
  inset: 0;
  z-index: 50;
}

.drawer__scrim {
  position: absolute;
  inset: 0;
  background: rgba(17, 30, 56, 0.36);
}

.drawer__panel {
  position: absolute;
  top: 0;
  right: 0;
  width: min(560px, 100%);
  height: 100%;
  padding: 24px;
  background: #fff;
  box-shadow: -14px 0 36px rgba(15, 32, 67, 0.16);
  overflow-y: auto;
}

.drawer__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.drawer__header h3 {
  margin: 0;
  font-size: 24px;
}

.drawer__header p {
  margin: 8px 0 0;
  color: var(--muted);
}

.drawer-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 22px;
}

.drawer-form label {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 6px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.form-error {
  margin: 0;
  color: var(--danger);
}

@media (max-width: 760px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
