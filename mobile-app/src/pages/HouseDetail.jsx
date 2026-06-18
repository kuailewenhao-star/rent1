import { COLOR } from '../data/colors.js';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getHouseById, getRoomsByHouse, getIncomeBillsByHouse, getExpenseBillsByHouse, fmtMoney } from '../data/mock.js';


export default function HouseDetail() {
  const nav = useNavigate();
  const { id } = useParams();
  const house = getHouseById(id);

  if (!house) {
    return <div className="p-6 text-center" style={{ color: COLOR.gray3 }}>找不到房源</div>;
  }

  const rooms = getRoomsByHouse(id);
  const income = getIncomeBillsByHouse(id);
  const expense = getExpenseBillsByHouse(id);

  const totalIncome = income.reduce((s, b) => s + b.amount, 0);
  const totalExpense = expense.reduce((s, b) => s + b.amount, 0);

  return (
    <div className="min-h-screen pb-12" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-4 pb-3 sticky top-0 z-10 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)] flex items-center gap-3">
        <button onClick={() => nav(-1)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.gray1 }}>
          <span style={{ color: COLOR.gray4, fontSize: 18 }}>←</span>
        </button>
        <h1 className="text-[16px] font-bold flex-1" style={{ color: COLOR.gray4 }}>房源详情</h1>
      </div>

      <div className="px-5 pt-4 space-y-3">
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div className="flex items-center gap-2">
            <h2 className="text-[18px] font-bold" style={{ color: COLOR.gray4 }}>{house.name}</h2>
            <span className="text-[10px] px-1.5 py-0.5 rounded font-medium" style={{ background: COLOR.brandSoft, color: COLOR.brand }}>{house.type}</span>
            <span className="text-[10px] px-1.5 py-0.5 rounded font-medium" style={{ background: COLOR.successSoft, color: COLOR.success }}>{house.status}</span>
          </div>
          <p className="text-[12px] mt-2" style={{ color: COLOR.gray3 }}>{house.address}</p>
          <div className="grid grid-cols-3 gap-2 mt-3 pt-3 text-center" style={{ borderTop: '1px solid #F1F5F9' }}>
            <div>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>户型</div>
              <div className="text-[14px] font-bold mt-0.5" style={{ color: COLOR.gray4 }}>{house.layout}</div>
            </div>
            <div>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>面积</div>
              <div className="text-[14px] font-bold mt-0.5" style={{ color: COLOR.gray4 }}>{house.area}㎡</div>
            </div>
            <div>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>月租金</div>
              <div className="text-[14px] font-bold mt-0.5" style={{ color: COLOR.gray4 }}>¥{fmtMoney(house.monthlyCost)}</div>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div className="grid grid-cols-3 gap-2 text-center">
          <div>
            <div className="text-[11px]" style={{ color: COLOR.gray3 }}>收入</div>
            <div className="text-[15px] font-bold mt-1" style={{ color: COLOR.success }}>¥{fmtMoney(totalIncome)}</div>
          </div>
          <div>
            <div className="text-[11px]" style={{ color: COLOR.gray3 }}>支出</div>
            <div className="text-[15px] font-bold mt-1" style={{ color: COLOR.danger }}>¥{fmtMoney(totalExpense)}</div>
          </div>
          <div>
            <div className="text-[11px]" style={{ color: COLOR.gray3 }}>净盈利</div>
            <div className="text-[15px] font-bold mt-1" style={{ color: COLOR.brand }}>¥{fmtMoney(totalIncome - totalExpense)}</div>
          </div>
        </div>
        <div className="mt-3 pt-3 text-[11px]" style={{ borderTop: '1px solid #F1F5F9', color: COLOR.gray3 }}>
          房源签约期 {house.leaseStart} ~ {house.leaseEnd}
        </div>
        </div>

        <div>
          <h3 className="text-[13px] font-semibold mb-2 px-1" style={{ color: COLOR.gray4 }}>房间 · 共 {rooms.length} 间</h3>
          {rooms.map((r) => (
            <div key={r.id} className="bg-white rounded-[20px] p-3 mb-2 shadow-[0_2px_8px_rgba(15,23,42,0.04)] tap-feedback" onClick={() => nav('/room/' + r.id)}>
            <div className="flex items-center justify-between">
              <div>
                <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>{r.roomNo}</div>
                <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>{r.area}㎡ · ¥{fmtMoney(r.rent)}/月</div>
              </div>
              <div>
                <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium" style={{ background: r.status === 'rented' ? COLOR.successSoft : COLOR.gray1, color: r.status === 'rented' ? COLOR.success : COLOR.gray3 }}>{r.status === 'rented' ? '已出租' : '空置中'}</span>
              </div>
            </div>
          </div>
          ))}
        </div>

        <div className="text-[11px] py-3 text-center" style={{ color: COLOR.gray3 }}>{house.remark}</div>
      </div>
    </div>
  );
}
