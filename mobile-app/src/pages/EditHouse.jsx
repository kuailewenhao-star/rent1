import { COLOR } from '../data/colors.js';
import { useNavigate, useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useData } from '../data/context.jsx';

export default function EditHouse() {
  const nav = useNavigate();
  const { id } = useParams();
  const { houses, rooms, addHouse, updateHouse, addRoom, updateRoom, deleteRoom } = useData();

  const isEditMode = !!id;
  const existingHouse = isEditMode ? houses.find(h => h.id === id) : null;
  const houseRooms = isEditMode ? rooms.filter(r => r.houseId === id) : [];

  const [form, setForm] = useState({
    name: existingHouse?.name || '',
    type: existingHouse?.type || '整租',
    layout: existingHouse?.layout || '',
    area: existingHouse?.area || '',
    province: existingHouse?.province || '',
    city: existingHouse?.city || '',
    district: existingHouse?.district || '',
    address: existingHouse?.address || '',
    monthlyCost: existingHouse?.monthlyCost || '',
    depositCost: existingHouse?.depositCost || '',
    leaseStart: existingHouse?.leaseStart || '',
    leaseEnd: existingHouse?.leaseEnd || '',
    remark: existingHouse?.remark || '',
  });

  const [roomsForm, setRoomsForm] = useState(
    houseRooms.map(r => ({
      id: r.id,
      roomNo: r.roomNo,
      rent: r.rent,
      area: r.area,
      deposit: r.deposit,
      status: r.status || 'vacant',
    })) || []
  );

  useEffect(() => {
    if (form.type === '整租' && roomsForm.length === 0) {
      setRoomsForm([{ id: null, roomNo: '整套', rent: '', area: '', deposit: '', status: 'vacant' }]);
    }
  }, [form.type]);

  const handleChange = (field, value) => {
    setForm(f => ({ ...f, [field]: value }));
    if (field === 'type' && value === '整租' && roomsForm.length === 0) {
      setRoomsForm([{ id: null, roomNo: '整套', rent: '', area: '', deposit: '', status: 'vacant' }]);
    }
    if (field === 'type' && value === '整租' && roomsForm.length > 1) {
      setRoomsForm([roomsForm[0]]);
    }
  };

  const handleRoomChange = (index, field, value) => {
    setRoomsForm(prev => prev.map((r, i) => i === index ? { ...r, [field]: value } : r));
  };

  const addNewRoom = () => {
    setRoomsForm(prev => [...prev, { id: null, roomNo: '', rent: '', area: '', deposit: '', status: 'vacant' }]);
  };

  const removeRoom = (index) => {
    if (roomsForm.length <= 1) {
      alert('至少保留一间房间');
      return;
    }
    const room = roomsForm[index];
    if (room.id && confirm('确认删除该房间？')) {
      deleteRoom(room.id);
    }
    setRoomsForm(prev => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = () => {
    if (!form.name || !form.address) {
      alert('请填写房源名称和地址');
      return;
    }

    if (form.type === '合租' && roomsForm.length === 0) {
      alert('合租房源至少需要添加一间房间');
      return;
    }

    const hasEmptyRoom = roomsForm.some(r => !r.roomNo || !r.rent);
    if (hasEmptyRoom) {
      alert('请填写所有房间的名称和租金');
      return;
    }

    if (isEditMode) {
      updateHouse(id, form);
      roomsForm.forEach(r => {
        if (r.id) {
          updateRoom(r.id, { ...r, houseId: id });
        } else {
          addRoom({ ...r, houseId: id });
        }
      });
    } else {
      const newHouse = addHouse(form);
      roomsForm.forEach(r => {
        addRoom({ ...r, houseId: newHouse.id });
      });
    }

    alert(`${isEditMode ? '编辑' : '新增'}房源成功`);
    nav('/archive');
  };

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>{isEditMode ? '编辑房源' : '新增房源'}</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {/* 基本信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>基本信息</h3>
          <div className="space-y-3">
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>房源名称</div>
              <input value={form.name} onChange={(e) => handleChange('name', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="如：阳光小区3-201" />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>租赁类型</div>
              <div className="flex gap-2">
                {['整租', '合租'].map(t => (
                  <button key={t} onClick={() => handleChange('type', t)} className="flex-1 py-2 rounded-[10px] text-[12px] font-medium transition"
                    style={{ background: form.type === t ? COLOR.brandSoft : COLOR.gray1, color: form.type === t ? COLOR.brand : COLOR.gray4 }}>
                    {t}
                  </button>
                ))}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>户型</div>
                <input value={form.layout} onChange={(e) => handleChange('layout', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="如：2室1厅1卫" />
              </div>
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>面积（㎡）</div>
                <input type="number" value={form.area} onChange={(e) => handleChange('area', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
              </div>
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>详细地址</div>
              <input value={form.address} onChange={(e) => handleChange('address', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="请输入详细地址" />
            </div>
          </div>
        </div>

        {/* 财务信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>财务信息</h3>
          <div className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>月租金（给大房东）</div>
                <input type="number" value={form.monthlyCost} onChange={(e) => handleChange('monthlyCost', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="0" />
              </div>
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>押金（给大房东）</div>
                <input type="number" value={form.depositCost} onChange={(e) => handleChange('depositCost', Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="0" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>签约开始日期</div>
                <input type="date" value={form.leaseStart} onChange={(e) => handleChange('leaseStart', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
              </div>
              <div>
                <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>签约结束日期</div>
                <input type="date" value={form.leaseEnd} onChange={(e) => handleChange('leaseEnd', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} />
              </div>
            </div>
          </div>
        </div>

        {/* 房间列表 */}
        {form.type === '合租' && (
          <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-[13px] font-bold" style={{ color: COLOR.gray4 }}>房间列表（合租房源）</h3>
              <button onClick={addNewRoom} className="text-[11px] font-medium active:scale-95 transition px-2 py-1 rounded-[8px]" style={{ background: COLOR.brandSoft, color: COLOR.brand }}>+ 添加房间</button>
            </div>
            <div className="space-y-3">
              {roomsForm.map((room, index) => (
                <div key={index} className="p-3 rounded-[14px]" style={{ background: COLOR.gray1 }}>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-[11px] font-medium" style={{ color: COLOR.gray3 }}>房间 {index + 1}</span>
                    {roomsForm.length > 1 && (
                      <button onClick={() => removeRoom(index)} className="text-[10px] font-medium" style={{ color: COLOR.danger }}>删除</button>
                    )}
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <div className="text-[10px] mb-1" style={{ color: COLOR.gray3 }}>房间名称</div>
                      <input value={room.roomNo} onChange={(e) => handleRoomChange(index, 'roomNo', e.target.value)} className="w-full p-2.5 rounded-[10px] border text-[12px]" style={{ borderColor: '#E2E8F0', background: 'white' }} placeholder="如：主卧、次卧A" />
                    </div>
                    <div>
                      <div className="text-[10px] mb-1" style={{ color: COLOR.gray3 }}>挂牌租金（元/月）</div>
                      <input type="number" value={room.rent} onChange={(e) => handleRoomChange(index, 'rent', Number(e.target.value))} className="w-full p-2.5 rounded-[10px] border text-[12px]" style={{ borderColor: '#E2E8F0', background: 'white' }} placeholder="0" />
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-3 mt-3">
                    <div>
                      <div className="text-[10px] mb-1" style={{ color: COLOR.gray3 }}>面积（㎡）</div>
                      <input type="number" value={room.area} onChange={(e) => handleRoomChange(index, 'area', Number(e.target.value))} className="w-full p-2.5 rounded-[10px] border text-[12px]" style={{ borderColor: '#E2E8F0', background: 'white' }} placeholder="0" />
                    </div>
                    <div>
                      <div className="text-[10px] mb-1" style={{ color: COLOR.gray3 }}>押金</div>
                      <input type="number" value={room.deposit} onChange={(e) => handleRoomChange(index, 'deposit', Number(e.target.value))} className="w-full p-2.5 rounded-[10px] border text-[12px]" style={{ borderColor: '#E2E8F0', background: 'white' }} placeholder="0" />
                    </div>
                  </div>
                  <div className="mt-3">
                    <div className="text-[10px] mb-1" style={{ color: COLOR.gray3 }}>房间状态</div>
                    <div className="flex gap-2">
                      {['vacant', 'rented'].map(s => (
                        <button key={s} onClick={() => handleRoomChange(index, 'status', s)} className="flex-1 py-1.5 rounded-[8px] text-[11px] font-medium transition"
                          style={{ background: room.status === s ? (s === 'rented' ? COLOR.successSoft : COLOR.gray1) : COLOR.gray1, color: room.status === s ? (s === 'rented' ? COLOR.success : COLOR.gray4) : COLOR.gray3 }}>
                          {s === 'vacant' ? '空置中' : '已出租'}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 备注 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>备注</h3>
          <textarea value={form.remark} onChange={(e) => handleChange('remark', e.target.value)} className="w-full p-3 rounded-[14px] border text-[13px] resize-none" style={{ borderColor: '#E2E8F0', background: COLOR.gray1, height: 80 }} placeholder="房源备注信息" />
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