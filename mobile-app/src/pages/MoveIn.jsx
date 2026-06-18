import { COLOR } from '../data/colors.js';
import { useNavigate, useParams } from 'react-router-dom';
import { useState } from 'react';
import { useData } from '../data/context.jsx';
import { todayISO } from '../data/mock.js';

const CHARGE_CYCLE_OPTIONS = [
  { value: 'monthly', label: '月付' },
  { value: 'quarterly', label: '季付' },
  { value: 'half-yearly', label: '半年付' },
  { value: 'yearly', label: '年付' },
];

export default function MoveIn() {
  const nav = useNavigate();
  const { id } = useParams();
  const { rooms, addTenant, addContract, generateRentBills, updateRoom } = useData();

  const room = rooms.find(r => r.id === id);

  const [form, setForm] = useState({
    name: '',
    idCard: '',
    phone: '',
    emergencyContact: '',
    emergencyPhone: '',
    monthRent: room?.rent || '',
    deposit: room?.deposit || '',
    chargeCycle: 'monthly',
    startDate: todayISO(),
    endDate: '',
    remark: '',
  });

  const handleChange = (field, value) => setForm(f => ({ ...f, [field]: value }));

  const validate = () => {
    if (!form.name) return '请填写租客姓名';
    if (!form.idCard) return '请填写身份证号';
    if (!/^\d{17}[\dXx]$/.test(form.idCard)) return '身份证号格式不正确';
    if (!form.phone) return '请填写手机号';
    if (!/^1\d{10}$/.test(form.phone)) return '手机号格式不正确';
    if (!form.monthRent || form.monthRent <= 0) return '请填写月租金';
    if (form.deposit === '' || form.deposit < 0) return '请填写押金';
    if (!form.startDate) return '请选择起租时间';
    if (!form.endDate) return '请选择到期时间';
    if (new Date(form.endDate) <= new Date(form.startDate)) return '到期时间必须大于起租时间';
    return null;
  };

  const handleSubmit = () => {
    const err = validate();
    if (err) {
      alert(err);
      return;
    }

    // 1. 创建租客（含通行证、主体信息，手机号为别名）
    const tenant = addTenant({
      name: form.name,
      phone: form.phone,
      idCard: form.idCard,
      emergencyContact: form.emergencyContact,
      emergencyPhone: form.emergencyPhone,
      roomId: id,
      contractId: '',
      status: '正常',
      joinDate: form.startDate,
      // 通行证信息
      passId: form.phone,  // 手机号作为通行证别名
      passName: form.name,
      // 主体信息
      subjectName: form.name,
      subjectIdCard: form.idCard,
      subjectPhone: form.phone,
    });

    // 2. 创建合约
    const contract = addContract({
      tenantId: tenant.id,
      roomId: id,
      houseId: room.houseId,
      startDate: form.startDate,
      endDate: form.endDate,
      status: 'active',
      monthRent: Number(form.monthRent),
      deposit: Number(form.deposit),
      chargeCycle: form.chargeCycle,
      remark: form.remark,
    });

    // 3. 回写租客合约ID
    addTenant({ ...tenant, contractId: contract.id });

    // 4. 生成租金账单和押金账单
    generateRentBills(contract, tenant);

    // 5. 更新房间状态为已出租
    updateRoom(id, { status: 'rented' });

    alert(`入驻签约成功！\n租客：${form.name}\n通行证别名（手机号）：${form.phone}\n已生成 ${form.chargeCycle === 'monthly' ? '月' : form.chargeCycle === 'quarterly' ? '季' : form.chargeCycle === 'half-yearly' ? '半年' : '年'}度账单`);
    nav('/room/' + id);
  };

  if (!room) {
    return <div className="p-6 text-center" style={{ color: COLOR.gray3 }}>找不到房间</div>;
  }

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>租客入驻签约</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {/* 房间信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>房间信息</h3>
          <div className="text-[12px]" style={{ color: COLOR.gray3 }}>
            房间号：<span style={{ color: COLOR.gray4, fontWeight: 600 }}>{room.roomNo}</span>
            <span className="ml-3">面积：<span style={{ color: COLOR.gray4, fontWeight: 600 }}>{room.area}㎡</span></span>
          </div>
        </div>

        {/* 租客身份信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>租客身份信息</h3>
          <div className="space-y-3">
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>租客姓名 <span style={{ color: COLOR.danger }}>*</span></div>
              <input value={form.name} onChange={(e) => handleChange('name', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="请输入真实姓名" />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>身份证号 <span style={{ color: COLOR.danger }}>*</span></div>
              <input value={form.idCard} onChange={(e) => handleChange('idCard', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="18位身份证号" maxLength={18} />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>手机号（通行证别名） <span style={{ color: COLOR.danger }}>*</span></div>
              <input value={form.phone} onChange={(e) => handleChange('phone', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="11位手机号，作为登录通行证别名" maxLength={11} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>紧急联系人</div>
                <input value={form.emergencyContact} onChange={(e) => handleChange('emergencyContact', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="选填" />
              </div>
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>紧急联系电话</div>
                <input value={form.emergencyPhone} onChange={(e) => handleChange('emergencyPhone', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="选填" maxLength={11} />
              </div>
            </div>
          </div>
        </div>

        {/* 合约信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>合约信息</h3>
          <div className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>月租金 <span style={{ color: COLOR.danger }}>*</span></div>
                <input type="number" value={form.monthRent} onChange={(e) => handleChange('monthRent', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="0" />
              </div>
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>押金 <span style={{ color: COLOR.danger }}>*</span></div>
                <input type="number" value={form.deposit} onChange={(e) => handleChange('deposit', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="0" />
              </div>
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>缴费周期</div>
              <div className="flex gap-2">
                {CHARGE_CYCLE_OPTIONS.map(opt => (
                  <button key={opt.value} onClick={() => handleChange('chargeCycle', opt.value)} className="flex-1 py-2 rounded-[10px] text-[12px] font-medium transition"
                    style={{ background: form.chargeCycle === opt.value ? COLOR.brandSoft : COLOR.gray1, color: form.chargeCycle === opt.value ? COLOR.brand : COLOR.gray4 }}>
                    {opt.label}
                  </button>
                ))}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>起租时间 <span style={{ color: COLOR.danger }}>*</span></div>
                <input type="date" value={form.startDate} onChange={(e) => handleChange('startDate', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
              </div>
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>到期时间 <span style={{ color: COLOR.danger }}>*</span></div>
                <input type="date" value={form.endDate} onChange={(e) => handleChange('endDate', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
              </div>
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>合约备注</div>
              <textarea value={form.remark} onChange={(e) => handleChange('remark', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px] resize-none" style={{ borderColor: '#E2E8F0', background: COLOR.gray1, height: 60 }} placeholder="选填，如：纸质合约已签约" />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-[20px] p-3 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div className="text-[11px]" style={{ color: COLOR.gray3, lineHeight: 1.7 }}>
            <div>• 入驻后该房间自动标记为已出租</div>
            <div>• 系统将按缴费周期自动生成租金账单，押金仅生成一次</div>
            <div>• 租客使用手机号作为通行证别名登录系统</div>
          </div>
        </div>

        <button
          onClick={handleSubmit}
          className="w-full py-3.5 rounded-[14px] font-semibold text-[15px] text-white active:scale-[0.98] transition"
          style={{ background: COLOR.brand, boxShadow: '0 8px 24px rgba(37,99,235,0.25)' }}
        >确认入驻签约</button>
      </div>
    </div>
  );
}
