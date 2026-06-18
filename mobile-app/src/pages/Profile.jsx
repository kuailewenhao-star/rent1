import { COLOR } from '../data/colors.js';
import { useNavigate } from 'react-router-dom';
import { profile, houseSources, rooms, contracts, incomeBills, expenseBills, fmtMoney, maskPhone } from '../data/mock.js';



export default function Profile() {
  const nav = useNavigate();

  const totalIncome = incomeBills.reduce((s, b) => s + b.amount, 0);
  const totalExpense = expenseBills.reduce((s, b) => s + b.amount, 0);
  const activeContracts = contracts.filter(c => c.status === 'active').length;
  const activeRooms = rooms.filter(r => r.status === 'rented').length;

  const menuGroups = [
    [
      { label: '房源管理', desc: `${houseSources.length} 套房源`, icon: '🏢', action: () => nav('/archive') },
      { label: '租客档案', desc: `共 ${rooms.filter(r => r.status === 'rented').length} 位租客`, icon: '👥', action: () => nav('/archive') }
    ],
    [
      { label: '合约管理', desc: `${activeContracts} 份生效合约`, icon: '📄', action: () => nav('/contracts') },
      { label: '房间统计', desc: `已出租 ${activeRooms} / 共 ${rooms.length} 间`, icon: '🛏️', action: () => nav('/archive') }
    ],
    [
      { label: '消息中心', desc: '最新账单/合约提醒', icon: '🔔', action: () => nav('/messages') },
      { label: '退出登录', desc: '安全退出当前账号', icon: '🚪', action: () => {
        if (confirm('确认退出登录？')) {
          localStorage.removeItem('isLoggedIn');
          localStorage.removeItem('userRole');
          nav('/login');
        }
      } }
    ]
  ];

  return (
    <div className="min-h-screen pb-24" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-8 pb-5" style={{ background: `linear-gradient(180deg, ${COLOR.brand} 0%, #1D4ED8 100%)` }}>
        <div className="flex items-center gap-3">
          <div className="w-14 h-14 rounded-full bg-white/25 backdrop-blur flex items-center justify-center text-white font-bold text-xl">{profile.avatarInitial}</div>
          <div className="flex-1">
            <div className="text-white text-[17px] font-bold">{profile.maskName}</div>
            <div className="text-white/80 text-[12px] mt-1">{maskPhone(profile.phone)}</div>
          </div>
        </div>

        <div className="mt-5 bg-white/15 backdrop-blur rounded-[16px] p-3 grid grid-cols-4 gap-1.5 text-center text-white">
          <div>
            <div className="text-[11px] opacity-80">房源</div>
            <div className="text-[15px] font-bold mt-0.5">{houseSources.length}</div>
          </div>
          <div>
            <div className="text-[11px] opacity-80">房间</div>
            <div className="text-[15px] font-bold mt-0.5">{rooms.length}</div>
          </div>
          <div>
            <div className="text-[11px] opacity-80">总收入</div>
            <div className="text-[13px] font-bold mt-0.5">{fmtMoney(totalIncome)}</div>
          </div>
          <div>
            <div className="text-[11px] opacity-80">总支出</div>
            <div className="text-[13px] font-bold mt-0.5">{fmtMoney(totalExpense)}</div>
          </div>
        </div>
      </div>

      <div className="px-5 -mt-3">
        <div className="bg-white rounded-[20px] p-4 shadow-[0_6px_20px_rgba(15,23,42,0.06)]">
          <div className="grid grid-cols-3 gap-2 text-center">
            <div>
              <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>净盈利</div>
              <div className="text-[16px] font-bold mt-0.5" style={{ color: COLOR.brand }}>¥{fmtMoney(totalIncome - totalExpense)}</div>
            </div>
            <div>
              <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>盈利率</div>
              <div className="text-[16px] font-bold mt-0.5" style={{ color: COLOR.success }}>{totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome * 100).toFixed(1) : 0}%</div>
            </div>
            <div>
              <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>生效合约</div>
              <div className="text-[16px] font-bold mt-0.5" style={{ color: COLOR.indigo }}>{activeContracts}</div>
            </div>
          </div>
        </div>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {menuGroups.map((group, gi) => (
          <div key={gi} className="bg-white rounded-[20px] shadow-[0_2px_8px_rgba(15,23,42,0.04)] overflow-hidden">
            {group.map((item, i) => (
              <button key={item.label} onClick={item.action} className="w-full flex items-center gap-3 px-4 py-3.5 active:bg-slate-50" style={i < group.length - 1 ? { borderBottom: '1px solid #F1F5F9' } : {}}>
                <div className="w-9 h-9 rounded-[12px] flex items-center justify-center text-lg" style={{ background: COLOR.brandSoft }}>{item.icon}</div>
                <div className="flex-1 text-left min-w-0">
                  <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>{item.label}</div>
                  <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>{item.desc}</div>
                </div>
                <span style={{ color: COLOR.gray3 }}>›</span>
              </button>
            ))}
          </div>
        ))}

        <div className="py-4 text-center text-[10px]" style={{ color: COLOR.gray3 }}>
          所有隐私信息均脱敏展示 · Version 1.0.0
        </div>
      </div>
    </div>
  );
}
