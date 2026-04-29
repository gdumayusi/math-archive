<template>
  <main class="auth-page">
    <div class="auth-page__backdrop auth-page__backdrop--top"></div>
    <div class="auth-page__backdrop auth-page__backdrop--bottom"></div>
    <div class="auth-page__wave auth-page__wave--one"></div>
    <div class="auth-page__wave auth-page__wave--two"></div>
    <div class="auth-page__wave auth-page__wave--three"></div>

    <div class="auth-page__dots auth-page__dots--top"></div>
    <div class="auth-page__dots auth-page__dots--bottom"></div>

    <div class="auth-page__formula auth-page__formula--integral">∫<sub>a</sub><sup>b</sup> f(x)dx</div>
    <div class="auth-page__formula auth-page__formula--limit">lim sinx/x = 1</div>
    <div class="auth-page__formula auth-page__formula--curve">y = sinx</div>

    <svg class="auth-page__sketch auth-page__sketch--left" viewBox="0 0 320 220" fill="none" aria-hidden="true">
      <path d="M24 170H286" />
      <path d="M84 24V208" />
      <path d="M24 160C68 148 90 130 112 108C130 90 144 82 166 82C190 82 212 96 236 128C254 151 270 160 286 160" />
      <path d="M48 132L48 186" />
      <path d="M124 102L124 182" />
      <path d="M166 82L166 182" />
      <path d="M210 112L210 182" />
      <path d="M260 158L260 182" />
      <circle cx="48" cy="132" r="3.5" fill="currentColor" stroke="none" />
      <circle cx="124" cy="102" r="3.5" fill="currentColor" stroke="none" />
      <circle cx="166" cy="82" r="3.5" fill="currentColor" stroke="none" />
      <circle cx="210" cy="112" r="3.5" fill="currentColor" stroke="none" />
      <circle cx="260" cy="158" r="3.5" fill="currentColor" stroke="none" />
    </svg>

    <svg class="auth-page__sketch auth-page__sketch--right" viewBox="0 0 260 240" fill="none" aria-hidden="true">
      <path d="M130 28L44 174L148 194L216 164L130 28Z" />
      <path d="M130 28L130 194" />
      <path d="M44 174L130 132L216 164" />
      <path d="M130 132L148 194" />
      <path d="M130 28L148 194" stroke-dasharray="7 7" />
      <path d="M130 28L216 164" stroke-dasharray="7 7" />
      <path d="M130 28L44 174" />
      <text x="30" y="187">A</text>
      <text x="224" y="168">C</text>
      <text x="126" y="19">B</text>
    </svg>

    <section class="auth-shell">
      <header class="auth-header">
        <div class="auth-logo" aria-hidden="true">
          <svg viewBox="0 0 92 76" fill="none">
            <path d="M14 14H40C50 14 58 16 64 20C70 16 78 14 88 14V60C78 60 70 62 64 66C58 62 50 60 40 60H14V14Z" fill="#EAF2FF" stroke="#1A5FDB" stroke-width="3.2" stroke-linejoin="round" />
            <path d="M8 20H14V66H40C49 66 57 68 64 72C71 68 79 66 88 66V20H92V70H86C78 70 71 72 64 76C57 72 50 70 42 70H8V20Z" fill="#8CC1FF" fill-opacity="0.5" />
            <path d="M64 20V66" stroke="#1A5FDB" stroke-width="3.2" stroke-linecap="round" />
            <path d="M35 28H52L42 39L52 50H35" stroke="#2452B8" stroke-width="4.2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <h1>项目考研数学真题分类管理系统</h1>
        <p>真题分类管理 · 高效学习备考 · 助力考研成功</p>
      </header>

      <section class="auth-card">
        <div class="auth-tabs" role="tablist" aria-label="登录注册切换">
          <button
            class="auth-tab"
            :class="{ 'auth-tab--active': activePanel === 'login' }"
            type="button"
            @click="switchPanel('login')"
          >
            用户登录
          </button>
          <button
            class="auth-tab"
            :class="{ 'auth-tab--active': activePanel === 'register' }"
            type="button"
            @click="switchPanel('register')"
          >
            用户注册
          </button>
        </div>

        <section v-if="activePanel === 'login'" class="auth-panel">
          <div class="auth-panel__heading">
            <h2>欢迎回来</h2>
            <p>输入账号和密码，继续你的考研复习</p>
          </div>

          <form class="auth-form" @submit.prevent="submitLogin">
            <label class="auth-field">
              <span class="auth-field__icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M18 20V18.8C18 16.149 15.314 14 12 14C8.686 14 6 16.149 6 18.8V20" />
                  <circle cx="12" cy="8" r="4" />
                </svg>
              </span>
              <input v-model.trim="loginForm.username" type="text" placeholder="请输入用户名" autocomplete="username" />
            </label>

            <label class="auth-field">
              <span class="auth-field__icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none">
                  <rect x="6" y="10" width="12" height="10" rx="2" />
                  <path d="M8 10V8A4 4 0 0 1 16 8V10" />
                </svg>
              </span>
              <input
                v-model="loginForm.password"
                :type="showLoginPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                autocomplete="current-password"
              />
              <button
                class="auth-field__action"
                type="button"
                :aria-label="showLoginPassword ? '隐藏密码' : '显示密码'"
                @click="showLoginPassword = !showLoginPassword"
              >
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M2 12C4.6 7.5 8 5.25 12 5.25C16 5.25 19.4 7.5 22 12C19.4 16.5 16 18.75 12 18.75C8 18.75 4.6 16.5 2 12Z" />
                  <circle cx="12" cy="12" r="3.1" />
                </svg>
              </button>
            </label>

            <div class="auth-form__meta">
              <label class="auth-check">
                <input v-model="loginForm.remember" type="checkbox" />
                <span>记住我</span>
              </label>
              <a href="#" @click.prevent>忘记密码？</a>
            </div>

            <p v-if="loginError" class="auth-error">{{ loginError }}</p>

            <button class="auth-submit" type="submit" :disabled="loadingLogin">
              {{ loadingLogin ? '登录中...' : '登录' }}
            </button>
          </form>

          <p class="auth-switch-link">
            还没有账号？
            <button type="button" @click="switchPanel('register')">立即注册</button>
          </p>
        </section>

        <section v-else class="auth-panel">
          <div class="auth-panel__heading">
            <h2>创建账号</h2>
            <p>完成注册后即可进入系统开始使用</p>
          </div>

          <form class="auth-form" @submit.prevent="submitRegister">
            <label class="auth-field">
              <span class="auth-field__icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M18 20V18.8C18 16.149 15.314 14 12 14C8.686 14 6 16.149 6 18.8V20" />
                  <circle cx="12" cy="8" r="4" />
                </svg>
              </span>
              <input v-model.trim="registerForm.username" type="text" placeholder="请输入用户名" autocomplete="username" />
            </label>

            <label class="auth-field">
              <span class="auth-field__icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none">
                  <rect x="6" y="10" width="12" height="10" rx="2" />
                  <path d="M8 10V8A4 4 0 0 1 16 8V10" />
                </svg>
              </span>
              <input v-model="registerForm.password" type="password" placeholder="请输入密码" autocomplete="new-password" />
            </label>

            <label class="auth-field">
              <span class="auth-field__icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none">
                  <rect x="6" y="10" width="12" height="10" rx="2" />
                  <path d="M8 10V8A4 4 0 0 1 16 8V10" />
                </svg>
              </span>
              <input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" autocomplete="new-password" />
            </label>

            <label class="auth-check auth-check--agreement">
              <input v-model="registerForm.agreed" type="checkbox" />
              <span>
                我已阅读并同意
                <a href="#" @click.prevent>《用户协议》</a>
                和
                <a href="#" @click.prevent>《隐私政策》</a>
              </span>
            </label>

            <p v-if="registerError" class="auth-error">{{ registerError }}</p>

            <button class="auth-submit" type="submit" :disabled="loadingRegister">
              {{ loadingRegister ? '注册中...' : '注册' }}
            </button>

            <p class="auth-switch-link">
              已有账号？
              <button type="button" @click="switchPanel('login')">立即登录</button>
            </p>
          </form>
        </section>
      </section>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { api } from '../services/api'
