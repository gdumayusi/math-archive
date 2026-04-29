<template>
  <div class="page-stack">
    <section class="page-header">
      <div>
        <div class="page-breadcrumb">管理后台 / 题目审核</div>
        <h1>题目审核</h1>
        <p>处理投稿题目、补录题库内容，并统一维护题干、答案与解析信息。</p>
      </div>
      <AppButton @click="openCreate">
        <AppIcon name="upload" :size="16" />
        <span>新增题目</span>
      </AppButton>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--orange">
          <AppIcon name="alert" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ overview.pendingQuestions || 0 }}</div>
          <div class="metric-card__label">待审核</div>
          <div class="metric-card__sub">需要管理员处理</div>
        </div>
      </article>
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--green">
          <AppIcon name="fileText" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ overview.approvedQuestions || 0 }}</div>
          <div class="metric-card__label">已通过</div>
          <div class="metric-card__sub">已进入正式题库</div>
        </div>
      </article>
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--red">
          <AppIcon name="close" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ overview.rejectedQuestions || 0 }}</div>
          <div class="metric-card__label">已驳回</div>
          <div class="metric-card__sub">需补充后再提交</div>
        </div>
      </article>
      <article class="metric-card">
        <div class="metric-card__icon metric-card__icon--blue">
          <AppIcon name="database" :size="24" />
        </div>
        <div>
          <div class="metric-card__value">{{ overview.totalQuestions || 0 }}</div>
          <div class="metric-card__label">题库总量</div>
          <div class="metric-card__sub">全部题目累计</div>
        </div>
      </article>
    </section>

    <section class="table-panel">
      <div class="panel-head">
        <h3>审核列表</h3>
      </div>
      <div class="filters-row">
        <AppInput v-model="filters.keyword" placeholder="搜索题目编号、题干、知识点、投稿人" />
        <select v-model="filters.status" class="app-select">
          <option value="ALL">全部状态</option>
          <option value="PENDING">待审核</option>
          <option value="APPROVED">已通过</option>
          <option value="REJECTED">已驳回</option>
        </select>
        <AppButton variant="secondary" @click="fetchReviews(0)">查询</AppButton>
        <AppButton variant="secondary" @click="resetFilters">重置</AppButton>
      </div>

      <div class="admin-table-wrap">
        <table class="simple-table">
          <thead>
            <tr>
              <th>题目编号</th>
              <th>题型信息</th>
              <th>知识模块</th>
              <th>投稿人</th>
              <th>状态</th>
              <th>提交时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in pageData.items" :key="item.id">
              <td>
                <strong>{{ item.archiveCode }}</strong>
              </td>
              <td>
                <div class="cell-stack">
                  <span>{{ item.questionType }}</span>
                  <small>{{ item.difficultyLevel }}</small>
                </div>
              </td>
              <td>
                <div class="cell-stack">
                  <span>{{ item.chapterName }}</span>
                  <small>{{ item.knowledgeTags }}</small>
                </div>
              </td>
              <td>
                <div class="cell-stack">
                  <span>{{ item.submitterName }}</span>
                  <small>{{ item.sourceOrg || '来源未填写' }}</small>
                </div>
              </td>
              <td>
                <span class="tag" :class="statusClass(item.reviewStatus)">{{ statusLabel(item.reviewStatus) }}</span>
              </td>
              <td>{{ item.submittedAt }}</td>
              <td>
                <div class="table-actions">
                  <button class="action-text" @click="openDetail(item)">查看</button>
                  <button class="action-text" @click="openEdit(item)">编辑</button>
                  <button class="action-text action-text--success" @click="approve(item)">通过</button>
                  <button class="action-text action-text--danger" @click="reject(item)">驳回</button>
                  <button class="icon-chip icon-chip--danger" @click="removeQuestion(item)">
                    <AppIcon name="trash" :size="15" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!pageData.items.length">
              <td colspan="7">
                <div class="empty-inline">暂无符合条件的题目</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar">
        <AppButton variant="secondary" :disabled="pageData.page <= 0" @click="fetchReviews(pageData.page - 1)">上一页</AppButton>
        <span>第 {{ pageData.page + 1 }} / {{ Math.max(pageData.totalPages, 1) }} 页</span>
        <AppButton variant="secondary" :disabled="pageData.page + 1 >= pageData.totalPages" @click="fetchReviews(pageData.page + 1)">下一页</AppButton>
      </div>
    </section>

    <Teleport to="body">
      <div v-if="drawerVisible" class="drawer">
        <div class="drawer__scrim" @click="closeDrawer"></div>
        <div class="drawer__panel drawer__panel--wide">
          <div class="drawer__header">
            <div>
              <h3>{{ drawerMode === 'detail' ? '题目详情' : editingId ? '编辑题目' : '新增题目' }}</h3>
              <p v-if="drawerMode === 'detail'">查看题干、答案、解析和审核状态。</p>
              <p v-else>维护题目基础信息，并支持直接进入审核流程。</p>
            </div>
            <button class="icon-chip" @click="closeDrawer">
              <AppIcon name="close" :size="16" />
            </button>
          </div>

          <template v-if="drawerMode === 'detail' && activeReview">
            <div class="detail-grid">
              <article class="detail-card">
                <span class="tag" :class="statusClass(activeReview.reviewStatus)">{{ statusLabel(activeReview.reviewStatus) }}</span>
                <h4>{{ activeReview.archiveCode }}</h4>
                <MathContent :content="activeReview.stemText" empty-text="暂无题干内容" />
              </article>
              <article class="detail-card">
                <h4>题目信息</h4>
                <div class="detail-list">
                  <div><span>章节</span><strong>{{ activeReview.chapterName }}</strong></div>
                  <div><span>知识点</span><strong>{{ activeReview.knowledgeTags }}</strong></div>
                  <div><span>来源</span><strong>{{ activeReview.sourceOrg || '未填写' }}</strong></div>
                  <div><span>投稿人</span><strong>{{ activeReview.submitterName }}</strong></div>
                  <div><span>题型</span><strong>{{ activeReview.questionType }}</strong></div>
                  <div><span>难度</span><strong>{{ activeReview.difficultyLevel }}</strong></div>
                </div>
              </article>
            </div>

            <article class="detail-card">
              <h4>参考答案</h4>
              <MathContent :content="activeReview.answerText" empty-text="暂无答案" />
            </article>

            <article class="detail-card">
              <h4>解析</h4>
              <MathContent :content="activeReview.analysisText" empty-text="暂无解析" />
            </article>

            <article v-if="activeReview.rejectReason" class="detail-card detail-card--warn">
              <h4>驳回原因</h4>
              <p>{{ activeReview.rejectReason }}</p>
            </article>
          </template>

          <form v-else class="drawer-form" @submit.prevent="submitForm">
            <div class="form-grid">
              <label>
                <span>题目编号</span>
                <AppInput v-model="form.archiveCode" placeholder="例如 T2026-001" />
              </label>
              <label>
                <span>年份</span>
                <select v-model.number="form.yearValue" class="app-select">
                  <option v-for="item in yearOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label>
                <span>卷别</span>
                <select v-model="form.paperName" class="app-select">
                  <option value="">请选择卷别</option>
                  <option v-for="item in paperOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label>
                <span>来源院校</span>
                <AppInput v-model="form.sourceOrg" placeholder="请输入来源院校或平台" />
              </label>
              <label>
                <span>科目</span>
                <select v-model="form.subject" class="app-select">
                  <option value="">请选择科目</option>
                  <option v-for="item in subjectOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label>
                <span>章节</span>
                <select v-model="form.chapterName" class="app-select" :disabled="!chapterOptions.length">
                  <option value="">{{ chapterOptions.length ? '请选择章节' : '请先选择科目' }}</option>
                  <option v-for="item in chapterOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label>
                <span>知识点</span>
                <select v-model="form.knowledgeTags" class="app-select" :disabled="!knowledgeTagOptions.length">
                  <option value="">{{ knowledgeTagOptions.length ? '请选择知识点' : '当前章节暂无知识点可选' }}</option>
                  <option v-for="item in knowledgeTagOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label>
                <span>题型</span>
                <select v-model="form.questionType" class="app-select">
                  <option value="">请选择题型</option>
                  <option v-for="item in questionTypeOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label>
                <span>难度</span>
                <select v-model="form.difficultyLevel" class="app-select">
                  <option value="">请选择难度</option>
                  <option v-for="item in difficultyOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
            </div>

            <label>
              <span>题干</span>
              <small class="drawer-form__hint">支持直接输入 LaTeX，例如 `$x^2+1$` 或 `$$\int_0^1 x dx$$`</small>
              <textarea v-model="form.stemText" class="app-textarea" rows="4" placeholder="请输入题干内容"></textarea>
            </label>

            <article class="latex-preview-card">
              <div class="latex-preview-card__head">
                <strong>题干预览</strong>
                <span>保存前确认公式显示是否正确</span>
              </div>
              <MathContent :content="form.stemText" empty-text="输入题干 LaTeX 后会在这里实时预览" />
            </article>

            <div class="form-grid">
              <label>
                <span>选项 A</span>
                <AppInput v-model="form.optionA" placeholder="可留空" />
              </label>
              <label>
                <span>选项 B</span>
                <AppInput v-model="form.optionB" placeholder="可留空" />
              </label>
              <label>
                <span>选项 C</span>
                <AppInput v-model="form.optionC" placeholder="可留空" />
              </label>
              <label>
                <span>选项 D</span>
                <AppInput v-model="form.optionD" placeholder="可留空" />
              </label>
            </div>

            <label>
              <span>答案</span>
              <textarea v-model="form.answerText" class="app-textarea" rows="2" placeholder="请输入参考答案"></textarea>
            </label>

            <label>
              <span>解析</span>
              <textarea v-model="form.analysisText" class="app-textarea" rows="4" placeholder="请输入答案解析"></textarea>
            </label>

            <p v-if="formError" class="form-error">{{ formError }}</p>

            <div class="drawer__footer">
              <AppButton variant="secondary" @click="closeDrawer">取消</AppButton>
              <AppButton type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存题目' }}</AppButton>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import AppButton from '../components/AppButton.vue'
