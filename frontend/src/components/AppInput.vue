<template>
  <input
    :value="modelValue"
    class="app-input"
    :type="type"
    :placeholder="placeholder"
    @input="$emit('update:modelValue', $event.target.value)"
  />
</template>

<script setup>
defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  }
})

defineEmits(['update:modelValue'])
</script>
