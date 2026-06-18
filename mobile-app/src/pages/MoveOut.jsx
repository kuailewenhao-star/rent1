import { COLOR } from '../data/colors.js';
import { useNavigate, useParams } from 'react-router-dom';
import { useState } from 'react';
import { useData } from '../data/context.jsx';
import { todayISO } from '../data/mock.js';

export default function MoveOut() {
  const nav = useNavigate();
  const { id } = useParams();
  const { rooms, tenants, contracts, moveOutByRoom } = useData();

  const room = rooms.find(r => r.id === id);
  const tenant = tenants.find(t => t.roomId === id);
  const contract = contracts.find(c => c.roomId === id && c.status === 'active');

  const totalDeposit = contract?.deposit || 0;

  const [refundAmount, setRefundAmount] = useState(totalDeposit);
  const [remark, setRemark] = useState('');

  const deductionAmount = Math.max(0, totalDeposit - refundAmount);

  const handleSubmit = () => {
    if (!tenant || !contract) {
      alert('该房间无有效租客或合约');
      return;
    }
    if (refundAmount < 0) {
      alert('退还金额不能为负数');
      return;
    }
    if (refundAmount > totalDeposit) {
      alert('退还金额不能超过押金总额');
      return;
    }
    if (deductionAmount > 0 && !remark) {
      alert('存在扣费时必须填写扣费备注');
      return;
    }

    if (!confirm(`确认退租？\n退还押金：¥${refundAmount}\n扣留押金：¥${deductionAmount}\n扣留金额将作为收入`)) {
      return;
    }

    moveOutByRoom(contract.id, refundAmount, deductionAmount, remark);
    alert(`退租成功！\n已退还押金：¥${refundAmount}\n扣留转为收入：¥${deductionAmount}`);
    nav('/room/' + id);
  };

  if (!room) {
    return <div className="p-6 text-center" style={{ color: COLOR.gray3 }}>找不到房间</div>;
  }

  if (!tenant || !contract) {
    return (
      <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
        <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
          <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
            <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
          </button>
          <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>租客退租</h1>
        </div>
        <div className="p-6 text-center" style={{ color: COLOR.gray3 }}>该房间暂无有效租客</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>租客退租</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {/* 租客与合约信息 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>租客与合约</h3>
          <div className="space-y-2 text-[12px]">
            <div className="flex justify-between">
              <span style={{ color: COLOR.gray3 }}>租客姓名</span>
              <span style={{ color: COLOR.gray4, fontWeight: 600 }}>{tenant.name}</span>
            </div>
            <div className="flex justify-between">
              <span style={{ color: COLOR.gray3 }}>房间号</span>
              <span style={{ color: COLOR.gray4 }}>{room.roomNo}</span>
            </div>
            <div className="flex justify-between">
              <span style={{ color: COLOR.gray3 }}>合约期</span>
              <span style={{ color: COLOR.gray4 }}>{contract.startDate} ~ {contract.endDate}</span>
            </div>
            <div className="flex justify-between">
              <span style={{ color: COLOR.gray3 }}>押金总额</span>
              <span className="font-bold" style={{ color: COLOR.indigo }}>¥{totalDeposit}</span>
            </div>
            <div className="flex justify-between">
              <span style={{ color: COLOR.gray3 }}>退租日期</span>
              <span style={{ color: COLOR.gray4 }}>{todayISO()}</span>
            </div>
          </div>
        </div>

        {/* 押金结算 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>押金结算</h3>
          <div className="space-y-3">
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>退还押金金额</div>
              <input type="number" value={refundAmount} onChange={(e) => setRefundAmount(Number(e.target.value))} className="w-full p-3 rounded-[14px] border text-[13px]" style={{ borderColor: '#E2E8F0', background: COLOR.gray1 }} placeholder="0" />
            </div>
            <div>
              <div className="text-[11px] mb-1" style={{ color: COLOR.gray3 }}>扣费备注 <span style={{ color: COLOR.danger }}>*</span></div>
              <textarea
                value={remark}
                onChange={(e) => setRemark(e.target.value)}
                className="w-full p-3 rounded-[14px] border text-[13px] resize-none"
                style={{ borderColor: '#E2E8F0', background: COLOR.gray1, height: 80 }}
                placeholder={deductionAmount > 0 ? "请详细说明扣费原因，如：房屋损坏维修费、清洁费、欠租等" : "若无扣费可不填"}
              />
            </div>
          </div>
        </div>

        {/* 结算汇总 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>结算汇总</h3>
          <div className="space-y-2 text-[12px]">
            <div className="flex justify-between">
              <span style={{ color: COLOR.gray3 }}>押金总额</span>
              <span style={{ color: COLOR.gray4, fontWeight: 600 }}>¥{totalDeposit}</span>
            </div>
            <div className="flex justify-between">
              <span style={{ color: COLOR.gray3 }}>退还租客</span>
              <span className="font-bold" style={{ color: COLOR.brand }}>-¥{refundAmount}</span>
            </div>
            <div className="flex justify-between">
              <span style={{ color: COLOR.gray3 }}>扣留转为收入</span>
              <span className="font-bold" style={{ color: COLOR.success }}>+¥{deductionAmount}</span>
            </div>
            <div className="pt-2 mt-2" style={{ borderTop: '1px dashed #E2E8F0' }}>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>
                {deductionAmount > 0
                  ? `扣留金额 ¥${deductionAmount} 将作为"扣留押金收入"自动生成收入账单`
                  : '本次退租无扣费，全额退还押金'}
              </div>
            </div>
          </div>
        </div>

        <button
          onClick={handleSubmit}
          className="w-full py-3.5 rounded-[14px] font-semibold text-[15px] text-white active:scale-[0.98] transition"
          style={{ background: COLOR.danger, boxShadow: '0 8px 24px rgba(239,68,68,0.25)' }}
        >确认退租</button>
      </div>
    </div>
  );
}