import AppIcon from '../components/AppIcon.vue'
import AppInput from '../components/AppInput.vue'
import MathContent from '../components/MathContent.vue'
import { api } from '../services/api'

const route = useRoute()
const overview = ref({})
const pageData = ref({
  items: [],
  totalElements: 0,
  totalPages: 0,
  page: 0,
  size: 8
})

const filters = reactive({
  keyword: '',
  status: 'ALL'
})

const drawerVisible = ref(false)
const drawerMode = ref('detail')
const editingId = ref(null)
const activeReview = ref(null)
const saving = ref(false)
const formError = ref('')
const taxonomy = ref([])

const yearOptions = [2026, 2025, 2024, 2023, 2022, 2021, 2020, 2019, 2018]
const paperOptions = ['数学一', '数学二', '数学三']
const questionTypeOptions = ['单项选择题', '填空题', '解答题', '证明题']
const difficultyOptions = ['基础', '强化', '冲刺', '中等', '偏难', '偏易']

const form = reactive(defaultForm())

const subjectOptions = computed(() => taxonomy.value.map((item) => item.label))

const selectedSubjectNode = computed(() => taxonomy.value.find((item) => item.label === form.subject) || null)

const chapterOptions = computed(() => {
  const options = selectedSubjectNode.value?.children?.map((item) => item.label) || []
  if (form.chapterName && !options.includes(form.chapterName)) {
    return [form.chapterName, ...options]
  }
  return options
})

