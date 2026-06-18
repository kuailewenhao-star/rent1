import { COLOR, TYPE_COLORS } from '../data/colors.js';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getIncomeInvoices, verifyPayment } from '../api/invoice.js';
import { getExpenseInvoices, deleteExpenseInvoice } from '../api/expense.js';
import { INCOME_FEE_TYPES, EXPENSE_TYPES, fmtMoney, todayISO } from '../data/mock.js';

const TAB_LIST = [
  { id: 'income', label: '收入账单' },
  { id: 'expense', label: '支出账单' }
];

export default function Bill() {
  const nav = useNavigate();
  const [tab, setTab] = useState('income');

  return (
    <div className="min-h-screen pb-24 relative" style={{ background: COLOR.gray1 }}>
      <div className="px-5 pt-8 pb-4 bg-white shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <h1 className="text-[20px] font-bold" style={{ color: COLOR.gray4 }}>账单管理</h1>
        <p className="text-[12px] mt-1" style={{ color: COLOR.gray3 }}>收支双账体系 · 支持标记已支付 / 新增 / 编辑 / 删除</p>
      </div>

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

      <div className="px-5 pt-4">
        {tab === 'income' ? <IncomeList nav={nav} /> : <ExpenseList nav={nav} />}
      </div>

      {/* 悬浮新增 */}
      <button
        onClick={() => {
          if (tab === 'income') {
            nav('/create-contract');
          } else {
            nav('/expense-form');
          }
        }}
        className="fixed right-5 bottom-[110px] w-14 h-14 rounded-full shadow-[0_8px_24px_rgba(37,99,235,0.4)] flex items-center justify-center text-white font-bold text-[28px] active:scale-95 transition z-20"
        style={{ background: COLOR.brand }}
      >+</button>
    </div>
  );
}

