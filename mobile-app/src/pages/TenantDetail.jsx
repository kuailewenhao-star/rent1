import { COLOR } from '../data/colors.js';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getTenantById, getRoomById, getContractByRoom, getIncomeBillsByRoom, fmtMoney, maskPhone, maskName
} from '../data/mock.js';

export default function TenantDetail() {
  const nav = useNavigate();
  const { id } = useParams();
  const tenant = getTenantById(id);

  if (!tenant) {
    return <div className="p-6 text-center" style={{ color: COLOR.gray3 }}>找不到租客</div>;
  }

  const room = getRoomById(tenant.roomId);
  const house = room ? getHouseById(room.houseId) : null;
  const contract = getContractByRoom(tenant.roomId);
  const bills = getIncomeBillsByRoom(tenant.roomId);

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>租客详情</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {/* 基本信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-lg" style={{ background: COLOR.brand }}>{tenant.maskName[0]}</div>
            <div className="flex-1 min-w-0">
              <div className="text-[15px] font-semibold" style={{ color: COLOR.gray4 }}>{tenant.maskName}</div>
              <div className="text-[12px] mt-0.5" style={{ color: COLOR.gray3 }}>{maskPhone(tenant.phone)}</div>
              <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium mt-1 inline-block" style={{ background: tenant.status === '正常' ? COLOR.successSoft : COLOR.dangerSoft, color: tenant.status === '正常' ? COLOR.success : COLOR.danger }}>{tenant.status}</span>
            </div>
          </div>
        </div>

        {/* 承租信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>承租信息</h3>
          <div className="space-y-2 text-[12px]">
            <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>所属房源</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{house?.name}</span></div>
            <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>房间</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{room?.roomNo}</span></div>
            <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>入住日期</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{tenant.joinDate}</span></div>
            {contract && <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>合约到期</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{contract.endDate}</span></div>}
          </div>
        </div>

        {/* 紧急联系人（PRD 4.3.5） */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>紧急联系人</h3>
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.brandSoft, color: COLOR.brand, fontSize: 14 }}>👤</div>
            <div>
              <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>{maskName(tenant.emergencyContact)}</div>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>{maskPhone(tenant.emergencyPhone)}</div>
            </div>
          </div>
        </div>

        {/* 账单记录 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>账单记录（{bills.length} 笔）</h3>
          <div className="space-y-2">
            {bills.map((b) => (
              <div key={b.id} className="flex items-center justify-between p-2.5 rounded-[12px] tap-feedback" style={{ background: COLOR.gray1 }} onClick={() => nav('/bill/' + b.id)}>
                <div className="flex-1 min-w-0">
                  <div className="text-[12px] font-semibold" style={{ color: COLOR.gray4 }}>{b.billName}</div>
                  <div className="text-[10px]" style={{ color: COLOR.gray3 }}>{b.periodStart || b.dueDate}</div>
                </div>
                <div className="text-[12px] font-bold" style={{ color: COLOR.success }}>¥{fmtMoney(b.amount)}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