const selectedChapterNode = computed(() => selectedSubjectNode.value?.children?.find((item) => item.label === form.chapterName) || null)

const knowledgeTagOptions = computed(() => {
  const options = selectedChapterNode.value?.children?.map((item) => item.label) || []
  if (form.knowledgeTags && !options.includes(form.knowledgeTags)) {
    return [form.knowledgeTags, ...options]
  }
  return options
})

onMounted(async () => {
  if (typeof route.query.keyword === 'string') {
    filters.keyword = route.query.keyword
  }
  await Promise.all([fetchOverview(), fetchReviews(0), fetchTaxonomy()])
})

watch(
  () => route.query.keyword,
  async (value) => {
    filters.keyword = typeof value === 'string' ? value : ''
    if (route.name === 'admin-reviews') {
      await fetchReviews(0)
    }
  }
)

watch(
  () => form.subject,
  () => {
    if (!chapterOptions.value.includes(form.chapterName)) {
      form.chapterName = ''
    }
  }
)

watch(
  () => form.chapterName,
  () => {
    if (!knowledgeTagOptions.value.includes(form.knowledgeTags)) {
      form.knowledgeTags = ''
    }
  }
)

function defaultForm() {
  return {
    archiveCode: '',
    yearValue: new Date().getFullYear(),
    paperName: '',
    sourceOrg: '',
    subject: '',
    chapterName: '',
    knowledgeTags: '',
    questionType: '',
    difficultyLevel: '',
    stemText: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    answerText: '',
    analysisText: ''
  }
}

async function fetchOverview() {
  overview.value = await api.getAdminOverview()
}

