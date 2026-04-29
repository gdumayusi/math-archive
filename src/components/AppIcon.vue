<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="strokeWidth" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <template v-if="name === 'brandBook'">
      <path d="M4.7 6.2C4.7 5.26 5.46 4.5 6.4 4.5H10.8C11.76 4.5 12.69 4.84 13.42 5.47L13.7 5.71C14 5.97 14.45 5.97 14.75 5.71L15.03 5.47C15.76 4.84 16.69 4.5 17.65 4.5H18.1C19.04 4.5 19.8 5.26 19.8 6.2V17.4C19.8 18.34 19.04 19.1 18.1 19.1H16.95C15.98 19.1 15.04 19.43 14.3 20.06L13.98 20.33C13.74 20.54 13.39 20.54 13.15 20.33L12.83 20.06C12.09 19.43 11.15 19.1 10.18 19.1H6.4C5.46 19.1 4.7 18.34 4.7 17.4V6.2Z" fill="currentColor" fill-opacity="0.1" stroke="none" />
      <path d="M5.2 6.2C5.2 5.54 5.74 5 6.4 5H10.8C11.64 5 12.45 5.3 13.08 5.84L13.36 6.08C13.85 6.5 14.58 6.5 15.08 6.08L15.36 5.84C15.99 5.3 16.8 5 17.65 5H18.1C18.76 5 19.3 5.54 19.3 6.2V16.9C19.3 17.56 18.76 18.1 18.1 18.1H16.95C15.86 18.1 14.8 18.47 13.97 19.18L13.7 19.42L13.43 19.18C12.6 18.47 11.54 18.1 10.45 18.1H6.4C5.74 18.1 5.2 17.56 5.2 16.9V6.2Z" fill="white" stroke="none" />
      <path d="M6.4 5H10.8C11.64 5 12.45 5.3 13.08 5.84L13.36 6.08C13.85 6.5 14.58 6.5 15.08 6.08L15.36 5.84C15.99 5.3 16.8 5 17.65 5H18.1C18.76 5 19.3 5.54 19.3 6.2V16.9C19.3 17.56 18.76 18.1 18.1 18.1H16.95C15.86 18.1 14.8 18.47 13.97 19.18L13.7 19.42L13.43 19.18C12.6 18.47 11.54 18.1 10.45 18.1H6.4C5.74 18.1 5.2 17.56 5.2 16.9V6.2C5.2 5.54 5.74 5 6.4 5Z" />
      <path d="M13.7 6.1V19.2" />
      <path d="M8.1 8.3H10.9" stroke-opacity="0.55" />
      <path d="M16 8.3H17.3" stroke-opacity="0.35" />
      <path d="M16 10.1H17.3" stroke-opacity="0.35" />
      <path d="M16 11.9H17.3" stroke-opacity="0.35" />
      <path d="M10.9 9.1C10.52 8.73 9.91 8.53 9.25 8.63C8.47 8.76 7.85 9.33 7.72 10.01C7.61 10.58 7.84 11.02 8.32 11.33L10.23 12.56C10.71 12.87 10.94 13.31 10.83 13.88C10.7 14.56 10.08 15.13 9.3 15.26C8.64 15.36 8.03 15.16 7.65 14.79" />
      <path d="M8.15 8.88H10.42" />
      <path d="M8.15 15.04H10.42" />
    </template>
    <template v-else-if="name === 'book'">
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
      <path d="M4 19.5A2.5 2.5 0 0 0 6.5 22H20V4H6.5A2.5 2.5 0 0 0 4 6.5v13z" />
    </template>
    <template v-else-if="name === 'layout'">
      <rect x="3" y="3" width="18" height="18" rx="2" />
      <path d="M3 9h18M9 21V9" />
    </template>
    <template v-else-if="name === 'adminShield'">
      <path d="M12 3.2 18.2 5.8v5.56c0 4.08-2.48 7.82-6.2 9.4-3.72-1.58-6.2-5.32-6.2-9.4V5.8L12 3.2Z" fill="currentColor" fill-opacity="0.1" stroke="none" />
      <path d="M12 3.8 17.5 6v5.16c0 3.74-2.2 7.15-5.5 8.63-3.3-1.48-5.5-4.89-5.5-8.63V6L12 3.8Z" />
      <rect x="8.15" y="8.2" width="7.7" height="6.4" rx="1.35" fill="white" stroke="none" />
      <path d="M8.15 10.2h7.7" />
      <path d="M12 14.55V10.2" />
      <path d="M9.8 16.9h4.4" stroke-opacity="0.55" />
    </template>
    <template v-else-if="name === 'home'">
      <path d="M3 10.5 12 3l9 7.5" />
      <path d="M5 9.5V21h14V9.5" />
    </template>
    <template v-else-if="name === 'fileText'">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <path d="M14 2v6h6" />
      <path d="M16 13H8M16 17H8M10 9H8" />
    </template>
    <template v-else-if="name === 'users'">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </template>
    <template v-else-if="name === 'search'">
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.35-4.35" />
    </template>
    <template v-else-if="name === 'filter'">
      <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" />
    </template>
    <template v-else-if="name === 'tag'">
      <path d="m20.59 13.41-7.18 7.18a2 2 0 0 1-2.82 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82Z" />
      <circle cx="7" cy="7" r="1.5" />
    </template>
    <template v-else-if="name === 'user'">
      <path d="M20 21a8 8 0 0 0-16 0" />
      <circle cx="12" cy="7" r="4" />
    </template>
    <template v-else-if="name === 'settings'">
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06A1.65 1.65 0 0 0 15 19.4a1.65 1.65 0 0 0-1 .6 1.65 1.65 0 0 0-.33 1V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-.33-1 1.65 1.65 0 0 0-1-.6 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.6 15a1.65 1.65 0 0 0-.6-1 1.65 1.65 0 0 0-1-.33H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1-.33 1.65 1.65 0 0 0 .6-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.6c.4-.05.77-.26 1-.6.21-.28.32-.62.33-.97V3a2 2 0 0 1 4 0v.09c.01.35.12.69.33.97.23.34.6.55 1 .6a1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9c.34.23.69.34 1.09.33H21a2 2 0 0 1 0 4h-.09c-.4-.01-.75.1-1.09.33-.34.23-.55.6-.6 1Z" />
    </template>
    <template v-else-if="name === 'bell'">
      <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
      <path d="M10.3 21a2 2 0 0 0 3.4 0" />
    </template>
    <template v-else-if="name === 'menu'">
      <path d="M4 7h16M4 12h16M4 17h16" />
    </template>
    <template v-else-if="name === 'star'">
      <path d="m12 2.5 2.95 5.98 6.6.96-4.77 4.65 1.13 6.57L12 17.55 6.09 20.66l1.13-6.57-4.77-4.65 6.6-.96L12 2.5Z" />
    </template>
    <template v-else-if="name === 'clock'">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 3" />
    </template>
    <template v-else-if="name === 'calendar'">
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M16 3v4M8 3v4M3 10h18" />
    </template>
    <template v-else-if="name === 'eye'">
      <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z" />
      <circle cx="12" cy="12" r="3" />
    </template>
    <template v-else-if="name === 'bookmark'">
      <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
    </template>
    <template v-else-if="name === 'alert'">
      <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3l-8.47-14.14a2 2 0 0 0-3.42 0z" />
      <path d="M12 9v4" />
      <path d="M12 17h.01" />
    </template>
    <template v-else-if="name === 'folder'">
      <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
    </template>
    <template v-else-if="name === 'upload'">
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
      <path d="m17 8-5-5-5 5" />
      <path d="M12 3v12" />
    </template>
    <template v-else-if="name === 'barChart'">
      <path d="M12 20V10M18 20V4M6 20v-4" />
    </template>
    <template v-else-if="name === 'trash'">
      <path d="M3 6h18" />
      <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" />
    </template>
    <template v-else-if="name === 'edit'">
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.12 2.12 0 1 1 3 3L7 19l-4 1 1-4Z" />
    </template>
    <template v-else-if="name === 'database'">
      <ellipse cx="12" cy="5" rx="8" ry="3" />
      <path d="M4 5v6c0 1.66 3.58 3 8 3s8-1.34 8-3V5" />
      <path d="M4 11v6c0 1.66 3.58 3 8 3s8-1.34 8-3v-6" />
    </template>
    <template v-else-if="name === 'chevronLeft'">
      <path d="m15 18-6-6 6-6" />
    </template>
    <template v-else-if="name === 'chevronRight'">
      <path d="m9 18 6-6-6-6" />
    </template>
    <template v-else-if="name === 'zoomIn'">
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.35-4.35M11 8v6M8 11h6" />
    </template>
    <template v-else-if="name === 'zoomOut'">
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.35-4.35M8 11h6" />
    </template>
    <template v-else-if="name === 'close'">
      <path d="M18 6 6 18M6 6l12 12" />
    </template>
    <template v-else-if="name === 'logout'">
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
      <path d="m16 17 5-5-5-5" />
      <path d="M21 12H9" />
    </template>
  </svg>
</template>

<script setup>
defineProps({
  name: {
    type: String,
    required: true
  },
  size: {
    type: Number,
    default: 18
  },
  strokeWidth: {
    type: Number,
    default: 1.8
  }
})
</script>
