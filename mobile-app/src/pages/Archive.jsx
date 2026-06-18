import { COLOR } from '../data/colors.js';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useData } from '../data/context.jsx';
import {
  getRoomsByHouse,
  fmtMoney,
  maskPhone
} from '../data/mock.js';


const TAB_LIST = [
  { id: 'house', label: '房源管理' },
  { id: 'tenant', label: '租客档案' }
];

// 移除模块级可变状态，使用 Context

export default function Archive() {
  const nav = useNavigate();
  const { houses, rooms: ctxRooms, tenants: ctxTenants, deleteHouse, deleteRoom, deleteTenant } = useData();
  const [tab, setTab] = useState('house');

  return (
    <div className="min-h-screen pb-24 relative" style={{ background: COLOR.gray1 }}>
      {/* 顶部 */}
      <div className="px-5 pt-8 pb-4 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <h1 className="text-[20px] font-bold" style={{ color: COLOR.gray4 }}>{tab === 'house' ? '房源管理' : '租客档案'}</h1>
        <p className="text-[12px] mt-1" style={{ color: COLOR.gray3 }}>
          {tab === 'house' ? '房源与房间双层隔离管理' : '当前在租租客信息'}
        </p>
      </div>

      {/* Tab */}
      <div className="px-5 pt-3">
        <div className="flex gap-2">
          {TAB_LIST.map((t) => (
            <button
              key={t.id}
              onClick={() => setTab(t.id)}
              className={`flex-1 text-[13px] py-2.5 rounded-[14px] transition font-medium ${tab === t.id ? 'text-white' : 'text-slate-500'}`}
              style={tab === t.id ? { background: COLOR.brand } : { background: COLOR.gray1 }}
            >{t.label}</button>
          ))}
        </div>
      </div>

      {/* 内容 */}
      <div className="px-5 pt-4">
        {tab === 'house' ? (
          <HouseList nav={nav} houses={houses} rooms={ctxRooms} tenants={ctxTenants} deleteHouse={deleteHouse} deleteRoom={deleteRoom} />
        ) : (
          <TenantList tenants={ctxTenants} rooms={ctxRooms} houses={houses} deleteTenant={deleteTenant} />
        )}
      </div>

      {/* 右下角悬浮新增按钮 */}
      <button
        onClick={() => {
          if (tab === 'house') {
            nav('/edit-house');
          } else {
            if (confirm('确认新增一条租客？(演示功能)')) alert('已调用新增租客接口（演示）');
          }
        }}
        className="fixed right-5 bottom-[110px] w-14 h-14 rounded-full shadow-[0_8px_24px_rgba(37,99,235,0.4)] flex items-center justify-center text-white font-bold text-[28px] active:scale-95 transition z-20"
        style={{ background: COLOR.brand }}
      >+</button>
    </div>
  );
}

