import { COLOR } from '../data/colors.js';
import { useNavigate } from 'react-router-dom';
import { profile, fmtMoney, maskPhone } from '../data/mock.js';

export default function TenantProfile() {
  const nav = useNavigate();

  return (
    <div className="min-h-screen pb-24" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-8 pb-5" style={{ background: `linear-gradient(180deg, ${COLOR.brand} 0%, #1D4ED8 100%)` }}>
        <div className="flex items-center gap-3">
          <div className="w-14 h-14 rounded-full bg-white/25 backdrop-blur flex items-center justify-center text-white font-bold text-xl">{profile.avatarInitial}</div>
          <div className="flex-1">
            <div className="text-white text-[17px] font-bold">{profile.maskName}</div>
            <div className="text-white/80 text-[12px] mt-1">租客身份</div>
          </div>
          <button onClick={() => { localStorage.removeItem('userRole'); localStorage.removeItem('isLoggedIn'); nav('/login'); }} className="text-white/80 text-[12px] px-3 py-1.5 rounded-full bg-white/15">退出</button>
        </div>
      </div>

      <div className="px-5 pt-4 space-y-3">
        {/* 我的房间 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>我的房间</h3>
          <div className="flex items-center gap-3 p-3 rounded-[14px]" style={{ background: COLOR.brandSoft }}>
            <div className="w-10 h-10 rounded-[12px] bg-white flex items-center justify-center text-lg">🔑</div>
            <div className="flex-1">
              <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>阳光小区3-201 · 整套</div>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>月租 ¥3,200 · 合约至 2026-12-31</div>
            </div>
          </div>
        </div>

        {/* 我的押金 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-2" style={{ color: COLOR.gray4 }}>有效押金</h3>
          <div className="text-[22px] font-bold" style={{ color: COLOR.indigo }}>¥{fmtMoney(6400)}</div>
          <div className="text-[11px] mt-1" style={{ color: COLOR.gray3 }}>退租时无违约全额退还</div>
        </div>

        {/* 紧急联系人 */}
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <h3 className="text-[13px] font-bold mb-3" style={{ color: COLOR.gray4 }}>紧急联系人</h3>
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: COLOR.brandSoft, color: COLOR.brand, fontSize: 14 }}>👤</div>
            <div>
              <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>张**</div>
              <div className="text-[11px]" style={{ color: COLOR.gray3 }}>{maskPhone('13900001111')}</div>
            </div>
          </div>
        </div>

        {/* 菜单 */}
        <div className="bg-white rounded-[20px] shadow-[0_2px_8px_rgba(15,23,42,0.04)] overflow-hidden">
          {[
            { label: '我的合约', desc: '查看合约详情', icon: '📄' },
            { label: '缴费记录', desc: '历史缴费明细', icon: '📋' },
            { label: '个人信息', desc: '修改个人资料', icon: '👤' },
          ].map((item, i) => (
            <button key={item.label} className="w-full flex items-center gap-3 px-4 py-3.5 active:bg-slate-50" style={i < 2 ? { borderBottom: '1px solid #F1F5F9' } : {}}>
              <div className="w-9 h-9 rounded-[12px] flex items-center justify-center text-lg" style={{ background: COLOR.brandSoft }}>{item.icon}</div>
              <div className="flex-1 text-left">
                <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>{item.label}</div>
                <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>{item.desc}</div>
              </div>
              <span style={{ color: COLOR.gray3 }}>›</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
