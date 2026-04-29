<template>
  <div class="page-stack">
    <section class="page-header">
      <div>
        <div class="page-breadcrumb">管理后台 / 系统设置</div>
        <h1>系统设置</h1>
        <p>维护站点基础信息、联系方式和展示文案，保存后立即同步到后台数据库。</p>
      </div>
    </section>

    <section class="settings-layout">
      <article class="table-panel settings-form-panel">
        <div class="panel-head">
          <h3>基础配置</h3>
        </div>
        <form class="settings-form" @submit.prevent="saveSettings">
          <div class="form-grid">
            <label>
              <span>站点名称</span>
              <AppInput v-model="form.siteName" placeholder="请输入站点名称" />
            </label>
            <label>
              <span>管理员邮箱</span>
              <AppInput v-model="form.adminEmail" placeholder="请输入联系邮箱" />
            </label>
            <label>
              <span>联系电话</span>
              <AppInput v-model="form.contactPhone" placeholder="请输入联系电话" />
            </label>
            <label>
              <span>备案号</span>
              <AppInput v-model="form.recordNumber" placeholder="请输入备案号" />
            </label>
          </div>

          <label>
            <span>系统简介</span>
            <textarea v-model="form.siteIntro" class="app-textarea" rows="5" placeholder="请输入系统简介"></textarea>
          </label>

          <p v-if="saveMessage" class="save-message">{{ saveMessage }}</p>

          <div class="settings-actions">
            <AppButton variant="secondary" @click="loadSettings">重新加载</AppButton>
            <AppButton type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存设置' }}</AppButton>
          </div>
        </form>
      </article>

      <aside class="settings-side">
        <article class="metric-card settings-side__card">
          <div class="metric-card__icon metric-card__icon--violet">
            <AppIcon name="database" :size="24" />
          </div>
          <div>
            <div class="metric-card__value">{{ settings.usedStorageGb || 0 }}/{{ settings.totalStorageGb || 0 }}GB</div>
            <div class="metric-card__label">存储使用情况</div>
            <div class="metric-card__sub">数据库与静态资源占用</div>
          </div>
        </article>

        <article class="table-panel list-card">
          <div class="panel-head">
            <h3>系统信息</h3>
          </div>
          <div class="info-list">
            <div><span>当前版本</span><strong>{{ settings.version || '--' }}</strong></div>
            <div><span>发布日期</span><strong>{{ settings.publishDate || '--' }}</strong></div>
            <div><span>维护团队</span><strong>{{ settings.teamName || '--' }}</strong></div>
            <div><span>系统时间</span><strong>{{ settings.currentTime || '--' }}</strong></div>
          </div>
        </article>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'

import AppButton from '../components/AppButton.vue'
import AppIcon from '../components/AppIcon.vue'
import AppInput from '../components/AppInput.vue'
import { api } from '../services/api'

const settings = ref({})
const saving = ref(false)
const saveMessage = ref('')

const form = reactive({
  siteName: '',
  adminEmail: '',
  contactPhone: '',
  siteIntro: '',
  recordNumber: ''
})

onMounted(loadSettings)

async function loadSettings() {
  settings.value = await api.getAdminSettings()
  Object.assign(form, {
    siteName: settings.value.siteName || '',
    adminEmail: settings.value.adminEmail || '',
    contactPhone: settings.value.contactPhone || '',
    siteIntro: settings.value.siteIntro || '',
    recordNumber: settings.value.recordNumber || ''
  })
  saveMessage.value = ''
}

async function saveSettings() {
  saving.value = true
  saveMessage.value = ''
  try {
    settings.value = await api.updateAdminSettings({ ...form })
    saveMessage.value = '系统设置已保存'
  } catch (error) {
    saveMessage.value = error.message
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.settings-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) 360px;
  gap: 16px;
}

.settings-form-panel {
  padding-bottom: 20px;
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 20px 4px;
}

.settings-form label {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.settings-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.settings-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.settings-side__card {
  min-height: 126px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-list div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.info-list div:first-child {
  border-top: 0;
  padding-top: 0;
}

.info-list span {
  color: var(--muted);
}

.save-message {
  margin: 0;
  color: var(--primary);
}

@media (max-width: 1100px) {
  .settings-layout,
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
