export const avatarOptions = [
  { key: 'classic', label: '经典蓝', className: 'avatar-theme--classic' },
  { key: 'ocean', label: '海洋青', className: 'avatar-theme--ocean' },
  { key: 'sunrise', label: '日出橙', className: 'avatar-theme--sunrise' },
  { key: 'forest', label: '森林绿', className: 'avatar-theme--forest' },
  { key: 'plum', label: '梅子红', className: 'avatar-theme--plum' },
  { key: 'night', label: '夜幕灰', className: 'avatar-theme--night' }
]

export function getAvatarTheme(key) {
  return avatarOptions.find((item) => item.key === key) || avatarOptions[0]
}

export function getInitial(text = '') {
  return (text || '数').trim().charAt(0) || '数'
}
