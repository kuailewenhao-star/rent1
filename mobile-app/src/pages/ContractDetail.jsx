import { COLOR } from '../data/colors.js';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getContractById, getTenantById, getRoomById, getHouseById,
  getIncomeBillsByRoom, fmtMoney, maskPhone, maskName
} from '../data/mock.js';
import { useData } from '../data/context.jsx';

export default function ContractDetail() {
  const nav = useNavigate();
  const { id } = useParams();
  const { contracts: ctxContracts } = useData();
  const contract = ctxContracts.find(c => c.id === id);

  if (!contract) {
    return <div className="p-6 text-center" style={{ color: COLOR.gray3 }}>找不到合约</div>;
  }

  const tenant = getTenantById(contract.tenantId);
  const room = getRoomById(contract.roomId);
  const house = getHouseById(contract.houseId);
  const bills = getIncomeBillsByRoom(contract.roomId);
  const remainDays = Math.max(0, Math.ceil((new Date(contract.endDate) - new Date()) / (1000 * 60 * 60 * 24)));
  const isExpiringSoon = remainDays <= 30 && contract.status === 'active';

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>合约详情</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {/* 合约状态 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div className="flex items-center gap-2 mb-3">
            <h2 className="text-[16px] font-bold" style={{ color: COLOR.gray4 }}>{house?.name} · {room?.roomNo}</h2>
            <span className="text-[10px] px-1.5 py-0.5 rounded font-medium" style={{ background: contract.status === 'active' ? COLOR.successSoft : COLOR.gray1, color: contract.status === 'active' ? COLOR.success : COLOR.gray3 }}>
              {contract.status === 'active' ? '履约中' : contract.status === 'ended' ? '已到期完结' : contract.status === 'cancelled' ? '提前解约' : '作废'}
            </span>
          </div>
          <div className="grid grid-cols-2 gap-3 text-[12px]">
            <div>
              <div style={{ color: COLOR.gray3 }}>合约期</div>
              <div className="mt-0.5 font-semibold" style={{ color: COLOR.gray4 }}>{contract.startDate} ~ {contract.endDate}</div>
            </div>
            <div>
              <div style={{ color: COLOR.gray3 }}>租客</div>
              <div className="mt-0.5 font-semibold" style={{ color: COLOR.gray4 }}>{tenant?.maskName} {maskPhone(tenant?.phone)}</div>
            </div>
            <div>
              <div style={{ color: COLOR.gray3 }}>月租金</div>
              <div className="mt-0.5 font-semibold" style={{ color: COLOR.success }}>¥{fmtMoney(contract.monthRent)}</div>
            </div>
            <div>
              <div style={{ color: COLOR.gray3 }}>押金</div>
              <div className="mt-0.5 font-semibold" style={{ color: COLOR.indigo }}>¥{fmtMoney(contract.deposit)}</div>
            </div>
          </div>
          {isExpiringSoon && (
            <div className="mt-3 p-2.5 rounded-[12px] text-[12px] font-medium text-center" style={{ background: COLOR.warnSoft, color: COLOR.warn }}>
              ⏰ 合约将于 {remainDays} 天后到期
            </div>
          )}
          {contract.remark && <div className="mt-3 pt-3 text-[11px]" style={{ borderTop: '1px solid #F1F5F9', color: COLOR.gray3 }}>{contract.remark}</div>}
        </div>

        {/* 账单记录 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>关联账单（{bills.length} 笔）</h3>
          <div className="space-y-2">
            {bills.map((b) => {
              const statusMap = {
                paid: { label: '已支付', color: COLOR.success, bg: COLOR.successSoft },
                pending: { label: '待支付', color: COLOR.warn, bg: COLOR.warnSoft },
                overdue: { label: '逾期未付', color: COLOR.danger, bg: COLOR.dangerSoft },
                deposit_paid: { label: '押金已收', color: COLOR.indigo, bg: COLOR.indigoSoft }
              };
              const st = statusMap[b.status];
              return (
                <div key={b.id} className="flex items-center justify-between p-2.5 rounded-[12px] tap-feedback" style={{ background: COLOR.gray1 }} onClick={() => nav('/bill/' + b.id)}>
                  <div className="flex-1 min-w-0">
                    <div className="text-[12px] font-semibold" style={{ color: COLOR.gray4 }}>{b.billName}</div>
                    <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>{b.periodStart || `到期 ${b.dueDate}`}</div>
                  </div>
                  <div className="text-right ml-2">
                    <div className="text-[12px] font-bold" style={{ color: COLOR.success }}>¥{fmtMoney(b.amount)}</div>
                    <span className="text-[9px] px-1 py-0.5 rounded font-medium" style={{ background: st.bg, color: st.color }}>{st.label}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* 操作按钮 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div className="flex gap-2">
            {contract.status === 'active' && (
              <button className="flex-1 py-2.5 rounded-[14px] font-medium text-[13px] active:scale-95 transition" style={{ background: COLOR.successSoft, color: COLOR.success }}>
                ✓ 标记完结
              </button>
            )}
            <button className="flex-1 py-2.5 rounded-[14px] font-medium text-[13px] active:scale-95 transition" style={{ background: COLOR.brandSoft, color: COLOR.brand }}>
              编辑合约
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