import { setSession } from '../stores/session'

const router = useRouter()
const route = useRoute()

const loadingLogin = ref(false)
const loadingRegister = ref(false)
const showLoginPassword = ref(false)
const loginError = ref('')
const registerError = ref('')
const activePanel = ref(route.query.mode === 'register' ? 'register' : 'login')

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  agreed: false
})

function resolveHomePath(role) {
  return role === 'ADMIN' ? '/admin/dashboard' : '/dashboard'
}

watch(
  () => route.query.mode,
  (mode) => {
    activePanel.value = mode === 'register' ? 'register' : 'login'
  }
)

function switchPanel(panel) {
  activePanel.value = panel
  loginError.value = ''
  registerError.value = ''
  router.replace({
    path: '/login',
    query: panel === 'register' ? { mode: 'register' } : {}
  })
}

async function submitLogin() {
  loginError.value = ''
  loadingLogin.value = true
  try {
    const response = await api.login({
      username: loginForm.username.trim(),
      password: loginForm.password
    })
    setSession(response)
    router.push(resolveHomePath(response.role))
  } catch (error) {
    loginError.value = error.message
  } finally {
    loadingLogin.value = false
  }
}

async function submitRegister() {
  registerError.value = ''

  if (!registerForm.agreed) {
    registerError.value = '请先阅读并同意用户协议和隐私政策'
    return
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    registerError.value = '两次输入的密码不一致'
    return
  }

  loadingRegister.value = true
  try {
    const response = await api.register({
      username: registerForm.username.trim(),
      displayName: registerForm.username.trim(),
      email: '',
      password: registerForm.password
    })
    setSession(response)
    router.push(resolveHomePath(response.role))
  } catch (error) {
    registerError.value = error.message
  } finally {
    loadingRegister.value = false
  }
}