/* ========== 收入账单 ========== */
function IncomeList({ nav }) {
  const [ib, setIb] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      const res = await getIncomeInvoices({ page: 1, pageSize: 100 });
      if (res.success && res.data) {
        setIb(res.data);
      }
    } catch (error) {
      console.error('加载收入账单失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const total = ib.reduce((s, b) => s + (b.amount || 0), 0);
  const pendingAmt = ib.filter(b => b.status === 'PENDING').reduce((s, b) => s + (b.amount || 0), 0);
  const overdueAmt = ib.filter(b => b.status === 'OVERDUE').reduce((s, b) => s + (b.amount || 0), 0);
  const paidAmt = ib.filter(b => b.status === 'PAID').reduce((s, b) => s + (b.amount || 0), 0);

  const statusMap = {
    PAID: { label: '已支付', color: COLOR.success, bg: COLOR.successSoft },
    PENDING: { label: '待支付', color: COLOR.warn, bg: COLOR.warnSoft },
    OVERDUE: { label: '逾期未付', color: COLOR.danger, bg: COLOR.dangerSoft },
    DEPOSIT_RECEIVED: { label: '押金已收', color: COLOR.indigo, bg: COLOR.indigoSoft }
  };

  const handleMarkPaid = async (invoiceId) => {
    if (!confirm('确认将此账单标记为已支付？')) return;
    try {
      await verifyPayment(invoiceId, todayISO());
      alert('标记成功');
      loadData();
    } catch (error) {
      console.error('标记失败:', error);
      alert('标记失败: ' + error.message);
    }
  };

  return (
    <div className="space-y-3">
      {loading && (
        <div className="flex items-center justify-center py-10">
          <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
        </div>
      )}
      {/* 统计 */}
      <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <div className="text-[11px]" style={{ color: COLOR.gray3 }}>累计收入</div>
        <div className="mt-1 text-[22px] font-bold" style={{ color: COLOR.success }}>¥{fmtMoney(total)}</div>
        <div className="grid grid-cols-3 gap-2 mt-3 pt-3" style={{ borderTop: '1px solid #F1F5F9' }}>
          <div className="text-center">
            <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>已收款</div>
            <div className="text-[13px] font-bold mt-0.5" style={{ color: COLOR.success }}>¥{fmtMoney(paidAmt)}</div>
          </div>
          <div className="text-center">
            <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>待支付</div>
            <div className="text-[13px] font-bold mt-0.5" style={{ color: COLOR.warn }}>¥{fmtMoney(pendingAmt)}</div>
          </div>
          <div className="text-center">
            <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>逾期</div>
            <div className="text-[13px] font-bold mt-0.5" style={{ color: COLOR.danger }}>¥{fmtMoney(overdueAmt)}</div>
          </div>
        </div>
      </div>

      {/* 费用类型汇总 */}
      <div className="bg-white rounded-[20px] p-3 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <div className="text-[11.5px] font-semibold mb-2" style={{ color: COLOR.gray4 }}>按费用类型</div>
        <div className="flex flex-wrap gap-1.5">
          {INCOME_FEE_TYPES.map((t) => {
            const sum = ib.filter(b => b.feeType === t.id).reduce((s, b) => s + (b.amount || 0), 0);
            return (
              <div key={t.id} className="px-2 py-1 rounded-[10px]" style={{ background: t.color === 'blue' ? COLOR.brandSoft : COLOR.gray1 }}>
                <div className="text-[10px]" style={{ color: t.color === 'blue' ? COLOR.brand : COLOR.gray3 }}>{t.label}</div>
                <div className="text-[11.5px] font-bold mt-0.5" style={{ color: t.color === 'blue' ? COLOR.brand : COLOR.gray4 }}>¥{fmtMoney(sum)}</div>
              </div>
            );
          })}
        </div>
      </div>

      {ib.map((b) => {
        const st = statusMap[b.status] || statusMap.PENDING;
        const canMarkPaid = b.status === 'PENDING' || b.status === 'OVERDUE';
        return (
          <div key={b.invoiceId} className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            <div className="flex items-start justify-between">
              <div className="flex-1 min-w-0 pr-3">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="text-[14px] font-bold truncate" style={{ color: COLOR.gray4 }}>{b.feeTypeName || b.feeType}</h3>
                  <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium" style={{ background: st.bg, color: st.color }}>{st.label}</span>
                </div>
                <div className="text-[11px] mt-1" style={{ color: COLOR.gray3 }}>房间 {b.roomId}</div>
                {b.cycleStart && (
                  <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>账期 {b.cycleStart} ~ {b.cycleEnd} · 到期 {b.dueDate}</div>
                )}
              </div>
              <div className="text-right flex-shrink-0">
                <div className="text-[15px] font-bold" style={{ color: COLOR.success }}>+¥{fmtMoney(b.amount)}</div>
              </div>
            </div>

            {/* 操作按钮 */}
            <div className="mt-3 pt-3 flex items-center gap-1.5 flex-wrap" style={{ borderTop: '1px solid #F1F5F9' }}>
              {canMarkPaid && (
                <button
                  onClick={() => handleMarkPaid(b.invoiceId)}
                  className="text-[11px] px-3 py-1.5 rounded-[10px] font-medium active:scale-95 transition"
                  style={{ background: COLOR.successSoft, color: COLOR.success }}
                >✓ 标记已支付</button>
              )}
              <button
                onClick={() => nav('/bill/' + b.invoiceId)}
                className="text-[11px] px-3 py-1.5 rounded-[10px] font-medium active:scale-95 transition"
                style={{ background: COLOR.brandSoft, color: COLOR.brand }}
              >详情</button>
            </div>
          </div>
        );
      })}
      {ib.length === 0 && !loading && (
        <div className="text-center py-10" style={{ color: COLOR.gray3 }}>
          暂无收入账单
        </div>
      )}
    </div>
  );
}

/* ========== 支出账单 ========== */
function ExpenseList({ nav }) {
  const [eb, setEb] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      const res = await getExpenseInvoices({ page: 1, pageSize: 100 });
      if (res.success && res.data) {
        setEb(res.data);
      }
    } catch (error) {
      console.error('加载支出账单失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const total = eb.reduce((s, b) => s + (b.amount || 0), 0);

  const typeColorMap = TYPE_COLORS;

  return (
    <div className="space-y-3">
      {loading && (
        <div className="flex items-center justify-center py-10">
          <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
        </div>
      )}
      <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <div className="text-[11px]" style={{ color: COLOR.gray3 }}>累计支出</div>
        <div className="mt-1 text-[22px] font-bold" style={{ color: COLOR.danger }}>¥{fmtMoney(total)}</div>
      </div>

      <div className="bg-white rounded-[20px] p-3 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
        <div className="text-[11.5px] font-semibold mb-2" style={{ color: COLOR.gray4 }}>支出类型（2类）</div>
        <div className="grid grid-cols-2 gap-1.5">
          {EXPENSE_TYPES.map((t) => {
            const color = typeColorMap[t.id] || typeColorMap.other;
            const count = eb.filter(e => e.expenseType === t.id).length;
            const sum = eb.filter(e => e.expenseType === t.id).reduce((s, b) => s + (b.amount || 0), 0);
            return (
              <div key={t.id} className="p-2.5 rounded-[14px]" style={{ background: color.bg }}>
                <div className="text-[10.5px] font-semibold" style={{ color: color.c }}>{t.label}</div>
                <div className="mt-1 flex items-end justify-between">
                  <div className="text-[13px] font-bold" style={{ color: COLOR.gray4 }}>¥{fmtMoney(sum)}</div>
                  <div className="text-[10px]" style={{ color: COLOR.gray3 }}>{count}笔</div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {eb.map((b) => {
        const type = EXPENSE_TYPES.find((t) => t.id === b.expenseType);
        const color = typeColorMap[b.expenseType] || typeColorMap.other;
        return (
          <div key={b.expenseId} className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
            <div className="flex items-start justify-between">
              <div className="flex items-start gap-2.5 flex-1 min-w-0">
                <div className="w-8 h-8 rounded-[12px] flex items-center justify-center text-sm flex-shrink-0" style={{ background: color.bg }}>📋</div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-1.5 flex-wrap">
                    <h3 className="text-[13px] font-bold truncate" style={{ color: COLOR.gray4 }}>{b.expenseName}</h3>
                    <span className="text-[9.5px] px-1.5 py-0.5 rounded font-medium" style={{ background: color.bg, color: color.c }}>{type?.label}</span>
                  </div>
                  <div className="text-[11px] mt-0.5" style={{ color: COLOR.gray3 }}>房源 {b.houseSourceId}</div>
                  {b.remark && <div className="text-[10.5px] mt-0.5" style={{ color: COLOR.gray3 }}>{b.remark}</div>}
                </div>
              </div>
              <div className="text-right flex-shrink-0 ml-2">
                <div className="text-[15px] font-bold" style={{ color: COLOR.danger }}>-¥{fmtMoney(b.amount)}</div>
                <div className="text-[10px] mt-0.5" style={{ color: COLOR.gray3 }}>发生于 {b.costDate}</div>
              </div>
            </div>

            {/* 操作 */}
            <div className="mt-3 pt-3 flex items-center gap-1.5 flex-wrap" style={{ borderTop: '1px solid #F1F5F9' }}>
              <button onClick={() => nav('/expense-form')} className="text-[11px] px-3 py-1.5 rounded-[10px] font-medium active:scale-95 transition" style={{ background: COLOR.brandSoft, color: COLOR.brand }}>编辑</button>
            </div>
          </div>
        );
      })}
      {eb.length === 0 && !loading && (
        <div className="text-center py-10" style={{ color: COLOR.gray3 }}>
          暂无支出账单
        </div>
      )}
    </div>
  );
}
