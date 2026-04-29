<template>
  <div
    ref="containerRef"
    class="math-content"
    :class="{
      'math-content--empty': !content,
      'math-content--fallback': renderFailed
    }"
  >
    {{ content || emptyText }}
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  emptyText: {
    type: String,
    default: '暂无内容'
  }
})

const containerRef = ref(null)
const renderFailed = ref(false)

let mathJaxLoader

function loadMathJax() {
  if (typeof window === 'undefined') {
    return Promise.resolve(null)
  }

  if (window.MathJax?.typesetPromise) {
    return Promise.resolve(window.MathJax)
  }

  if (mathJaxLoader) {
    return mathJaxLoader
  }

  mathJaxLoader = new Promise((resolve, reject) => {
    window.MathJax = window.MathJax || {
      tex: {
        inlineMath: [['$', '$'], ['\\(', '\\)']],
        displayMath: [['$$', '$$'], ['\\[', '\\]']]
      },
      svg: {
        fontCache: 'global'
      },
      startup: {
        typeset: false
      }
    }

    const existing = document.querySelector('script[data-mathjax-loader="true"]')
    if (existing) {
      existing.addEventListener('load', () => resolve(window.MathJax), { once: true })
      existing.addEventListener('error', reject, { once: true })
      return
    }

    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-svg.js'
    script.async = true
    script.dataset.mathjaxLoader = 'true'
    script.onload = () => resolve(window.MathJax)
    script.onerror = reject
    document.head.appendChild(script)
  })

  return mathJaxLoader
}

async function renderMath() {
  await nextTick()

  const el = containerRef.value
  if (!el) {
    return
  }

  el.textContent = props.content || props.emptyText

  if (!props.content) {
    renderFailed.value = false
    return
  }

  try {
    const mathJax = await loadMathJax()
    if (!mathJax?.typesetPromise) {
      renderFailed.value = true
      return
    }
    renderFailed.value = false
    await mathJax.typesetPromise([el])
  } catch {
    renderFailed.value = true
  }
}

onMounted(renderMath)

watch(
  () => props.content,
  () => {
    renderMath()
  }
)
</script>

<style scoped>
.math-content {
  min-height: 24px;
  color: #22324d;
  white-space: pre-wrap;
  line-height: 1.85;
  word-break: break-word;
}

.math-content--empty {
  color: var(--muted);
}

.math-content--fallback {
  font-family: "Consolas", "SFMono-Regular", "Microsoft YaHei", monospace;
  font-size: 14px;
}
</style>
