import { COLOR } from '../data/colors.js';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getContracts, terminateContract, voidContract } from '../api/contract.js';
import { fmtMoney, maskPhone } from '../data/mock.js';


const STATUS_MAP = {
  ACTIVE: { label: '履约中', color: COLOR.success, bg: COLOR.successSoft },
  EXPIRED: { label: '已到期完结', color: COLOR.gray3, bg: COLOR.gray1 },
  TERMINATED: { label: '提前解约', color: COLOR.warn, bg: COLOR.warnSoft },
  VOID: { label: '作废', color: COLOR.danger, bg: COLOR.dangerSoft }
};

export default function Contracts() {
  const nav = useNavigate();
  const [contracts, setContracts] = useState([]);
  const [loading, setLoading] = useState(true);

  // 加载合约列表
  const loadContracts = async () => {
    try {
      setLoading(true);
      const res = await getContracts({ status: 'ACTIVE', page: 1, pageSize: 100 });
      if (res.success && res.data) {
        setContracts(res.data);
      }
    } catch (error) {
      console.error('加载合约列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContracts();
  }, []);

  const activeOnes = contracts.filter(c => c.status === 'ACTIVE');
  const total = activeOnes.reduce((s, c) => s + (c.monthRent || 0), 0);

  const handleTerminate = async (contractId) => {
    if (!confirm('确认将此合约标记为提前解约？')) return;
    try {
      await terminateContract(contractId, { terminationType: 'LANDLORD_REFUND' });
      alert('解约成功');
      loadContracts();
    } catch (error) {
      console.error('解约失败:', error);
      alert('解约失败: ' + error.message);
    }
  };

  const handleVoid = async (contractId) => {
    if (!confirm('确认作废此合约？')) return;
    try {
      await voidContract(contractId, '作废');
      alert('作废成功');
      loadContracts();
    } catch (error) {
      console.error('作废失败:', error);
      alert('作废失败: ' + error.message);
    }
  };

  return (
    <div className="min-h-screen pb-24 relative" style={{ background: COLOR.gray1 }}>
      {loading && (
        <div className="flex items-center justify-center py-20">
          <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
        </div>
      )}
      <div className="px-5 pt-8 pb-4 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <h1 className="text-[20px] font-bold" style={{ color: COLOR.gray4 }}>合约管理</h1>
        <p className="text-[12px] mt-1" style={{ color: COLOR.gray3 }}>租约生效后锁定计费规则 · 关联房源、房间与租客</p>
      </div>

      <div className="px-5 pt-4">
        <div className="bg-white rounded-[20px] p-3 flex items-center justify-between shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
          <div>
            <div className="text-[11px]" style={{ color: COLOR.gray3 }}>生效合约</div>
            <div className="text-[16px] font-bold mt-0.5" style={{ color: COLOR.brand }}>{activeOnes.length} 份</div>
          </div>
          <div className="text-right">
            <div className="text-[11px]" style={{ color: COLOR.gray3 }}>月租总收入</div>
            <div className="text-[16px] font-bold mt-0.5" style={{ color: COLOR.success }}>¥{fmtMoney(total)}</div>
          </div>
        </div>

        <div className="mt-3 space-y-3">
          {contracts.map((c) => {
            const st = STATUS_MAP[c.status] || STATUS_MAP.ACTIVE;
            return (
              <div key={c.contractId} className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-2.5 flex-1 min-w-0">
                    <div className="w-9 h-9 rounded-[12px] flex items-center justify-center text-base flex-shrink-0" style={{ background: COLOR.brandSoft }}>📄</div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5 flex-wrap">
                        <h3 className="text-[13px] font-bold truncate" style={{ color: COLOR.gray4 }}>房间 {c.roomId}</h3>
                        <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium" style={{ background: st.bg, color: st.color }}>{st.label}</span>
                      </div>
                      <div className="text-[11px] mt-0.5" style={{ color: COLOR.gray3 }}>{c.tenantName || '租客'} · {maskPhone(c.tenantPhone || '')}</div>
                    </div>
                  </div>
                  <div className="text-right flex-shrink-0 ml-2">
                    <div className="text-[13px] font-bold" style={{ color: COLOR.success }}>¥{fmtMoney(c.monthRent)}/月</div>
                    <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>押金 ¥{fmtMoney(c.deposit)}</div>
                  </div>
                </div>

                <div className="mt-3 pt-3 text-[11px]" style={{ borderTop: '1px solid #F1F5F9', color: COLOR.gray3 }}>
                  合约期 {c.startDate} ~ {c.endDate}
                </div>

                {/* 操作按钮 */}
                <div className="mt-3 pt-3 flex items-center gap-1.5 flex-wrap" style={{ borderTop: '1px solid #F1F5F9' }}>
                  {c.status === 'ACTIVE' && (
                    <button
                      onClick={() => handleTerminate(c.contractId)}
                      className="text-[11px] px-3 py-1.5 rounded-[10px] font-medium active:scale-95 transition"
                      style={{ background: COLOR.warnSoft, color: COLOR.warn }}
                    >提前解约</button>
                  )}
                  <button
                    onClick={() => nav('/contract-detail/' + c.contractId)}
                    className="text-[11px] px-3 py-1.5 rounded-[10px] font-medium active:scale-95 transition"
                    style={{ background: COLOR.brandSoft, color: COLOR.brand }}
                  >详情</button>
                  {c.status === 'ACTIVE' && (
                    <button
                      onClick={() => handleVoid(c.contractId)}
                      className="text-[11px] px-3 py-1.5 rounded-[10px] font-medium active:scale-95 transition"
                      style={{ background: COLOR.dangerSoft, color: COLOR.danger }}
                    >作废</button>
                  )}
                </div>
              </div>
            );
          })}
          {contracts.length === 0 && !loading && (
            <div className="text-center py-20" style={{ color: COLOR.gray3 }}>
              暂无合约数据
            </div>
          )}
        </div>
      </div>

      <button
        onClick={() => nav('/create-contract')}
        className="fixed right-5 bottom-[110px] w-14 h-14 rounded-full shadow-[0_8px_24px_rgba(37,99,235,0.4)] flex items-center justify-center text-white font-bold text-[28px] active:scale-95 transition z-20"
        style={{ background: COLOR.brand }}
      >+</button>
    </div>
  );
}