async function fetchTaxonomy() {
  taxonomy.value = await api.getQuestionTaxonomy()
}

async function fetchReviews(page = 0) {
  pageData.value = await api.getAdminReviews({
    keyword: filters.keyword,
    status: filters.status,
    page,
    size: pageData.value.size
  })
}

function resetFilters() {
  filters.keyword = ''
  filters.status = 'ALL'
  fetchReviews(0)
}

function resetForm() {
  Object.assign(form, defaultForm())
  editingId.value = null
  formError.value = ''
}

function openCreate() {
  resetForm()
  drawerMode.value = 'form'
  activeReview.value = null
  drawerVisible.value = true
}

function openDetail(item) {
  drawerMode.value = 'detail'
  activeReview.value = item
  drawerVisible.value = true
}

async function openEdit(item) {
  resetForm()
  drawerMode.value = 'form'
  activeReview.value = item
  editingId.value = item.id
  const detail = await api.getQuestionDetail(item.id)
  Object.assign(form, {
    ...detail,
    sourceOrg: item.sourceOrg || ''
  })
  drawerVisible.value = true
}

function closeDrawer() {
  drawerVisible.value = false
  activeReview.value = null
  resetForm()
}

async function submitForm() {
  saving.value = true
  formError.value = ''

  try {
    const payload = {
      ...form,
      yearValue: Number(form.yearValue)
    }

    if (editingId.value) {
      await api.updateQuestion(editingId.value, payload)
    } else {
      await api.createQuestion(payload)
    }

    closeDrawer()
    await Promise.all([fetchOverview(), fetchReviews(pageData.value.page)])
  } catch (error) {
    formError.value = error.message
  } finally {
    saving.value = false
  }
}

async function approve(item) {
  await api.reviewQuestion(item.id, { action: 'APPROVE' })
  await Promise.all([fetchOverview(), fetchReviews(pageData.value.page)])
}

async function reject(item) {
  const reason = window.prompt('请输入驳回原因（可为空）', item.rejectReason || '')
  if (reason === null) {
    return
  }
  await api.reviewQuestion(item.id, { action: 'REJECT', rejectReason: reason })
  await Promise.all([fetchOverview(), fetchReviews(pageData.value.page)])
}

async function removeQuestion(item) {
  if (!window.confirm(`确认删除题目 ${item.archiveCode} 吗？`)) {
    return
  }
  await api.deleteQuestion(item.id)
  await Promise.all([
    fetchOverview(),
    fetchReviews(Math.max(pageData.value.page - (pageData.value.items.length === 1 ? 1 : 0), 0))
  ])
}

function statusLabel(status) {
  if (status === 'PENDING') return '待审核'
  if (status === 'APPROVED') return '已通过'
  return '已驳回'
}

function statusClass(status) {
  if (status === 'PENDING') return 'tag--orange'
  if (status === 'APPROVED') return 'tag--green'
  return 'tag--red'
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

.table-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  min-width: 220px;
}

.action-text {
  border: 0;
  padding: 0;
  background: transparent;
  color: #2f6df3;
  font-weight: 600;
}

.action-text--success {
  color: var(--success);
}

.action-text--danger {
  color: var(--danger);
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
  width: min(620px, 100%);
  height: 100%;
  padding: 24px;
  background: #fff;
  box-shadow: -14px 0 36px rgba(15, 32, 67, 0.16);
  overflow-y: auto;
}

.drawer__panel--wide {
  width: min(760px, 100%);
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
}

.drawer-form__hint {
  color: var(--muted);
  font-size: 12px;
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

.detail-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 14px;
  margin-top: 22px;
}

.detail-card {
  margin-top: 14px;
  padding: 18px;
  border-radius: 18px;
  background: #f8fbff;
  border: 1px solid #e6eefb;
}

.detail-card:first-of-type {
  margin-top: 22px;
}

.detail-card--warn {
  background: #fff8f2;
  border-color: #ffe2bf;
}

.detail-card h4 {
  margin: 0 0 12px;
  font-size: 18px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-list div {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.detail-list span {
  color: var(--muted);
}

.empty-inline {
  padding: 24px 0;
  color: var(--muted);
  text-align: center;
}

.latex-preview-card {
  padding: 16px 18px;
  border-radius: 16px;
  border: 1px solid #dbe7fb;
  background: #f8fbff;
}

.latex-preview-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.latex-preview-card__head strong {
  font-size: 15px;
}

.latex-preview-card__head span {
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 860px) {
  .form-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .latex-preview-card__head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