/* ========== 房源列表 ========== */
function HouseList({ nav, houses, rooms: ctxRooms, tenants: ctxTenants, deleteHouse, deleteRoom }) {
  return (
    <div className="space-y-3.5">
      {/* 顶部统计卡 */}
      <div className="bg-white rounded-[20px] p-3 flex items-center justify-between shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <div>
          <div className="text-[11px]" style={{ color: COLOR.gray3 }}>当前房源</div>
          <div className="mt-0.5 text-[15px] font-bold" style={{ color: COLOR.gray4 }}>{houses.length} 套</div>
        </div>
        <div className="flex gap-4">
          <div className="text-right">
            <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>已出租房间</div>
            <div className="text-[13px] font-bold" style={{ color: COLOR.success }}>{ctxRooms.filter(r => r.status === 'rented').length} 间</div>
          </div>
          <div className="text-right">
            <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>空置房间</div>
            <div className="text-[13px] font-bold" style={{ color: COLOR.gray3 }}>{ctxRooms.filter(r => r.status === 'vacant').length} 间</div>
          </div>
        </div>
      </div>

      {houses.map((h) => {
        const hrooms = getRoomsByHouse(h.id);
        const rented = hrooms.filter(r => r.status === 'rented').length;
        return (
          <div key={h.id} className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            {/* 房源头 */}
            <div className="flex items-start justify-between">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <button onClick={() => nav('/house/' + h.id)} className="text-[14px] font-bold truncate text-left" style={{ color: COLOR.gray4 }}>{h.name}</button>
                  <span className="text-[10px] px-1.5 py-0.5 rounded font-medium" style={{ background: COLOR.brandSoft, color: COLOR.brand }}>{h.type}</span>
                </div>
                <div className="text-[11px] mt-1" style={{ color: COLOR.gray3 }}>{h.layout} · {h.area}㎡ · {h.district}</div>
              </div>
              {/* 房源操作按钮 */}
              <div className="flex items-center gap-1 ml-2 flex-shrink-0">
                <button
                  onClick={() => nav('/edit-house/' + h.id)}
                  className="text-[11px] px-2 py-1 rounded-[8px] font-medium active:scale-95 transition"
                  style={{ background: COLOR.brandSoft, color: COLOR.brand }}
                >编辑</button>
                <button
                  onClick={() => {
                    if (confirm('确认删除该房源？将同时删除其所有房间')) {
                      deleteHouse(h.id);
                    }
                  }}
                  className="text-[11px] px-2 py-1 rounded-[8px] font-medium active:scale-95 transition"
                  style={{ background: COLOR.dangerSoft, color: COLOR.danger }}
                >删除</button>
              </div>
            </div>

            {/* 房间聚合 */}
            <div className="mt-3 pt-3" style={{ borderTop: '1px dashed #E2E8F0' }}>
              <div className="flex items-center justify-between mb-2.5">
                <div className="text-[11.5px] font-semibold" style={{ color: COLOR.gray4 }}>
                  房间列表 · 共 {hrooms.length} 间（已出租 {rented} 间）
                </div>
                <button
                  onClick={() => nav('/edit-house/' + h.id)}
                  className="text-[10.5px] font-medium active:scale-95 transition"
                  style={{ color: COLOR.brand }}
                >+ 新增房间</button>
              </div>
              <div className="space-y-2">
                {hrooms.map((r) => {
                  const tenant = ctxTenants.find(t => t.roomId === r.id);
                  return (
                    <div key={r.id} className="p-3 rounded-[14px] tap-feedback" style={{ background: r.status === 'rented' ? COLOR.successSoft : COLOR.gray1, cursor: 'pointer' }} onClick={() => nav('/room/' + r.id)}>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2 flex-1 min-w-0">
                          <div className="w-7 h-7 rounded-[10px] flex items-center justify-center text-[12px] bg-white">{r.status === 'rented' ? '🔑' : '🏠'}</div>
                          <div className="min-w-0 flex-1">
                            <div className="flex items-center gap-1.5 flex-wrap">
                              <div className="text-[12.5px] font-semibold truncate" style={{ color: COLOR.gray4 }}>{r.roomNo}</div>
                              <span className="text-[9px] px-1.5 py-0.5 rounded font-medium" style={{ background: r.status === 'rented' ? 'white' : '#F8FAFC', color: r.status === 'rented' ? COLOR.success : COLOR.gray3 }}>{r.status === 'rented' ? '已出租' : '空置中'}</span>
                            </div>
                            <div className="text-[10px] mt-0.5 truncate" style={{ color: COLOR.gray3 }}>{tenant ? tenant.maskName + ' · ' + maskPhone(tenant.phone) : '暂无租客'}</div>
                          </div>
                        </div>
                        <div className="flex items-center gap-1 ml-2 flex-shrink-0">
                          <div className="text-[12px] font-bold mr-1" style={{ color: r.status === 'rented' ? COLOR.success : COLOR.gray3 }}>¥{fmtMoney(r.rent)}</div>
                          <button onClick={(e) => { e.stopPropagation(); nav('/edit-room/' + r.id); }} className="text-[10px] px-1.5 py-1 rounded-[6px] font-medium" style={{ background: 'white', color: COLOR.brand }}>编辑</button>
                          <button
                            onClick={(e) => { e.stopPropagation(); if (confirm('确认删除该房间？')) { deleteRoom(r.id); }}}
                            className="text-[10px] px-1.5 py-1 rounded-[6px] font-medium" style={{ background: 'white', color: COLOR.danger }}
                          >删除</button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* 底部信息 */}
            <div className="mt-3 pt-3 text-[10.5px] flex items-center justify-between" style={{ borderTop: '1px solid #F1F5F9', color: COLOR.gray3 }}>
              <span>月租支出 ¥{fmtMoney(h.monthlyCost)}</span>
              <span>签约至 {h.leaseEnd}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
}

/* ========== 租客列表 ========== */
function TenantList({ nav, tenants: ctxTenants, rooms: ctxRooms, houses, deleteTenant }) {
  return (
    <div className="space-y-3">
      <div className="bg-white rounded-[20px] p-3 flex items-center justify-between shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <div>
          <div className="text-[11px]" style={{ color: COLOR.gray3 }}>在租租客</div>
          <div className="mt-0.5 text-[15px] font-bold" style={{ color: COLOR.gray4 }}>{ctxTenants.length} 位</div>
        </div>
        <div className="text-right">
          <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>欠费提醒</div>
          <div className="text-[13px] font-bold" style={{ color: ctxTenants.filter(t => t.status === '欠费').length > 0 ? COLOR.danger : COLOR.gray3 }}>{ctxTenants.filter(t => t.status === '欠费').length} 位</div>
        </div>
      </div>

      {ctxTenants.map((t) => {
        const room = ctxRooms.find(r => r.id === t.roomId);
        const house = houses.find(h => h.id === room?.houseId);
        return (
          <div key={t.id} className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            <div className="flex items-start gap-3">
              <div className="w-11 h-11 rounded-full flex items-center justify-center text-white font-bold text-base flex-shrink-0" style={{ background: COLOR.brand }}>{t.maskName[0]}</div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <button onClick={() => nav('/tenant/' + t.id)} className="text-[14px] font-bold truncate text-left" style={{ color: COLOR.gray4 }}>{t.maskName}</button>
                  <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium" style={{ background: t.status === '正常' ? COLOR.successSoft : COLOR.dangerSoft, color: t.status === '正常' ? COLOR.success : COLOR.danger }}>{t.status}</span>
                </div>
                <div className="text-[11px] mt-1" style={{ color: COLOR.gray3 }}>{maskPhone(t.phone)}</div>
                <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>{house?.name} · {room?.roomNo} · 入住 {t.joinDate}</div>
              </div>
              {/* 租客操作 */}
              <div className="flex items-center gap-1 flex-shrink-0">
                <button onClick={() => nav('/tenant/' + t.id)} className="text-[11px] px-2 py-1 rounded-[8px] font-medium active:scale-95 transition" style={{ background: COLOR.brandSoft, color: COLOR.brand }}>详情</button>
                <button
                  onClick={() => {
                    if (confirm('确认删除该租客档案？')) {
                      deleteTenant(t.id);
                    }
                  }}
                  className="text-[11px] px-2 py-1 rounded-[8px] font-medium active:scale-95 transition"
                  style={{ background: COLOR.dangerSoft, color: COLOR.danger }}
                >删除</button>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
