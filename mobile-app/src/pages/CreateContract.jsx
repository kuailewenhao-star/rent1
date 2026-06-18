import { COLOR } from '../data/colors.js';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import {
  rooms, tenants, contracts, houseSources,
  INCOME_FEE_TYPES, fmtMoney, todayISO
} from '../data/mock.js';
import { useData } from '../data/context.jsx';

export default function CreateContract() {
  const nav = useNavigate();
  const { contracts: ctxContracts, rooms: ctxRooms } = useData();
  const [form, setForm] = useState({
    tenantId: '', roomId: '', startDate: todayISO(), endDate: '',
    monthRent: 0, deposit: 0, remark: ''
  });

  const vacantRooms = ctxRooms.filter(r => r.status === 'vacant');

  const handleChange = (field, value) => {
    const newForm = { ...form, [field]: value };
    // 自动填充租金和押金
    if (field === 'roomId') {
      const room = ctxRooms.find(r => r.id === value);
      if (room) {
        newForm.monthRent = room.rent;
        newForm.deposit = room.deposit;
      }
    }
    setForm(newForm);
  };

  const handleSubmit = () => {
    if (!form.tenantId || !form.roomId || !form.endDate) {
      alert('请填写必填项');
      return;
    }
    alert('合约创建成功（演示）');
    nav('/contracts');
  };

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>新建合约</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {/* 房间选择 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>选择房间</h3>
          <select
            value={form.roomId}
            onChange={(e) => handleChange('roomId', e.target.value)}
            className="w-full p-3 rounded-[14px] border text-[13px]"
            style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }}
          >
            <option value="">请选择空置房间</option>
            {vacantRooms.map(r => {
              const house = houseSources.find(h => h.id === r.houseId);
              return <option key={r.id} value={r.id}>{house?.name} · {r.roomNo} · ¥{fmtMoney(r.rent)}/月</option>;
            })}
          </select>
        </div>

        {/* 租客选择 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>租客信息</h3>
          <select
            value={form.tenantId}
            onChange={(e) => handleChange('tenantId', e.target.value)}
            className="w-full p-3 rounded-[14px] border text-[13px]"
            style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }}
          >
            <option value="">请选择租客</option>
            {tenants.map(t => <option key={t.id} value={t.id}>{t.maskName} · {t.emergencyContact}</option>)}
          </select>
        </div>

        {/* 合约期 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>合约期限</h3>
          <div className="space-y-3">
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>起始日期</div>
              <input type="date" value={form.startDate} onChange={(e) => handleChange('startDate', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>结束日期</div>
              <input type="date" value={form.endDate} onChange={(e) => handleChange('endDate', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
            </div>
          </div>
        </div>

        {/* 金额 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>金额配置</h3>
          <div className="space-y-3">
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>月租金</div>
              <input type="number" value={form.monthRent} onChange={(e) => handleChange('monthRent', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>押金</div>
              <input type="number" value={form.deposit} onChange={(e) => handleChange('deposit', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
            </div>
          </div>
        </div>

        {/* 备注 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>备注</h3>
          <textarea
            value={form.remark}
            onChange={(e) => handleChange('remark', e.target.value)}
            rows={3}
            className="w-full p-3 rounded-[14px] border text-[13px]"
            style={{ borderColor: '#E2E8F0', background: COLOR.gray1, resize: 'none' }}
            placeholder="合约备注信息（如纸质合约上传说明）"
          />
        </div>

        {/* 提交 */}
        <button
          onClick={handleSubmit}
          className="w-full py-3.5 rounded-[14px] font-semibold text-[15px] text-white active:scale-[0.98] transition"
          style={{ background: COLOR.brand, boxShadow: '0 8px 24px rgba(37,99,235,0.25)' }}
        >确认创建合约</button>
      </div>
    </div>
  );
}