</script>

<style scoped>
.auth-page {
  --auth-blue: #1f5fd7;
  --auth-blue-deep: #0f377f;
  --auth-line: rgba(76, 130, 223, 0.2);
  --auth-surface: rgba(255, 255, 255, 0.94);
  --auth-text: #0f1e3d;
  --auth-muted: #66789a;
  --auth-decor: rgba(80, 133, 231, 0.32);

  position: relative;
  min-height: 100vh;
  overflow: hidden;
  padding: 42px 24px 68px;
  background:
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.96), rgba(239, 246, 255, 0.84) 52%, rgba(225, 236, 255, 0.96) 100%),
    linear-gradient(180deg, #f9fbff 0%, #e8f0ff 100%);
}

.auth-page__backdrop,
.auth-page__wave,
.auth-page__dots,
.auth-page__formula,
.auth-page__sketch {
  pointer-events: none;
  position: absolute;
}

.auth-page__backdrop {
  border-radius: 999px;
  filter: blur(2px);
}

.auth-page__backdrop--top {
  top: -88px;
  right: -66px;
  width: 224px;
  height: 174px;
  background: linear-gradient(180deg, rgba(170, 197, 255, 0.55), rgba(145, 182, 255, 0.32));
}

.auth-page__backdrop--bottom {
  left: -112px;
  bottom: -82px;
  width: 364px;
  height: 208px;
  background: linear-gradient(180deg, rgba(175, 202, 255, 0.4), rgba(136, 176, 255, 0.22));
}

.auth-page__wave {
  left: -4%;
  width: 112%;
  border-radius: 50%;
  background-repeat: no-repeat;
  background-size: cover;
}

