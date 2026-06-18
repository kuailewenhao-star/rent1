import { COLOR } from '../data/colors.js';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { profile, rooms, contracts, incomeBills, tenants, getRoomStats, getBillingSummary, fmtMoney, maskPhone } from '../data/mock.js';

const PERIOD_TABS = [
  { id: 'month', label: '本月' },
  { id: 'quarter', label: '本季' },
  { id: 'year', label: '今年' },
  { id: 'all', label: '全部' }
];

const getGreeting = () => {
  const h = new Date().getHours();
  if (h < 6) return '凌晨好';
  if (h < 12) return '早上好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
};

export default function TenantHome() {
  const nav = useNavigate();
  const [period, setPeriod] = useState('month');

  // 当前租客的房间
  const myContract = contracts.find(c => true); // 简化：取第一个
  const myRoom = rooms.find(r => r.status === 'rented');
  const myBills = incomeBills.filter(b => b.tenantId === 'tn_01');

  const pendingBills = myBills.filter(b => b.status === 'pending');
  const overdueBills = myBills.filter(b => b.status === 'overdue');
  const totalPending = pendingBills.reduce((s, b) => s + b.amount, 0);
  const totalOverdue = overdueBills.reduce((s, b) => s + b.amount, 0);

  const today = new Date();
  const dateStr = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`;
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][today.getDay()];

  return (
    <div className="min-h-screen pb-24" style={{ background: COLOR.gray1 }}>
      {/* 顶部问候 */}
      <div className="px-5 pt-8 pb-12" style={{ background: `linear-gradient(180deg, ${COLOR.brand} 0%, #1D4ED8 100%)` }}>
        <div className="flex items-start justify-between">
          <div>
            <div className="text-white text-[18px] font-bold leading-tight">{getGreeting()}，{profile.maskName}</div>
            <div className="text-white/75 text-[12px] mt-1.5">{dateStr} · {weekDay}</div>
          </div>
          <button onClick={() => nav('/tenant-profile')} className="w-10 h-10 rounded-full bg-white/15 backdrop-blur flex items-center justify-center active:scale-95 transition flex-shrink-0">
            <span style={{ color: 'white', fontSize: 18 }}>👤</span>
          </button>
        </div>
      </div>

      {/* 我的房间概况 */}
      <div className="px-5 -mt-6">
        <div className="bg-white rounded-[20px] p-4 shadow-[0_6px_20px_rgba(15,23,42,0.06)]">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-[14px] font-semibold" style={{ color: COLOR.gray4 }}>我的房间</h2>
            <button onClick={() => nav('/tenant-bills')} className="text-[12px]" style={{ color: COLOR.brand }}>待缴账单 →</button>
          </div>
          {myRoom ? (
            <div className="flex items-center gap-3 p-3 rounded-[14px]" style={{ background: COLOR.brandSoft }}>
              <div className="w-10 h-10 rounded-[12px] bg-white flex items-center justify-center text-lg">🔑</div>
              <div className="flex-1 min-w-0">
                <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>{myRoom.roomNo}</div>
                <div className="text-[11px]" style={{ color: COLOR.gray3 }}>已出租 · 合约履约中</div>
              </div>
              <div className="text-right">
                <div className="text-[14px] font-bold" style={{ color: COLOR.brand }}>¥{fmtMoney(myRoom.rent)}</div>
                <div className="text-[10px]" style={{ color: COLOR.gray3 }}>月租金</div>
              </div>
            </div>
          ) : (
            <div className="text-center py-4" style={{ color: COLOR.gray3 }}>暂无承租房间</div>
          )}
        </div>
      </div>

      {/* 待缴账单 */}
      <div className="px-5 mt-4">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-[14px] font-semibold" style={{ color: COLOR.gray4 }}>缴费提醒</h2>
        </div>
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)] space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-[12px] flex items-center justify-center" style={{ background: COLOR.warnSoft }}>
                <span className="text-[14px]">⏳</span>
              </div>
              <div>
                <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>待支付</div>
                <div className="text-[11px] mt-0.5" style={{ color: COLOR.gray3 }}>共 {pendingBills.length} 笔</div>
              </div>
            </div>
            <div className="text-right">
              <div className="text-[14px] font-bold" style={{ color: COLOR.warn }}>¥{fmtMoney(totalPending)}</div>
            </div>
          </div>
          <div className="h-px" style={{ background: '#F1F5F9' }} />
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-[12px] flex items-center justify-center" style={{ background: COLOR.dangerSoft }}>
                <span className="text-[14px]">⚠️</span>
              </div>
              <div>
                <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>逾期未付</div>
                <div className="text-[11px] mt-0.5" style={{ color: COLOR.gray3 }}>共 {overdueBills.length} 笔</div>
              </div>
            </div>
            <div className="text-right">
              <div className="text-[14px] font-bold" style={{ color: COLOR.danger }}>¥{fmtMoney(totalOverdue)}</div>
            </div>
          </div>
        </div>
      </div>

      {/* 快捷入口 */}
      <div className="px-5 mt-4">
        <h2 className="text-[14px] font-semibold mb-3" style={{ color: COLOR.gray4 }}>快捷入口</h2>
        <div className="grid grid-cols-4 gap-2">
          {[
            { label: '我的合约', icon: '📄', action: () => {} },
            { label: '我的账单', icon: '📋', action: () => nav('/tenant-bills') },
            { label: '我的房间', icon: '🏠', action: () => {} },
            { label: '个人中心', icon: '👤', action: () => nav('/tenant-profile') }
          ].map(item => (
            <button key={item.label} onClick={item.action} className="bg-white rounded-[18px] p-3 text-center tap-feedback shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
              <div className="text-[20px]">{item.icon}</div>
              <div className="text-[10.5px] mt-1.5" style={{ color: COLOR.gray3 }}>{item.label}</div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
