import { COLOR } from '../data/colors.js';
import { useNavigate, useParams } from 'react-router-dom';
import {
  incomeBills, tenants, houseSources, rooms, fmtMoney
} from '../data/mock.js';


export default function BillDetail() {
  const nav = useNavigate();
  const { id } = useParams();
  const bill = incomeBills.find((b) => b.id === id);

  if (!bill) {
    return <div className="p-6 text-center" style={{ color: COLOR.gray3 }}>找不到账单</div>;
  }

  const tenant = tenants.find((t) => t.id === bill.tenantId);
  const house = houseSources.find((h) => h.id === bill.houseId);
  const room = rooms.find((r) => r.id === bill.roomId);

  const statusMap = {
    paid: { label: '已支付', color: COLOR.success, bg: COLOR.successSoft },
    pending: { label: '待支付', color: COLOR.warn, bg: COLOR.warnSoft },
    overdue: { label: '逾期未付', color: COLOR.danger, bg: COLOR.dangerSoft },
    deposit_paid: { label: '押金已收', color: COLOR.indigo, bg: COLOR.indigoSoft }
  };
  const st = statusMap[bill.status];

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>账单详情</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        <div className="bg-white rounded-[20px] p-5 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div className="flex items-center justify-between">
            <div className="text-[12px]" style={{ color: COLOR.gray3 }}>账单金额</div>
            <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium" style={{ background: st.bg, color: st.color }}>{st.label}</span>
          </div>
          <div className="mt-1 text-[28px] font-bold" style={{ color: COLOR.success }}>+¥{fmtMoney(bill.amount)}</div>
          <div className="mt-1 text-[11px]" style={{ color: COLOR.gray3 }}>{bill.billName}</div>
        </div>

        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>账单信息</h3>
          <div className="space-y-2 text-[12px]">
            <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>账单类型</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{bill.billName}</span></div>
            {bill.periodStart && <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>账期</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{bill.periodStart} ~ {bill.periodEnd}</span></div>}
            <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>到期日</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{bill.dueDate}</span></div>
            {bill.paidDate && <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>付款日期</span><span className="font-semibold" style={{ color: COLOR.success }}>{bill.paidDate}</span></div>}
            <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>创建时间</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{bill.createTime}</span></div>
          </div>
        </div>

        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>关联信息</h3>
          <div className="space-y-2 text-[12px]">
            <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>房源</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{house?.name}</span></div>
            <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>房间</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{room?.roomNo}</span></div>
            {tenant && <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>租客</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{tenant.maskName}</span></div>}
            <div className="flex justify-between"><span style={{ color: COLOR.gray3 }}>账单编号</span><span className="font-semibold" style={{ color: COLOR.gray4 }}>{bill.id}</span></div>
          </div>
        </div>
      </div>
    </div>
  );
}
