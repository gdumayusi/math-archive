<template>
  <button :class="['app-button', `app-button--${variant}`, `app-button--${size}`, { 'app-button--block': block }]" :type="type" :disabled="disabled">
    <slot />
  </button>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary'
  },
  size: {
    type: String,
    default: 'md'
  },
  type: {
    type: String,
    default: 'button'
  },
  block: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})
</script>
