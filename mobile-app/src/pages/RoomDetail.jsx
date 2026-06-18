import { COLOR } from '../data/colors.js';
import { useNavigate, useParams } from 'react-router-dom';
import { useData } from '../data/context.jsx';
import { fmtMoney, maskPhone, maskName } from '../data/mock.js';

export default function RoomDetail() {
  const nav = useNavigate();
  const { id } = useParams();
  const { rooms, houses, tenants, contracts, incomeBills } = useData();

  const room = rooms.find(r => r.id === id);

  if (!room) {
    return <div className="p-6 text-center" style={{ color: COLOR.gray3 }}>找不到房间</div>;
  }

  const house = houses.find(h => h.id === room.houseId);
  const tenant = tenants.find(t => t.roomId === id);
  const contract = contracts.find(c => c.roomId === id && c.status === 'active');
  const bills = incomeBills.filter(b => b.roomId === id);

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>房间详情</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div className="flex items-center gap-2">
            <h2 className="text-[17px] font-bold" style={{ color: COLOR.gray4 }}>{house?.name} · {room.roomNo}</h2>
            <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium" style={{ background: room.status === 'rented' ? COLOR.successSoft : COLOR.gray1, color: room.status === 'rented' ? COLOR.success : COLOR.gray3 }}>{room.status === 'rented' ? '已出租' : '空置中'}</span>
          </div>
          <div className="grid grid-cols-3 gap-2 mt-4 text-center">
            <div>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>月租金</div>
              <div className="text-[14px] font-bold mt-0.5" style={{ color: COLOR.success }}>¥{fmtMoney(room.rent)}</div>
            </div>
            <div>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>押金</div>
              <div className="text-[14px] font-bold mt-0.5" style={{ color: COLOR.indigo }}>¥{fmtMoney(room.deposit)}</div>
            </div>
            <div>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>面积</div>
              <div className="text-[14px] font-bold mt-0.5" style={{ color: COLOR.gray4 }}>{room.area}㎡</div>
            </div>
          </div>
        </div>

        {tenant ? (
          <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>租客信息</h3>
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-full flex items-center justify-center text-white font-bold" style={{ background: COLOR.brand }}>{tenant.maskName?.[0] || tenant.name?.[0] || '?'}</div>
              <div className="flex-1 min-w-0">
                <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>{tenant.name || tenant.maskName}</div>
                <div className="text-[11px] mt-0.5" style={{ color: COLOR.gray3 }}>{maskPhone(tenant.phone)}</div>
              </div>
              <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium" style={{ background: tenant.status === '正常' ? COLOR.successSoft : tenant.status === '已退租' ? COLOR.gray1 : COLOR.dangerSoft, color: tenant.status === '正常' ? COLOR.success : tenant.status === '已退租' ? COLOR.gray3 : COLOR.danger }}>{tenant.status}</span>
            </div>
            <div className="mt-3 pt-3 text-[11px] space-y-1" style={{ borderTop: '1px solid #F1F5F9', color: COLOR.gray3 }}>
              <div>入住日期：{tenant.joinDate}</div>
              {tenant.emergencyContact && <div>紧急联系人：{maskName(tenant.emergencyContact)}</div>}
              {tenant.subjectIdCard && <div>身份证号：{tenant.subjectIdCard.replace(/^(.{6}).+(.{4})$/, '$1********$2')}</div>}
            </div>
          </div>
        ) : (
          <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>租客信息</h3>
            <div className="text-[12px] py-3 text-center" style={{ color: COLOR.gray3 }}>该房间暂无租客</div>
            <button
              onClick={() => nav('/move-in/' + id)}
              className="w-full mt-2 py-3 rounded-[14px] font-semibold text-[14px] text-white active:scale-[0.98] transition"
              style={{ background: COLOR.brand, boxShadow: '0 8px 24px rgba(37,99,235,0.25)' }}
            >+ 租客入驻签约</button>
          </div>
        )}

        {contract && (
          <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>合约信息</h3>
            <div className="space-y-2 text-[12px]">
              <div className="flex justify-between">
                <span style={{ color: COLOR.gray3 }}>合约期</span>
                <span style={{ color: COLOR.gray4 }}>{contract.startDate} ~ {contract.endDate}</span>
              </div>
              <div className="flex justify-between">
                <span style={{ color: COLOR.gray3 }}>月租金</span>
                <span className="font-bold" style={{ color: COLOR.success }}>¥{fmtMoney(contract.monthRent)}</span>
              </div>
              <div className="flex justify-between">
                <span style={{ color: COLOR.gray3 }}>押金</span>
                <span className="font-bold" style={{ color: COLOR.indigo }}>¥{fmtMoney(contract.deposit)}</span>
              </div>
              <div className="flex justify-between">
                <span style={{ color: COLOR.gray3 }}>缴费周期</span>
                <span style={{ color: COLOR.gray4 }}>{contract.chargeCycle === 'monthly' ? '月付' : contract.chargeCycle === 'quarterly' ? '季付' : contract.chargeCycle === 'half-yearly' ? '半年付' : '年付'}</span>
              </div>
              <div className="flex justify-between">
                <span style={{ color: COLOR.gray3 }}>合约状态</span>
                <span className="font-bold" style={{ color: contract.status === 'active' ? COLOR.success : COLOR.gray3 }}>{contract.status === 'active' ? '履约中' : '已结束'}</span>
              </div>
            </div>
            {contract.remark && <div className="mt-3 pt-3 text-[11px]" style={{ borderTop: '1px solid #F1F5F9', color: COLOR.gray3 }}>{contract.remark}</div>}

            {/* 退租按钮（仅在合约履约中且有租客时显示） */}
            {contract.status === 'active' && tenant && tenant.status === '正常' && (
              <button
                onClick={() => nav('/move-out/' + id)}
                className="w-full mt-3 py-2.5 rounded-[12px] font-medium text-[13px] active:scale-[0.98] transition"
                style={{ background: COLOR.dangerSoft, color: COLOR.danger }}
              >租客退租</button>
            )}
          </div>
        )}

        {room.feeItems && room.feeItems.length > 0 && (
          <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>计费规则</h3>
            <div className="space-y-1.5">
              {room.feeItems.map((it, i) => (
                <div key={i} className="flex items-start justify-between p-2.5 rounded-[12px]" style={{ background: COLOR.gray1 }}>
                  <div className="flex-1 min-w-0">
                    <div className="text-[12px] font-semibold" style={{ color: COLOR.gray4 }}>{it.feeType}</div>
                    <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>{it.chargeCycle ? `${it.chargeCycle} · ` : ''}按 {it.chargeType === 'fixed' ? '固定金额' : '比例分摊'}</div>
                    {it.remark && <div className="text-[10px] mt-1" style={{ color: COLOR.gray3 }}>{it.remark}</div>}
                  </div>
                  <div className="text-right ml-2">
                    {it.chargeType === 'fixed' ? (
                      <div className="text-[13px] font-bold" style={{ color: COLOR.success }}>¥{fmtMoney(it.chargeValue)}</div>
                    ) : (
                      <div className="text-[13px] font-bold" style={{ color: COLOR.warn }}>{Math.round(it.chargeValue * 100)}%</div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {bills.length > 0 && (
          <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>账单记录（{bills.length}）</h3>
            <div className="space-y-2">
              {bills.map((b) => (
                <div key={b.id} className="flex items-center justify-between p-2.5 rounded-[12px]" style={{ background: COLOR.gray1 }}>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <div className="text-[12px] font-semibold" style={{ color: COLOR.gray4 }}>{b.billName}</div>
                      <span className="text-[9px] px-1.5 py-0.5 rounded font-medium" style={{
                        background: b.status === 'paid' ? COLOR.successSoft : b.status === 'overdue' ? COLOR.dangerSoft : b.status === 'deposit_paid' ? '#EEF2FF' : COLOR.warnSoft,
                        color: b.status === 'paid' ? COLOR.success : b.status === 'overdue' ? COLOR.danger : b.status === 'deposit_paid' ? COLOR.indigo : COLOR.warn
                      }}>{b.status === 'paid' ? '已支付' : b.status === 'overdue' ? '逾期' : b.status === 'deposit_paid' ? '押金' : '待支付'}</span>
                    </div>
                    <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>
                      {b.periodStart ? `${b.periodStart} ~ ${b.periodEnd}` : `到期 ${b.dueDate}`}
                    </div>
                  </div>
                  <div className="text-[13px] font-bold ml-2" style={{ color: COLOR.success }}>+¥{fmtMoney(b.amount)}</div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
