import { Routes, Route, Navigate } from 'react-router-dom'
import { DataProvider } from './data/context.jsx'
import AuthGuard from './components/AuthGuard.jsx'
import AppLayout from './components/AppLayout.jsx'
import Login from './pages/Login.jsx'
import RoleSelect from './pages/RoleSelect.jsx'
import Home from './pages/Home.jsx'
import Archive from './pages/Archive.jsx'
import Bill from './pages/Bill.jsx'
import Profile from './pages/Profile.jsx'
import Contracts from './pages/Contracts.jsx'
import Messages from './pages/Messages.jsx'
import HouseDetail from './pages/HouseDetail.jsx'
import RoomDetail from './pages/RoomDetail.jsx'
import BillDetail from './pages/BillDetail.jsx'
import ContractDetail from './pages/ContractDetail.jsx'
import TenantDetail from './pages/TenantDetail.jsx'
import TenantHome from './pages/TenantHome.jsx'
import TenantBills from './pages/TenantBills.jsx'
import TenantProfile from './pages/TenantProfile.jsx'
import CreateContract from './pages/CreateContract.jsx'
import EditRoom from './pages/EditRoom.jsx'
import EditHouse from './pages/EditHouse.jsx'
import ExpenseForm from './pages/ExpenseForm.jsx'
import MoveIn from './pages/MoveIn.jsx'
import MoveOut from './pages/MoveOut.jsx'

export default function App() {
  return (
    <DataProvider>
      <div className="min-h-screen w-full bg-slate-50">
        <div className="w-full max-w-[480px] min-h-screen mx-auto bg-slate-50 relative shadow-[0_0_60px_rgba(15,23,42,0.08)] overflow-hidden">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/role-select" element={<RoleSelect />} />
            <Route path="/" element={<AuthGuard><AppLayout /></AuthGuard>}>
              <Route index element={<Navigate to="/home" replace />} />
              <Route path="home" element={<Home />} />
              <Route path="tenant-home" element={<TenantHome />} />
              <Route path="archive" element={<Archive />} />
              <Route path="bill" element={<Bill />} />
              <Route path="bill/:id" element={<BillDetail />} />
              <Route path="contracts" element={<Contracts />} />
              <Route path="contract-detail/:id" element={<ContractDetail />} />
              <Route path="messages" element={<Messages />} />
              <Route path="house/:id" element={<HouseDetail />} />
              <Route path="room/:id" element={<RoomDetail />} />
              <Route path="tenant/:id" element={<TenantDetail />} />
              <Route path="tenant-bills" element={<TenantBills />} />
              <Route path="tenant-profile" element={<TenantProfile />} />
              <Route path="profile" element={<Profile />} />
              <Route path="create-contract" element={<CreateContract />} />
              <Route path="edit-room/:id" element={<EditRoom />} />
              <Route path="edit-house" element={<EditHouse />} />
              <Route path="edit-house/:id" element={<EditHouse />} />
              <Route path="move-in/:id" element={<MoveIn />} />
              <Route path="move-out/:id" element={<MoveOut />} />
              <Route path="expense-form" element={<ExpenseForm />} />
            </Route>
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </div>
      </div>
    </DataProvider>
  )
}
