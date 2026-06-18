// ========== 全局设计令牌（Design Tokens）==========
// 所有页面统一从此文件导入，禁止在各组件中重复定义 COLOR 对象

export const COLOR = {
  // 品牌色
  brand: '#2563EB',
  brandSoft: '#EFF6FF',
  brandDark: '#1D4ED8',

  // 成功/正向
  success: '#16A34A',
  successSoft: '#ECFDF5',

  // 警告
  warn: '#F59E0B',
  warnSoft: '#FFFBEB',

  // 危险
  danger: '#DC2626',
  dangerSoft: '#FEF2F2',

  // 强调色
  indigo: '#4F46E5',
  indigoSoft: '#EEF2FF',

  // 中性色
  gray1: '#F8FAFC',
  gray2: '#CBD5E1',
  gray3: '#64748B',
  gray4: '#1E293B',
}

// Tailwind 兼容的颜色映射（用于动态 style 属性）
export const TYPE_COLORS = {
  // 收入费用类型颜色
  rent: { c: '#2563EB', bg: '#EFF6FF' },
  deposit: { c: '#4F46E5', bg: '#EEF2FF' },
  water: { c: '#06B6D4', bg: '#ECFEFF' },
  electric: { c: '#F59E0B', bg: '#FFFBEB' },
  gas: { c: '#F97316', bg: '#FFF7ED' },
  broadband: { c: '#4F46E5', bg: '#EEF2FF' },
  property: { c: '#16A34A', bg: '#ECFDF5' },
  trash: { c: '#64748B', bg: '#F8FAFC' },
  other: { c: '#1E293B', bg: '#F1F5F9' },

  // 支出类型颜色
  house_rent: { c: '#2563EB', bg: '#EFF6FF' },
  house_deposit: { c: '#4F46E5', bg: '#EEF2FF' },
  repair: { c: '#DC2626', bg: '#FEF2F2' },
}
