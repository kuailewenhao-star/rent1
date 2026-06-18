import { COLOR } from '../data/colors.js';
import { useNavigate, useParams } from 'react-router-dom';
import { useState } from 'react';
import { rooms, houseSources, getRoomsByHouse, fmtMoney } from '../data/mock.js';
import { useData } from '../data/context.jsx';

export default function EditRoom() {
  const nav = useNavigate();
  const { id } = useParams();
  const { rooms: ctxRooms } = useData();

  const isEditMode = id.startsWith('rm_');
  const existingRoom = isEditMode ? ctxRooms.find(r => r.id === id) : null;

  const [form, setForm] = useState({
    roomNo: existingRoom?.roomNo || '',
    area: existingRoom?.area || '',
    rent: existingRoom?.rent || '',
    deposit: existingRoom?.deposit || '',
    chargeCycle: '每月',
    status: existingRoom?.status || 'vacant',
  });

  const handleChange = (field, value) => setForm(f => ({ ...f, [field]: value }));

  const handleSubmit = () => {
    if (!form.roomNo || !form.rent) {
      alert('请填写房间号和租金');
      return;
    }
    alert(`${isEditMode ? '编辑' : '新增'}房间成功（演示）`);
    nav('/archive');
  };

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>{isEditMode ? '编辑房间' : '新增房间'}</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {/* 基本信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>基本信息</h3>
          <div className="space-y-3">
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>房间号</div>
              <input value={form.roomNo} onChange={(e) => handleChange('roomNo', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="如：主卧、次卧A、整套" />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>面积（㎡）</div>
              <input type="number" value={form.area} onChange={(e) => handleChange('area', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>房间状态</div>
              <div className="flex gap-2">
                {['vacant', 'rented'].map(s => (
                  <button key={s} onClick={() => handleChange('status', s)} className="flex-1 py-2 rounded-[10px] text-[12px] font-medium transition"
                    style={{ background: form.status === s ? (s === 'rented' ? COLOR.successSoft : COLOR.gray1) : COLOR.gray1, color: form.status === s ? (s === 'rented' ? COLOR.success : COLOR.gray4) : COLOR.gray3 }}>
                    {s === 'vacant' ? '空置中' : '已出租'}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* 财务信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>财务信息</h3>
          <div className="space-y-3">
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>月租金</div>
              <input type="number" value={form.rent} onChange={(e) => handleChange('rent', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="0" />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>押金</div>
              <input type="number" value={form.deposit} onChange={(e) => handleChange('deposit', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="0" />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>缴费周期</div>
              <select value={form.chargeCycle} onChange={(e) => handleChange('chargeCycle', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }}>
                {['每月', '每三月', '每六月', '每年'].map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
          </div>
        </div>

        <button
          onClick={handleSubmit}
          className="w-full py-3.5 rounded-[14px] font-semibold text-[15px] text-white active:scale-[0.98] transition"
          style={{ background: COLOR.brand, boxShadow: '0 8px 24px rgba(37,99,235,0.25)' }}
        >{isEditMode ? '保存修改' : '确认新增'}</button>
      </div>
    </div>
  );
}
