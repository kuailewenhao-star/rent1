import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { COLOR } from '../data/colors.js';
import { getDashboard } from '../api/dashboard.js';
import { getProfile } from '../api/auth.js';
import { getNotifications, getUnreadCount } from '../api/notification.js';
import { fmtMoney } from '../data/mock.js';

const PERIOD_TABS = [
  { id: 'THIS_MONTH', label: '本月' },
  { id: 'THIS_QUARTER', label: '本季' },
  { id: 'THIS_YEAR', label: '今年' },
  { id: 'ALL', label: '全部' }
];

const PERIOD_MAP = {
  'month': 'THIS_MONTH',
  'quarter': 'THIS_QUARTER',
  'year': 'THIS_YEAR',
  'all': 'ALL'
};

const getGreeting = () => {
  const h = new Date().getHours();
  if (h < 6) return '凌晨好';
  if (h < 12) return '早上好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
};

export default function Home() {
  const nav = useNavigate();
  const [period, setPeriod] = useState('THIS_MONTH');
  const [loading, setLoading] = useState(true);
  const [profile, setProfile] = useState({ maskName: '' });
  const [dashboard, setDashboard] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  // 加载看板数据
  const loadDashboard = useCallback(async () => {
    try {
      setLoading(true);
      const [dashboardRes, profileRes, notifRes, countRes] = await Promise.all([
        getDashboard(period),
        getProfile(),
        getNotifications({ page: 1, pageSize: 10 }),
        getUnreadCount()
      ]);
      
      if (dashboardRes.success && dashboardRes.data) {
        setDashboard(dashboardRes.data);
      }
      if (profileRes.success && profileRes.data) {
        setProfile({
          maskName: profileRes.data.realName ? profileRes.data.realName.charAt(0) + '**' : '用户'
        });
      }
      if (notifRes.success && notifRes.data) {
        setNotifications(notifRes.data.list || []);
      }
      if (countRes.success && countRes.data) {
        setUnreadCount(countRes.data.unreadCount || 0);
      }
    } catch (error) {
      console.error('加载首页数据失败:', error);
    } finally {
      setLoading(false);
    }
  }, [period]);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const today = new Date();
  const dateStr = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`;
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][today.getDay()];

  // 从dashboard数据中提取统计数据
  const roomStats = dashboard?.rooms ? {
    total: dashboard.rooms.total || 0,
    rented: dashboard.rooms.occupied || 0,
    vacant: dashboard.rooms.vacant || 0,
    expiring: dashboard.rooms.expiringSoon || 0
  } : { total: 0, rented: 0, vacant: 0, expiring: 0 };

  const billSummary = dashboard ? {
    pendingCount: dashboard.pendingBills?.count || 0,
    pendingAmount: dashboard.pendingBills?.amount || 0,
    overdueCount: dashboard.overdueBills?.count || 0,
    overdueAmount: dashboard.overdueBills?.amount || 0,
    depositTotal: dashboard.effectiveDepositAmount || 0
  } : { pendingCount: 0, pendingAmount: 0, overdueCount: 0, overdueAmount: 0, depositTotal: 0 };

  const profit = dashboard?.profit ? {
    totalIncome: dashboard.profit.income || 0,
    totalExpense: dashboard.profit.expense || 0,
    netProfit: dashboard.profit.profit || 0,
    rate: dashboard.profit.income > 0 
      ? ((dashboard.profit.profit / dashboard.profit.income) * 100).toFixed(1) 
      : '0.0'
  } : { totalIncome: 0, totalExpense: 0, netProfit: 0, rate: '0.0' };

  // 生成最新提醒列表
  const reminders = [];
  
  // 从dashboard数据中添加逾期和到期提醒
  if (dashboard?.overdueBills?.count > 0) {
    reminders.push({
      id: 'rem-overdue',
      type: 'overdue',
      title: `有 ${dashboard.overdueBills.count} 笔账单逾期未付，需催收`,
      date: '今天'
    });
  }
  if (dashboard?.rooms?.expiringSoon > 0) {
    reminders.push({
      id: 'rem-expiring',
      type: 'expiring',
      title: `有 ${dashboard.rooms.expiringSoon} 个合约即将到期`,
      date: '今天'
    });
  }
  
  // 从notifications列表中添加消息提醒
  if (notifications && notifications.length > 0) {
    notifications.slice(0, 3).forEach((notif) => {
      const notifType = notif.type === 'BILL_OVERDUE' ? 'overdue' 
        : notif.type === 'CONTRACT_EXPIRING' ? 'expiring' 
        : 'paid';
      reminders.push({
        id: notif.notificationId || `notif-${Math.random()}`,
        type: notifType,
        title: notif.title,
        date: notif.createTime ? new Date(notif.createTime).toLocaleDateString('zh-CN') : ''
      });
    });
  }
  
  // 如果没有任何提醒，显示默认提醒
  if (reminders.length === 0) {
    reminders.push({
      id: 'rem-empty',
      type: 'paid',
      title: '暂无待处理事项',
      date: ''
    });
  }

  // 处理时间范围切换
  const handlePeriodChange = (newPeriod) => {
    // 将UI的period id映射到API的timeRange
    const timeRangeMap = {
      'month': 'THIS_MONTH',
      'quarter': 'THIS_QUARTER',
      'year': 'THIS_YEAR',
      'all': 'ALL'
    };
    setPeriod(timeRangeMap[newPeriod] || 'THIS_MONTH');
  };

  // 获取UI显示用的period id（用于高亮）
  const getPeriodId = () => {
    const reverseMap = {
      'THIS_MONTH': 'month',
      'THIS_QUARTER': 'quarter',
      'THIS_YEAR': 'year',
      'ALL': 'all'
    };
    return reverseMap[period] || 'month';
  };

  return (
    <div className="min-h-screen pb-24" style={{ background: COLOR.gray1 }}>
      {loading && (
        <div className="absolute inset-0 bg-white/50 flex items-center justify-center z-50">
          <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
        </div>
      )}
      {/* 顶部问候区域（恢复上版本简洁设计） */}
      <div className="px-5 pt-8 pb-20" style={{ background: `linear-gradient(180deg, ${COLOR.brand} 0%, #1D4ED8 100%)` }}>
        <div className="flex items-start justify-between">
          <div className="flex-1 min-w-0">
            <div className="text-white text-[18px] font-bold leading-tight">{getGreeting()}，{profile.maskName}</div>
            <div className="text-white/75 text-[12px] mt-1.5">{dateStr} · {weekDay}</div>
          </div>
          <button onClick={() => nav('/messages')} className="relative w-10 h-10 rounded-full bg-white/15 backdrop-blur flex items-center justify-center active:scale-95 transition flex-shrink-0">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
              <path d="M13.73 21a2 2 0 0 1-3.46 0" />
            </svg>
            {unreadCount > 0 && (
              <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] font-semibold rounded-full min-w-[18px] h-[18px] px-1 flex items-center justify-center">{unreadCount}</span>
            )}
          </button>
        </div>

        {/* 净盈利卡（嵌在蓝色区域） */}
        <div className="mt-6">
          <div className="text-white/75 text-[12px]">经营净盈利 · {PERIOD_TABS.find(p => p.id === period)?.label || '本月'}</div>
          <div className="mt-1.5">
            <span className="text-white text-[32px] font-bold leading-none tracking-tight">¥{fmtMoney(profit.netProfit)}</span>
          </div>
          <div className="mt-2.5 flex gap-2 flex-wrap">
            {PERIOD_TABS.map(p => (
              <button
                key={p.id}
                onClick={() => handlePeriodChange(p.id)}
                className={`text-[11.5px] px-3 py-1 rounded-full transition ${getPeriodId() === p.id ? 'bg-white text-blue-700 font-semibold' : 'bg-white/15 text-white'}`}
              >{p.label}</button>
            ))}
          </div>
        </div>
      </div>

      {/* 收入/支出/利润率副卡（白色卡片覆盖蓝色底部） */}
      <div className="px-5 -mt-14">
        <div className="bg-white rounded-[20px] p-4 shadow-[0_6px_20px_rgba(15,23,42,0.06)]">
          <div className="grid grid-cols-3 gap-2">
            <div className="text-center py-1">
              <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>总收入</div>
              <div className="mt-1 text-[15px] font-bold" style={{ color: COLOR.success }}>¥{fmtMoney(profit.totalIncome)}</div>
            </div>
            <div className="text-center py-1 border-l border-r" style={{ borderColor: '#E2E8F0' }}>
              <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>总支出</div>
              <div className="mt-1 text-[15px] font-bold" style={{ color: COLOR.danger }}>¥{fmtMoney(profit.totalExpense)}</div>
            </div>
            <div className="text-center py-1">
              <div className="text-[10.5px]" style={{ color: COLOR.gray3 }}>盈利率</div>
              <div className="mt-1 text-[15px] font-bold" style={{ color: COLOR.brand }}>{profit.rate}%</div>
            </div>
          </div>
        </div>
      </div>

      {/* 房间概况 */}
      <div className="px-5 mt-4">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-[15px] font-semibold" style={{ color: COLOR.gray4 }}>房间概况</h2>
          <button onClick={() => nav('/archive')} className="text-[12px]" style={{ color: COLOR.brand }}>查看全部 →</button>
        </div>
        <div className="grid grid-cols-4 gap-2.5">
          {[
            { label: '房间总数', val: roomStats.total, color: COLOR.brand, bg: COLOR.brandSoft, icon: '🏠' },
            { label: '已出租', val: roomStats.rented, color: COLOR.success, bg: COLOR.successSoft, icon: '🔑' },
            { label: '空置中', val: roomStats.vacant, color: COLOR.gray3, bg: '#F1F5F9', icon: '🏚️' },
            { label: '即将到期', val: roomStats.expiring, color: COLOR.warn, bg: COLOR.warnSoft, icon: '⏰' }
          ].map((it) => (
            <div key={it.label} className="bg-white rounded-[18px] p-3 text-center shadow-[0_2px_8px_rgba(15,23,42,0.04)]">
              <div className="w-8 h-8 mx-auto rounded-full flex items-center justify-center text-base" style={{ background: it.bg }}>{it.icon}</div>
              <div className="mt-1.5 text-[17px] font-bold" style={{ color: it.color }}>{it.val}</div>
              <div className="text-[10px] mt-0.5" style={{ color: COLOR.gray3 }}>{it.label}</div>
            </div>
          ))}
        </div>
      </div>

      {/* 账单数据 */}
      <div className="px-5 mt-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-[15px] font-semibold" style={{ color: COLOR.gray4 }}>账单数据</h2>
          <button onClick={() => nav('/bill')} className="text-[12px]" style={{ color: COLOR.brand }}>账单管理 →</button>
        </div>
        <div className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)] space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-[12px] flex items-center justify-center" style={{ background: COLOR.warnSoft }}>
                <span className="text-[14px]">⏳</span>
              </div>
              <div>
                <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>待支付账单</div>
                <div className="text-[11px] mt-0.5" style={{ color: COLOR.gray3 }}>共 {billSummary.pendingCount} 笔</div>
              </div>
            </div>
            <div className="text-right">
              <div className="text-[14px] font-bold" style={{ color: COLOR.warn }}>¥{fmtMoney(billSummary.pendingAmount)}</div>
            </div>
          </div>
          <div className="h-px" style={{ background: '#F1F5F9' }} />
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-[12px] flex items-center justify-center" style={{ background: COLOR.dangerSoft }}>
                <span className="text-[14px]">⚠️</span>
              </div>
              <div>
                <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>逾期未付</div>
                <div className="text-[11px] mt-0.5" style={{ color: COLOR.gray3 }}>共 {billSummary.overdueCount} 笔，需催收</div>
              </div>
            </div>
            <div className="text-right">
              <div className="text-[14px] font-bold" style={{ color: COLOR.danger }}>¥{fmtMoney(billSummary.overdueAmount)}</div>
            </div>
          </div>
          <div className="h-px" style={{ background: '#F1F5F9' }} />
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-[12px] flex items-center justify-center" style={{ background: COLOR.indigoSoft }}>
                <span className="text-[14px]">💰</span>
              </div>
              <div>
                <div className="text-[13px] font-semibold" style={{ color: COLOR.gray4 }}>已收押金</div>
                <div className="text-[11px] mt-0.5" style={{ color: COLOR.gray3 }}>累计押金收入</div>
              </div>
            </div>
            <div className="text-right">
              <div className="text-[14px] font-bold" style={{ color: COLOR.indigo }}>¥{fmtMoney(billSummary.depositTotal)}</div>
            </div>
          </div>
        </div>
      </div>

      {/* 最新提醒 */}
      <div className="px-5 mt-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-[15px] font-semibold" style={{ color: COLOR.gray4 }}>最新提醒</h2>
          <button onClick={() => nav('/messages')} className="text-[12px]" style={{ color: COLOR.brand }}>消息中心 →</button>
        </div>
        <div className="bg-white rounded-[20px] shadow-[0_2px_8px_rgba(15,23,42,0.04)] overflow-hidden">
          {reminders.map((rem, i) => {
            const colorMap = { overdue: { c: COLOR.danger, bg: COLOR.dangerSoft }, expiring: { c: COLOR.warn, bg: COLOR.warnSoft }, paid: { c: COLOR.success, bg: COLOR.successSoft } };
            const m = colorMap[rem.type] || colorMap.paid;
            const label = rem.type === 'overdue' ? '账单逾期' : rem.type === 'expiring' ? '合约到期' : '账单已付';
            return (
              <div key={rem.id} className="flex items-start gap-3 p-4" style={i < reminders.length - 1 ? { borderBottom: '1px solid #F1F5F9' } : {}}>
                <div className="mt-0.5 w-9 h-9 rounded-[12px] flex items-center justify-center" style={{ background: m.bg }}>
                  <span className="text-[12px] font-bold" style={{ color: m.c }}>!</span>
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] px-1.5 py-0.5 rounded font-medium" style={{ color: m.c, background: m.bg }}>{label}</span>
                    <span className="text-[10.5px]" style={{ color: COLOR.gray3 }}>{rem.date}</span>
                  </div>
                  <div className="text-[12.5px] mt-1.5 leading-relaxed" style={{ color: COLOR.gray4 }}>{rem.title}</div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* 注意：已删除底部重复图标行（与 BottomNav 冲突） */}
    </div>
  );
}
