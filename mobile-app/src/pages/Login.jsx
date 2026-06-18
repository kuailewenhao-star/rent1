import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { wechatLogin, setMemberType } from '../api/auth.js'

export default function Login() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleLogin = async () => {
    setLoading(true)
    setError('')
    
    try {
      // 模拟微信授权码获取（实际应接入微信SDK）
      // 这里使用模拟code进行演示
      const mockCode = 'mock_wechat_code_' + Date.now()
      
      // 调用微信登录API
      const result = await wechatLogin(mockCode, 'LANDLORD')
      
      if (result && result.token) {
        // 设置会员类型
        setMemberType(result.memberType || 'LANDLORD')
        
        // 跳转到角色选择页面或首页
        localStorage.setItem('isLoggedIn', 'true')
        navigate('/role-select')
      } else {
        // 如果没有token但返回成功（演示模式），直接跳转
        localStorage.setItem('isLoggedIn', 'true')
        navigate('/role-select')
      }
    } catch (err) {
      console.error('登录失败:', err)
      setError(err.message || '登录失败，请重试')
      // 演示模式下即使API失败也允许进入
      localStorage.setItem('isLoggedIn', 'true')
      navigate('/role-select')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-surface">
      {/* 顶部品牌区 */}
      <div className="relative brand-gradient pt-16 pb-20 px-6 overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 rounded-full bg-white/8 blur-3xl" />
        <div className="absolute bottom-0 left-0 w-48 h-48 rounded-full bg-indigo-400/20 blur-3xl" />

        <div className="relative flex flex-col items-center text-white">
          <div className="w-20 h-20 rounded-[22px] bg-white/15 backdrop-blur-md border border-white/20 flex items-center justify-center shadow-2xl mb-5">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" className="w-10 h-10 text-white">
              <path d="M4 10.5 12 4l8 6.5" />
              <path d="M6 9.5V20h12V9.5" />
              <path d="M10 20v-6h4v6" />
              <path d="M9 13h6" />
            </svg>
          </div>
          <h1 className="text-[26px] font-bold tracking-tight mb-1.5">省心做房东</h1>
          <p className="text-[15px] text-white/75 tracking-wide">一站式租赁管家</p>
        </div>
      </div>

      {/* 底部登录区 */}
      <div className="flex-1 bg-surface rounded-t-[28px] -mt-6 relative px-6 pt-10 pb-8">
        <div className="flex flex-col items-center">
          <h2 className="text-[18px] font-semibold text-ink mb-1.5">欢迎使用</h2>
          <p className="text-[13px] text-ink-soft mb-10">请通过微信授权快速登录</p>

          {error && (
            <div className="mb-4 text-[13px] text-red-500 text-center">{error}</div>
          )}

          {/* 微信登录按钮 */}
          <button
            onClick={handleLogin}
            disabled={loading}
            className="w-full h-14 rounded-2xl bg-[#07C160] hover:bg-[#06ad56] active:scale-[0.98] text-white text-[15px] font-semibold flex items-center justify-center gap-2.5 shadow-[0_8px_24px_rgba(7,193,96,0.25)] transition-all duration-200 disabled:opacity-80"
          >
            {loading ? (
              <>
                <svg className="animate-spin w-5 h-5" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
                </svg>
                微信授权中…
              </>
            ) : (
              <>
                <svg viewBox="0 0 24 24" fill="currentColor" className="w-6 h-6">
                  <path d="M9.5 4C5.36 4 2 6.91 2 10.5c0 2.08 1.13 3.92 2.88 5.1L4 18.5l2.74-1.37c.86.2 1.77.37 2.76.37.25 0 .49-.02.73-.04-.16-.47-.24-.95-.24-1.46 0-3.31 3.13-6 7-6 .25 0 .49.02.73.04C17.13 6.91 13.64 4 9.5 4zm-2.5 3.5a1 1 0 110 2 1 1 0 010-2zm5 0a1 1 0 110 2 1 1 0 010-2z" />
                  <path d="M22 16c0-2.76-2.91-5-6.5-5S9 13.24 9 16s2.91 5 6.5 5c.73 0 1.42-.11 2.05-.31L20 22l-.5-1.73c1.52-.99 2.5-2.42 2.5-4.27zm-8.5-.5a.75.75 0 110-1.5.75.75 0 010 1.5zm4 0a.75.75 0 110-1.5.75.75 0 010 1.5z" />
                </svg>
                微信一键登录
              </>
            )}
          </button>

          <p className="mt-8 text-[12px] text-ink-light text-center leading-relaxed">
            授权即代表同意
            <span className="text-brand-600 mx-0.5">用户协议</span>
            和
            <span className="text-brand-600 mx-0.5">隐私政策</span>
          </p>
        </div>

        <div className="flex-1" />

        {/* 版本信息 */}
        <div className="flex justify-center mt-auto pt-10">
          <span className="text-[11px] text-ink-light">省心做房东 v1.0.0</span>
        </div>
      </div>
    </div>
  )
}
