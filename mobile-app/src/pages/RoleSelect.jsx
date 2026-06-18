import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function RoleSelect() {
  const navigate = useNavigate()
  const [role, setRole] = useState(() => localStorage.getItem('userRole') || null)

  const canEnter = role !== null

  const handleEnter = () => {
    if (!canEnter) return
    localStorage.setItem('userRole', role)
    // 房东去 /home，租客去 /tenant-home
    navigate(role === 'tenant' ? '/tenant-home' : '/home')
  }

  const handleBack = () => {
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col bg-surface">
      {/* 顶部品牌区 */}
      <div className="relative brand-gradient pt-14 pb-16 px-6 overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 rounded-full bg-white/8 blur-3xl" />

        <button
          onClick={handleBack}
          className="relative text-white/80 hover:text-white flex items-center gap-1.5 text-[13px] mb-4 transition-colors"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" className="w-4 h-4">
            <path d="M15 18l-6-6 6-6" />
          </svg>
          返回
        </button>

        <div className="relative flex flex-col items-center text-white">
          <div className="w-16 h-16 rounded-[18px] bg-white/15 backdrop-blur-md border border-white/20 flex items-center justify-center mb-4">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" className="w-8 h-8">
              <path d="M4 10.5 12 4l8 6.5" />
              <path d="M6 9.5V20h12V9.5" />
            </svg>
          </div>
          <h1 className="text-[22px] font-bold tracking-tight mb-1">省心做房东</h1>
          <p className="text-[13px] text-white/75 tracking-wide">一站式租赁管家</p>
        </div>
      </div>

      {/* 身份选择 */}
      <div className="flex-1 bg-surface rounded-t-[28px] -mt-5 relative px-6 pt-8 pb-8">
        <div className="flex flex-col items-center mb-6">
          <h2 className="text-[17px] font-semibold text-ink mb-1.5">请选择您的登录身份</h2>
          <p className="text-[12.5px] text-ink-soft">同一微信号可切换房东 / 租客双重身份</p>
        </div>

        <div className="space-y-3.5 mb-8">
          {/* 房东卡片 */}
          <button
            onClick={() => setRole('landlord')}
            className={[
              'w-full p-5 rounded-[20px] border-2 text-left flex items-center gap-4 transition-all duration-200 tap-feedback',
              role === 'landlord'
                ? 'bg-brand-50 border-brand-500 shadow-[0_8px_24px_rgba(37,99,235,0.12)]'
                : 'bg-surface border-slate-100 shadow-card hover:border-brand-200'
            ].join(' ')}
          >
            <div className={[
              'w-12 h-12 rounded-2xl flex items-center justify-center flex-shrink-0 transition-colors',
              role === 'landlord' ? 'bg-brand-500 text-white' : 'bg-brand-50 text-brand-600'
            ].join(' ')}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
                <path d="M4 10.5 12 4l8 6.5" />
                <path d="M6 10v10h12V10" />
                <path d="M10 20v-6h4v6" />
              </svg>
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-0.5">
                <h3 className="text-[16px] font-semibold text-ink">我是房东</h3>
                {role === 'landlord' && (
                  <span className="w-5 h-5 rounded-full bg-brand-500 flex items-center justify-center">
                    <svg viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth={3} strokeLinecap="round" strokeLinejoin="round" className="w-3 h-3">
                      <path d="M5 12l5 5L20 7" />
                    </svg>
                  </span>
                )}
              </div>
              <p className="text-[12.5px] text-ink-soft">管理房源、租客、账单</p>
            </div>
          </button>

          {/* 租客卡片 */}
          <button
            onClick={() => setRole('tenant')}
            className={[
              'w-full p-5 rounded-[20px] border-2 text-left flex items-center gap-4 transition-all duration-200 tap-feedback',
              role === 'tenant'
                ? 'bg-brand-50 border-brand-500 shadow-[0_8px_24px_rgba(37,99,235,0.12)]'
                : 'bg-surface border-slate-100 shadow-card hover:border-brand-200'
            ].join(' ')}
          >
            <div className={[
              'w-12 h-12 rounded-2xl flex items-center justify-center flex-shrink-0 transition-colors',
              role === 'tenant' ? 'bg-brand-500 text-white' : 'bg-brand-50 text-brand-600'
            ].join(' ')}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
                <circle cx="12" cy="8" r="4" />
                <path d="M4 20.5c1.5-3.5 4.5-5.5 8-5.5s6.5 2 8 5.5" />
              </svg>
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-0.5">
                <h3 className="text-[16px] font-semibold text-ink">我是租客</h3>
                {role === 'tenant' && (
                  <span className="w-5 h-5 rounded-full bg-brand-500 flex items-center justify-center">
                    <svg viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth={3} strokeLinecap="round" strokeLinejoin="round" className="w-3 h-3">
                      <path d="M5 12l5 5L20 7" />
                    </svg>
                  </span>
                )}
              </div>
              <p className="text-[12.5px] text-ink-soft">查看合约、账单、缴费</p>
            </div>
          </button>
        </div>

        {/* 进入按钮 */}
        <button
          onClick={handleEnter}
          disabled={!canEnter}
          className={[
            'w-full h-14 rounded-2xl text-[15px] font-semibold transition-all duration-200 flex items-center justify-center',
            canEnter
              ? 'bg-brand-600 hover:bg-brand-700 text-white shadow-[0_8px_24px_rgba(37,99,235,0.25)] active:scale-[0.98]'
              : 'bg-slate-100 text-ink-light cursor-not-allowed'
          ].join(' ')}
        >
          确认进入
        </button>

        <p className="mt-5 text-center text-[12px] text-ink-light">
          <span className="text-brand-600">用户协议</span>
          <span className="mx-1">·</span>
          <span className="text-brand-600">隐私政策</span>
        </p>
      </div>
    </div>
  )
}
