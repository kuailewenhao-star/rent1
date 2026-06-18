import { COLOR } from '../data/colors.js';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { houseSources, EXPENSE_TYPES, fmtMoney, todayISO } from '../data/mock.js';
import { TYPE_COLORS } from '../data/colors.js';

export default function ExpenseForm() {
  const nav = useNavigate();
  const [form, setForm] = useState({
    houseId: '', expenseType: '', amount: '', costDate: todayISO(), remark: ''
  });

  const handleChange = (field, value) => setForm(f => ({ ...f, [field]: value }));

  const handleSubmit = () => {
    if (!form.houseId || !form.expenseType || !form.amount) {
      alert('请填写必填项');
      return;
    }
    alert('支出账单创建成功（演示）');
    nav('/bill');
  };

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>新增支出</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {/* 房源选择 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>关联房源</h3>
          <select value={form.houseId} onChange={(e) => handleChange('houseId', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }}>
            <option value="">请选择房源</option>
            {houseSources.map(h => <option key={h.id} value={h.id}>{h.name}（{h.district}）</option>)}
          </select>
        </div>

        {/* 支出类型 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>支出类型（2类固定枚举）</h3>
          <div className="grid grid-cols-2 gap-2">
            {EXPENSE_TYPES.map(t => {
              const tc = TYPE_COLORS[t.id] || TYPE_COLORS.other;
              return (
                <button key={t.id} onClick={() => handleChange('expenseType', t.id)}
                  className={`p-3 rounded-[14px] text-left transition ${form.expenseType === t.id ? 'ring-2' : ''}`}
                  style={{ background: form.expenseType === t.id ? tc.bg : COLOR.gray1, borderColor: form.expenseType === t.id ? tc.c : 'transparent', color: tc.c }}>
                  <div className="text-[12px] font-semibold">{t.label}</div>
                </button>
              );
            })}
          </div>
        </div>

        {/* 金额和日期 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>金额与日期</h3>
          <div className="space-y-3">
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>支出金额</div>
              <input type="number" value={form.amount} onChange={(e) => handleChange('amount', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="0" />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>支出日期</div>
              <input type="date" value={form.costDate} onChange={(e) => handleChange('costDate', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
            </div>
          </div>
        </div>

        {/* 备注 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>备注</h3>
          <textarea value={form.remark} onChange={(e) => handleChange('remark', e.target.value)} rows={3} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1, resize: 'none' }} placeholder="补充说明支出用途" />
        </div>

        <button onClick={handleSubmit} className="w-full py-3.5 rounded-[14px] font-semibold text-[15px] text-white active:scale-[0.98] transition" style={{ background: COLOR.brand, boxShadow: '0 8px 24px rgba(37,99,235,0.25)' }}>确认新增支出</button>
      </div>
    </div>
  );
}