.auth-page__wave--one {
  bottom: -110px;
  height: 290px;
  background: linear-gradient(180deg, rgba(138, 181, 255, 0.24), rgba(103, 156, 255, 0.32));
  opacity: 0.75;
}

.auth-page__wave--two {
  bottom: -136px;
  height: 250px;
  background: linear-gradient(180deg, rgba(120, 167, 255, 0.38), rgba(67, 129, 241, 0.48));
  transform: rotate(-2.2deg);
}

.auth-page__wave--three {
  bottom: -174px;
  height: 290px;
  background: linear-gradient(180deg, rgba(108, 157, 248, 0.58), rgba(43, 107, 225, 0.82));
  transform: rotate(3deg);
}

.auth-page__dots {
  width: 114px;
  height: 92px;
  opacity: 0.8;
  background-image: radial-gradient(circle, rgba(113, 160, 245, 0.46) 0, rgba(113, 160, 245, 0.46) 3px, transparent 3.4px);
  background-size: 28px 28px;
}

.auth-page__dots--top {
  top: 154px;
  right: 56px;
}

.auth-page__dots--bottom {
  left: 18px;
  bottom: 18px;
}

.auth-page__formula {
  color: var(--auth-decor);
  font-family: "Cambria Math", "Times New Roman", serif;
  font-weight: 500;
}

.auth-page__formula--integral {
  top: 214px;
  left: 34px;
  font-size: 29px;
}

.auth-page__formula--limit {
  top: 340px;
  right: 38px;
  font-size: 34px;
}

.auth-page__formula--curve {
  left: 62px;
  bottom: 182px;
  font-size: 28px;
}

.auth-page__sketch {
  color: rgba(113, 160, 245, 0.55);
  stroke: currentColor;
  stroke-width: 1.7;
}

.auth-page__sketch text {
  fill: currentColor;
  font-size: 20px;
  stroke: none;
}

.auth-page__sketch--left {
  left: 10px;
  bottom: 264px;
  width: 208px;
}

.auth-page__sketch--right {
  right: 12px;
  bottom: 176px;
  width: 194px;
}

.auth-shell {
  position: relative;
  z-index: 1;
  width: min(1110px, 100%);
  margin: 0 auto;
}

.auth-header {
  text-align: center;
  color: var(--auth-text);
}

.auth-logo {
  width: 94px;
  margin: 0 auto 8px;
}

.auth-logo svg {
  display: block;
  width: 100%;
  height: auto;
}

.auth-header h1 {
  margin: 0;
  font-size: clamp(34px, 4vw, 58px);
  line-height: 1.18;
  font-weight: 800;
  color: var(--auth-blue-deep);
}

.auth-header p {
  margin: 22px 0 0;
  font-size: 23px;
  color: #5a6e95;
}

.auth-card {
  width: min(620px, 100%);
  margin: 42px auto 0;
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(146, 184, 244, 0.34);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 26px 48px rgba(84, 119, 188, 0.14);
  backdrop-filter: blur(8px);
}

.auth-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: rgba(241, 247, 255, 0.88);
}

.auth-tab {
  position: relative;
  border: 0;
  padding: 28px 20px 24px;
  background: transparent;
  color: #5a6f96;
  font-size: 28px;
  font-weight: 800;
  transition: color 0.2s ease, background-color 0.2s ease;
}

.auth-tab::after {
  content: "";
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 56px;
  height: 4px;
  border-radius: 999px;
  background: transparent;
  transform: translateX(-50%);
}

.auth-tab--active {
  color: #11151f;
  background: rgba(255, 255, 255, 0.7);
}

.auth-tab--active::after {
  background: var(--auth-blue);
}

.auth-panel {
  min-height: 560px;
  padding: 44px 56px 34px;
  background: var(--auth-surface);
}

.auth-panel__heading {
  text-align: center;
}

