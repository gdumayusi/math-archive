<template>
  <svg class="hero-illustration" viewBox="0 0 520 280" aria-hidden="true">
    <defs>
      <linearGradient :id="`${variant}PaperFill`" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#ffffff" stop-opacity="0.96" />
        <stop offset="100%" stop-color="#d9e6ff" stop-opacity="0.92" />
      </linearGradient>
      <filter :id="`${variant}Shadow`" x="-20%" y="-20%" width="160%" height="160%">
        <feDropShadow dx="0" dy="14" stdDeviation="14" :flood-color="variant === 'admin' ? '#0b2f85' : '#0f3fa9'" flood-opacity="0.18" />
      </filter>
    </defs>

    <template v-if="variant === 'admin'">
      <g opacity="0.12" stroke="#dbe8ff" stroke-width="2">
        <path d="M146 52h92" />
        <path d="M146 78h92" />
        <path d="M146 104h92" />
        <path d="M416 46h28" />
        <path d="M430 32v28" />
        <path d="M410 195h56" />
        <path d="M410 213h74" />
      </g>

      <g opacity="0.15" fill="#dbe8ff" font-size="18" font-weight="600">
        <text x="392" y="108">Users</text>
        <text x="403" y="136">Audit</text>
        <text x="399" y="164">Logs</text>
      </g>

      <g :filter="`url(#${variant}Shadow)`">
        <g transform="translate(248 36) rotate(-6 76 60)">
          <rect x="0" y="0" width="156" height="120" rx="14" :fill="`url(#${variant}PaperFill)`" />
          <rect x="22" y="24" width="86" height="12" rx="6" fill="#c8d8ff" />
          <rect x="22" y="50" width="104" height="10" rx="5" fill="#d8e3ff" />
          <rect x="22" y="72" width="118" height="10" rx="5" fill="#d8e3ff" />
          <rect x="22" y="94" width="76" height="10" rx="5" fill="#d8e3ff" />
          <path d="M112 72h28v34l-28 8z" fill="#cddcff" />
          <path d="M95 102l16-20 10 12 14-26 6 42-46 8z" fill="#bcd3ff" />
        </g>

        <g transform="translate(206 158)">
          <ellipse cx="96" cy="68" rx="106" ry="26" fill="#1f4db6" fill-opacity="0.22" />
          <rect x="0" y="14" width="188" height="50" rx="12" fill="url(#adminDeskGradient)" />
          <rect x="16" y="26" width="72" height="10" rx="5" fill="#d7e5ff" fill-opacity="0.55" />
          <rect x="16" y="42" width="44" height="8" rx="4" fill="#d7e5ff" fill-opacity="0.4" />
          <rect x="118" y="24" width="54" height="28" rx="10" fill="#4f84f5" fill-opacity="0.22" />
        </g>

        <g transform="translate(226 114)">
          <rect x="0" y="30" width="112" height="76" rx="14" fill="url(#adminPanelGradient)" />
          <rect x="16" y="48" width="52" height="10" rx="5" fill="#dbe6ff" fill-opacity="0.88" />
          <rect x="16" y="66" width="78" height="8" rx="4" fill="#aecdff" fill-opacity="0.6" />
          <rect x="16" y="80" width="64" height="8" rx="4" fill="#aecdff" fill-opacity="0.45" />
          <circle cx="90" cy="68" r="12" fill="#7aa8ff" fill-opacity="0.28" />
        </g>

        <g transform="translate(374 114)">
          <rect x="0" y="36" width="60" height="70" rx="14" fill="url(#adminChartGradient)" />
          <rect x="12" y="50" width="10" height="42" rx="5" fill="#dfe9ff" fill-opacity="0.85" />
          <rect x="26" y="42" width="10" height="50" rx="5" fill="#bad2ff" fill-opacity="0.9" />
          <rect x="40" y="58" width="10" height="34" rx="5" fill="#93b8ff" fill-opacity="0.86" />
        </g>

        <g transform="translate(430 118)">
          <circle cx="22" cy="22" r="13" fill="#1c4db7" fill-opacity="0.9" />
          <circle cx="22" cy="22" r="6" fill="#8db4ff" />
          <path d="M22 35v53" stroke="#2e66de" stroke-width="7" stroke-linecap="round" />
          <path d="M22 88 5 116" stroke="#7ea8ff" stroke-width="7" stroke-linecap="round" />
          <path d="M22 88 40 116" stroke="#2a56b6" stroke-width="7" stroke-linecap="round" />
          <path d="M12 35 0 74" stroke="#133784" stroke-width="5" stroke-linecap="round" />
        </g>
      </g>

      <defs>
        <linearGradient id="adminDeskGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#2f67de" />
          <stop offset="100%" stop-color="#163e9d" />
        </linearGradient>
        <linearGradient id="adminPanelGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#386eea" />
          <stop offset="100%" stop-color="#183fa8" />
        </linearGradient>
        <linearGradient id="adminChartGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#4f84f5" />
          <stop offset="100%" stop-color="#1d49b3" />
        </linearGradient>
      </defs>
    </template>

    <template v-else>
      <g opacity="0.16" stroke="#d5e4ff" stroke-width="2">
        <path d="M188 34v76" />
        <path d="M150 72h74" />
        <path d="M166 80c10-26 24-40 42-40 19 0 34 18 48 45 9 17 16 22 28 22" fill="none" />
        <path d="M424 46h32" />
        <path d="M439 30v32" />
        <path d="M398 205l38-68 58 39-87 30Z" fill="none" />
      </g>

      <g opacity="0.15" fill="#d5e4ff" font-size="18" font-weight="600">
        <text x="130" y="58">y=sin x</text>
        <text x="412" y="40">y=tan x</text>
        <text x="425" y="70">sin x</text>
        <text x="386" y="92">lim</text>
        <text x="382" y="111">x→0</text>
        <text x="426" y="108">sin x</text>
        <text x="425" y="126">x</text>
      </g>

      <g :filter="`url(#${variant}Shadow)`">
        <g transform="translate(276 28) rotate(-7 68 54)">
          <rect x="0" y="0" width="138" height="104" rx="12" :fill="`url(#${variant}PaperFill)`" />
          <rect x="24" y="24" width="70" height="11" rx="5.5" fill="#c6d7ff" />
          <rect x="24" y="46" width="86" height="9" rx="4.5" fill="#d4e1ff" />
          <rect x="24" y="66" width="72" height="9" rx="4.5" fill="#d4e1ff" />
          <path d="M98 68l26 6v28l-26 8z" fill="#d9e4ff" />
          <path d="M84 92l16-18 8 10 13-22 4 34-41 10z" fill="#c6d7ff" />
        </g>

        <g transform="translate(250 164)">
          <ellipse cx="78" cy="58" rx="88" ry="24" fill="#2d63dd" fill-opacity="0.18" />
          <g transform="translate(0 24)">
            <path d="M24 0h112l16 14H38z" fill="url(#dashboardBookSide)" />
            <rect x="0" y="12" width="144" height="26" rx="8" fill="url(#dashboardBookBase)" />
            <rect x="10" y="14" width="50" height="5" rx="2.5" fill="#f4f8ff" fill-opacity="0.85" />
          </g>
          <g transform="translate(16 0)">
            <path d="M22 0h130l16 14H38z" fill="#4b8fff" fill-opacity="0.94" />
            <path d="M8 12h152c4.42 0 8 3.58 8 8v36c0 4.42-3.58 8-8 8H8c-4.42 0-8-3.58-8-8V20c0-4.42 3.58-8 8-8Z" fill="url(#dashboardBookTop)" />
            <rect x="22" y="18" width="24" height="40" rx="7" fill="#2d63dd" />
            <rect x="56" y="18" width="90" height="32" rx="4" fill="#1e53ca" fill-opacity="0.16" />
            <path d="M46 28h66" stroke="#ffffff" stroke-width="4" stroke-linecap="round" />
            <path d="M48 38c14-6 28-6 42 0" stroke="#ffffff" stroke-width="4" stroke-linecap="round" />
            <circle cx="124" cy="43" r="9" fill="#ffffff" fill-opacity="0.14" />
          </g>
        </g>

        <g transform="translate(420 116)">
          <circle cx="20" cy="20" r="11" fill="#1f57d7" fill-opacity="0.88" />
          <circle cx="20" cy="20" r="5" fill="#8db4ff" />
          <path d="M20 31v48" stroke="#2f6df3" stroke-width="6" stroke-linecap="round" />
          <path d="M20 79 6 106" stroke="#7fa9ff" stroke-width="6" stroke-linecap="round" />
          <path d="M20 79 35 106" stroke="#2d57b8" stroke-width="6" stroke-linecap="round" />
          <path d="M11 32 1 66" stroke="#173a9f" stroke-width="4" stroke-linecap="round" />
        </g>
      </g>

      <defs>
        <linearGradient id="dashboardBookTop" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#3f86ff" />
          <stop offset="100%" stop-color="#1f57d7" />
        </linearGradient>
        <linearGradient id="dashboardBookSide" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#285fd9" />
          <stop offset="100%" stop-color="#173a9f" />
        </linearGradient>
        <linearGradient id="dashboardBookBase" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#316fe8" />
          <stop offset="100%" stop-color="#163fbb" />
        </linearGradient>
      </defs>
    </template>
  </svg>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'dashboard'
  }
})
</script>

<style scoped>
.hero-illustration {
  width: min(100%, 500px);
  height: auto;
  display: block;
}
</style>
