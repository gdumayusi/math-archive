<template>
  <span :class="['app-badge', `app-badge--${tone}`]">
    <slot />
  </span>
</template>

<script setup>
defineProps({
  tone: {
    type: String,
    default: 'neutral'
  }
})
</script>
