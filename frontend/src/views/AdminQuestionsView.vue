<template>
  <div class="page-stack">
    <section class="toolbar">
      <div class="toolbar__actions">
        <AppInput v-model="keyword" placeholder="输入档案编号、知识点或标签检索" />
        <AppButton variant="secondary" @click="fetchQuestions(0)">筛选</AppButton>
      </div>
      <AppButton @click="openCreate">
        <AppIcon name="upload" :size="14" />
        <span>新增题目</span>
      </AppButton>
    </section>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>档案编号</th>
            <th>试卷信息</th>
            <th>章节</th>
            <th>知识点标签</th>
            <th>录入时间</th>
            <th>管理操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pageData.items" :key="item.id">
            <td>{{ item.archiveCode }}</td>
            <td>
              <div class="table-tags">
                <AppBadge>{{ item.yearValue }}</AppBadge>
                <AppBadge tone="info">{{ item.subject }}</AppBadge>
              </div>
            </td>
            <td>{{ item.chapterName }}</td>
            <td>{{ item.knowledgeTags }}</td>
            <td>{{ item.createdDate }}</td>
            <td>
              <div class="table-actions">
                <button class="icon-chip" @click="openEdit(item)">
                  <AppIcon name="edit" :size="15" />
                </button>
                <button class="icon-chip icon-chip--danger" @click="removeQuestion(item)">
                  <AppIcon name="trash" :size="15" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination">
      <AppButton variant="secondary" :disabled="pageData.page <= 0" @click="fetchQuestions(pageData.page - 1)">上一页</AppButton>
      <span>第 {{ pageData.page + 1 }} / {{ Math.max(pageData.totalPages, 1) }} 页</span>
      <AppButton variant="secondary" :disabled="pageData.page + 1 >= pageData.totalPages" @click="fetchQuestions(pageData.page + 1)">下一页</AppButton>
    </div>

    <Teleport to="body">
      <div v-if="formVisible" class="drawer">
        <div class="drawer__scrim" @click="closeForm"></div>
        <div class="drawer__panel">
          <div class="drawer__header">
            <div>
              <h3>{{ editingId ? '编辑题目档案' : '新建题目档案' }}</h3>
              <p>补齐真题元信息、知识点标签、选项与解析。</p>
            </div>
            <button class="icon-chip" @click="closeForm">
              <AppIcon name="close" :size="16" />
            </button>
          </div>

          <form class="drawer-form" @submit.prevent="submitForm">
            <div class="form-grid">
              <label>
                <span>档案编号</span>
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
                <span>科目</span>
                <select v-model="form.subject" class="app-select">
                  <option value="">请选择科目</option>
                  <option v-for="item in subjectOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label>
                <span>核心章节</span>
                <select v-model="form.chapterName" class="app-select" :disabled="!chapterOptions.length">
                  <option value="">{{ chapterOptions.length ? '请选择章节' : '请先选择科目' }}</option>
                  <option v-for="item in chapterOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label>
                <span>知识点标签</span>
                <select v-model="form.knowledgeTags" class="app-select" :disabled="!knowledgeTagOptions.length">
                  <option value="">{{ knowledgeTagOptions.length ? '请选择知识点' : '当前章节暂无知识点可选' }}</option>
                  <option v-for="item in knowledgeTagOptions" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label>
                <span>题目类型</span>
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
              <small class="drawer-form__hint">支持直接输入 LaTeX，例如 `$f(x)=x^2$`、`$$\sum_{i=1}^n i$$`</small>
              <textarea v-model="form.stemText" class="app-textarea" rows="4" placeholder="请输入题干内容"></textarea>
            </label>

            <article class="latex-preview-card">
              <div class="latex-preview-card__head">
                <strong>题干预览</strong>
                <span>实时查看 LaTeX 显示效果</span>
              </div>
              <MathContent :content="form.stemText" empty-text="输入题干 LaTeX 后会在这里显示预览" />
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
              <span>参考答案</span>
              <textarea v-model="form.answerText" class="app-textarea" rows="2" placeholder="请输入参考答案"></textarea>
            </label>

            <label>
              <span>解析</span>
              <textarea v-model="form.analysisText" class="app-textarea" rows="4" placeholder="请输入解析"></textarea>
            </label>

            <p v-if="formError" class="form-error">{{ formError }}</p>

            <div class="drawer__footer">
              <AppButton variant="secondary" @click="closeForm">取消</AppButton>
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

import AppBadge from '../components/AppBadge.vue'
import AppButton from '../components/AppButton.vue'
import AppIcon from '../components/AppIcon.vue'
import AppInput from '../components/AppInput.vue'
import MathContent from '../components/MathContent.vue'
import { api } from '../services/api'

const pageData = ref({
  items: [],
  totalElements: 0,
  totalPages: 0,
  page: 0,
  size: 8
})
const keyword = ref('')
const formVisible = ref(false)
const editingId = ref(null)
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
  await Promise.all([fetchQuestions(0), fetchTaxonomy()])
})

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

async function fetchQuestions(page = 0) {
  pageData.value = await api.getAdminQuestions({
    keyword: keyword.value,
    page,
    size: pageData.value.size
  })
}

async function fetchTaxonomy() {
  taxonomy.value = await api.getQuestionTaxonomy()
}

function defaultForm() {
  return {
    archiveCode: '',
    yearValue: new Date().getFullYear(),
    paperName: '',
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

function resetFormState() {
  Object.assign(form, defaultForm())
  editingId.value = null
  formError.value = ''
}

function openCreate() {
  resetFormState()
  formVisible.value = true
}

async function openEdit(item) {
  resetFormState()
  const detail = await api.getQuestionDetail(item.id)
  editingId.value = item.id
  Object.assign(form, detail)
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
    const payload = {
      ...form,
      yearValue: Number(form.yearValue)
    }
    if (editingId.value) {
      await api.updateQuestion(editingId.value, payload)
    } else {
      await api.createQuestion(payload)
    }
    closeForm()
    await fetchQuestions(pageData.value.page)
  } catch (error) {
    formError.value = error.message
  } finally {
    saving.value = false
  }
}

async function removeQuestion(item) {
  const confirmed = window.confirm(`确认删除题目 ${item.archiveCode} 吗？`)
  if (!confirmed) {
    return
  }
  await api.deleteQuestion(item.id)
  fetchQuestions(Math.max(pageData.value.page - (pageData.value.items.length === 1 ? 1 : 0), 0))
}
</script>

<style scoped>
.drawer-form__hint {
  color: var(--muted);
  font-size: 12px;
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
  .latex-preview-card__head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
