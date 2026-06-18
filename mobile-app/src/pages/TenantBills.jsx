import { COLOR } from '../data/colors.js';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { incomeBills, fmtMoney, maskPhone } from '../data/mock.js';

export default function TenantBills() {
  const nav = useNavigate();
  const [filter, setFilter] = useState('all');

  // 简化：取第一个租客的账单
  const myBills = incomeBills.filter(b => b.tenantId === 'tn_01');
  const filtered = filter === 'all' ? myBills : myBills.filter(b => b.status === filter);

  const statusMap = {
    paid: { label: '已支付', color: COLOR.success, bg: COLOR.successSoft },
    pending: { label: '待支付', color: COLOR.warn, bg: COLOR.warnSoft },
    overdue: { label: '逾期未付', color: COLOR.danger, bg: COLOR.dangerSoft },
    deposit_paid: { label: '押金已收', color: COLOR.indigo, bg: COLOR.indigoSoft }
  };

  const FILTER_TABS = [
    { id: 'all', label: '全部' },
    { id: 'pending', label: '待支付' },
    { id: 'overdue', label: '逾期' },
    { id: 'paid', label: '已付' }
  ];

  return (
    <div className="min-h-screen pb-24" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-8 pb-4 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <h1 className="text-[20px] font-bold" style={{ color: COLOR.gray4 }}>我的账单</h1>
        <p className="text-[12px] mt-1" style={{ color: COLOR.gray3 }}>查看待缴、已缴账单记录</p>
      </div>

      {/* 筛选 Tab */}
      <div className="px-5 pt-3">
        <div className="flex gap-2">
          {FILTER_TABS.map(t => (
            <button key={t.id} onClick={() => setFilter(t.id)}
              className={`flex-1 text-[12px] py-2 rounded-[14px] transition font-medium ${filter === t.id ? 'text-white' : 'text-slate-500'}`}
              style={filter === t.id ? { background: COLOR.brand } : { background: COLOR.gray1 }}>
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* 账单列表 */}
      <div className="px-5 pt-4 space-y-3">
        {filtered.map((b) => {
          const st = statusMap[b.status];
          return (
            <div key={b.id} className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)] tap-feedback" onClick={() => nav('/bill/' + b.id)}>
              <div className="flex items-start justify-between">
                <div className="flex-1 min-w-0 pr-3">
                  <div className="flex items-center gap-2 flex-wrap">
                    <h3 className="text-[14px] font-bold" style={{ color: COLOR.gray4 }}>{b.billName}</h3>
                    <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium" style={{ background: st.bg, color: st.color }}>{st.label}</span>
                  </div>
                  {b.periodStart && (
                    <div className="text-[10.5px] mt-1" style={{ color: COLOR.gray3 }}>账期 {b.periodStart} ~ {b.periodEnd}</div>
                  )}
                </div>
                <div className="text-right flex-shrink-0">
                  <div className="text-[15px] font-bold" style={{ color: COLOR.success }}>¥{fmtMoney(b.amount)}</div>
                </div>
              </div>
            </div>
          );
        })}
        {filtered.length === 0 && (
          <div className="text-center py-12" style={{ color: COLOR.gray3 }}>暂无账单</div>
        )}
      </div>
    </div>
  );
}