.auth-panel__heading h2 {
  margin: 0;
  font-size: 34px;
  line-height: 1.2;
  font-weight: 800;
  color: #11151f;
}

.auth-panel__heading p {
  margin: 16px 0 0;
  color: #627597;
  font-size: 17px;
}

.auth-form {
  margin-top: 38px;
}

.auth-field {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 58px;
  border: 1px solid rgba(177, 197, 227, 0.76);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.2);
}

.auth-field + .auth-field,
.auth-field + .auth-check--agreement {
  margin-top: 18px;
}

.auth-field:focus-within {
  border-color: rgba(48, 106, 214, 0.7);
  box-shadow: 0 0 0 4px rgba(55, 120, 235, 0.1);
}

.auth-field input {
  width: 100%;
  height: 100%;
  padding: 0 20px 0 58px;
  border: 0;
  outline: 0;
  color: var(--auth-text);
  background: transparent;
  font-size: 17px;
}

.auth-field input::placeholder {
  color: #8d9ebb;
}

.auth-field__icon,
.auth-field__action {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #5d6f90;
}

.auth-field__icon {
  left: 18px;
  width: 24px;
  height: 24px;
}

.auth-field__icon svg,
.auth-field__action svg {
  width: 100%;
  height: 100%;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.auth-field__action {
  right: 16px;
  width: 24px;
  height: 24px;
  border: 0;
  padding: 0;
  background: transparent;
}

.auth-form__meta,
.auth-check,
.auth-switch-link {
  display: flex;
  align-items: center;
}

.auth-form__meta {
  justify-content: space-between;
  gap: 16px;
  margin-top: 20px;
  font-size: 17px;
  color: #44577c;
}

.auth-check {
  gap: 10px;
  color: #44577c;
  font-size: 17px;
}

.auth-check input {
  width: 20px;
  height: 20px;
  margin: 0;
  accent-color: var(--auth-blue);
}

.auth-check a,
.auth-form__meta a {
  color: var(--auth-blue);
  text-decoration: none;
  font-weight: 600;
}

.auth-check--agreement {
  align-items: flex-start;
  margin-top: 18px;
  font-size: 15px;
  line-height: 1.65;
}

.auth-error {
  margin: 12px 0 0;
  color: #ca3c4c;
  font-size: 14px;
}

.auth-submit {
  width: 100%;
  height: 60px;
  margin-top: 24px;
  border: 0;
  border-radius: 9px;
  background: linear-gradient(180deg, #2a70ed, #1f5fd7);
  color: #fff;
  font-size: 21px;
  font-weight: 800;
  box-shadow: 0 14px 28px rgba(38, 98, 212, 0.18);
}

.auth-submit:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.auth-switch-link {
  justify-content: center;
  gap: 8px;
  margin: 24px 0 0;
  color: #4d5f80;
  font-size: 17px;
}

.auth-switch-link button {
  border: 0;
  padding: 0;
  background: transparent;
  color: var(--auth-blue);
  font-size: inherit;
  font-weight: 700;
}

@media (max-width: 720px) {
  .auth-page {
    padding: 26px 16px 48px;
  }

  .auth-header h1 {
    font-size: 30px;
  }

  .auth-header p {
    margin-top: 14px;
    font-size: 16px;
  }

  .auth-card {
    margin-top: 28px;
  }

  .auth-tab {
    padding: 22px 16px 18px;
    font-size: 22px;
  }

  .auth-panel {
    min-height: auto;
    padding: 34px 18px 28px;
  }

  .auth-panel__heading h2 {
    font-size: 28px;
  }

  .auth-panel__heading p,
  .auth-field input,
  .auth-form__meta,
  .auth-check,
  .auth-switch-link {
    font-size: 15px;
  }

  .auth-form {
    margin-top: 30px;
  }

  .auth-submit {
    height: 54px;
    font-size: 19px;
  }

  .auth-page__formula,
  .auth-page__sketch,
  .auth-page__dots {
    opacity: 0.45;
  }
}
</style>
