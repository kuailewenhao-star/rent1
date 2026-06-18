import { NavLink } from 'react-router-dom'
import { COLOR } from '../data/colors.js'

const items = [
  { to: '/home', label: '首页', icon: HomeIcon },
  { to: '/archive', label: '档案', icon: ArchiveIcon },
  { to: '/bill', label: '账单', icon: BillIcon },
  { to: '/profile', label: '我的', icon: ProfileIcon }
]

export default function BottomNav() {
  return (
    <nav className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-[480px] z-40">
      <div className="bg-white/95 backdrop-blur-xl border-t border-slate-100 px-2 pt-2 pb-[max(12px,env(safe-area-inset-bottom))]">
        <div className="grid grid-cols-4 gap-1">
          {items.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => [
                'flex flex-col items-center justify-center gap-0.5 py-2 rounded-2xl transition tap-feedback',
                isActive ? 'text-brand-600' : 'text-gray-400'
              ].join(' ')}
              style={({ isActive }) => ({
                color: isActive ? COLOR.brand : undefined,
              })}
            >
              {({ isActive }) => (
                <>
                  <div className={[
                    'relative w-11 h-11 flex items-center justify-center rounded-2xl transition-all duration-300',
                    isActive ? '' : ''
                  ].join(' ')}
                  style={isActive ? { background: COLOR.brandSoft } : {}}
                  >
                    <Icon active={isActive} />
                  </div>
                  <span className={[
                    'text-[11px] font-medium transition-all duration-200',
                    isActive ? 'font-semibold tracking-tight' : ''
                  ].join(' ')}>{label}</span>
                </>
              )}
            </NavLink>
          ))}
        </div>
      </div>
    </nav>
  )
}

function IconWrap({ children, active = false }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={active ? 2.4 : 1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
      className={['w-6 h-6 transition-all duration-200', active ? 'scale-105' : ''].join(' ')}
    >
      {children}
    </svg>
  )
}

function HomeIcon({ active }) {
  return (
    <IconWrap active={active}>
      <path d="M3 11.5 12 4l9 7.5" />
      <path d="M5 10v10h14V10" />
      <path d="M10 20v-6h4v6" />
    </IconWrap>
  )
}

function ArchiveIcon({ active }) {
  return (
    <IconWrap active={active}>
      <rect x="3.5" y="3.5" width="17" height="17" rx="3" />
      <path d="M8 8.5h8" />
      <path d="M8 12.5h8" />
      <path d="M8 16.5h5" />
    </IconWrap>
  )
}

function BillIcon({ active }) {
  return (
    <IconWrap active={active}>
      <path d="M5 4.5v15l2-1.5 2 1.5 2-1.5 2 1.5 2-1.5 2 1.5v-15z" />
      <path d="M9 9h7" />
      <path d="M9 12.5h7" />
      <path d="M9 16h4" />
    </IconWrap>
  )
}

function ProfileIcon({ active }) {
  return (
    <IconWrap active={active}>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20.5c1.5-3.5 4.5-5.5 8-5.5s6.5 2 8 5.5" />
    </IconWrap>
  )
}
