import { Outlet, useLocation } from 'react-router-dom'
import BottomNav from './BottomNav.jsx'

export default function AppLayout() {
  const location = useLocation()
  return (
    <div key={location.pathname} className="min-h-screen pb-[96px]">
      <Outlet />
      <BottomNav />
    </div>
  )
}
