function escapeXml(value = '') {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&apos;')
}

export function buildPreviewSvg(question) {
  if (!question) {
    return ''
  }

  const snippet = escapeXml((question.stemText || '').slice(0, 72))
  const paper = escapeXml(question.paperName || '')
  const subject = escapeXml(question.subject || '')
  const chapter = escapeXml(question.chapterName || '')
  const archiveCode = escapeXml(question.archiveCode || '')
  const optionA = question.optionA ? `(A) ${escapeXml(question.optionA)}` : ''
  const optionB = question.optionB ? `(B) ${escapeXml(question.optionB)}` : ''
  const optionC = question.optionC ? `(C) ${escapeXml(question.optionC)}` : ''
  const optionD = question.optionD ? `(D) ${escapeXml(question.optionD)}` : ''

  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="720" height="520" viewBox="0 0 720 520">
      <rect width="100%" height="100%" fill="#faf9f6"/>
      <rect x="18" y="18" width="684" height="484" rx="18" fill="#fffefb" stroke="#d8dde6" stroke-width="2"/>
      <path d="M52 94H668" stroke="#e7ebf1" stroke-width="2"/>
      <path d="M52 350H668" stroke="#eef1f5" stroke-width="1.5" stroke-dasharray="7 6"/>
      <text x="52" y="62" fill="#64748b" font-size="18" font-family="Arial, Microsoft YaHei, sans-serif">档案编号: ${archiveCode} / 卷别: ${paper}</text>
      <text x="52" y="130" fill="#122033" font-size="30" font-family="Georgia, serif" font-weight="700">${snippet}</text>
      <text x="52" y="188" fill="#314055" font-size="22" font-family="Arial, Microsoft YaHei, sans-serif">${optionA}</text>
      <text x="52" y="228" fill="#314055" font-size="22" font-family="Arial, Microsoft YaHei, sans-serif">${optionB}</text>
      <text x="52" y="268" fill="#314055" font-size="22" font-family="Arial, Microsoft YaHei, sans-serif">${optionC}</text>
      <text x="52" y="308" fill="#314055" font-size="22" font-family="Arial, Microsoft YaHei, sans-serif">${optionD}</text>
      <text x="52" y="392" fill="#64748b" font-size="18" font-family="Arial, Microsoft YaHei, sans-serif">学科: ${subject}</text>
      <text x="52" y="426" fill="#64748b" font-size="18" font-family="Arial, Microsoft YaHei, sans-serif">考点: ${chapter}</text>
      <text x="52" y="468" fill="#94a3b8" font-size="16" font-family="Arial, Microsoft YaHei, sans-serif">系统扫描归档预览</text>
    </svg>
  `

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}
