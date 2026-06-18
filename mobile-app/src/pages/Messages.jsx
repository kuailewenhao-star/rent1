import { COLOR } from '../data/colors.js';
import { useState, useEffect } from 'react';
import { getNotifications, markAsRead, markAllAsRead } from '../api/notification.js';

const TYPE_MAP = {
  bill: { label: '账单提醒', color: COLOR.danger, bg: COLOR.dangerSoft, icon: '💰' },
  contract: { label: '合约提醒', color: COLOR.warn, bg: COLOR.warnSoft, icon: '📄' },
  system: { label: '系统公告', color: COLOR.brand, bg: COLOR.brandSoft, icon: '🔔' },
  // 后端返回的类型映射
  BILL_OVERDUE: { label: '账单逾期', color: COLOR.danger, bg: COLOR.dangerSoft, icon: '💰' },
  CONTRACT_EXPIRING: { label: '合约到期', color: COLOR.warn, bg: COLOR.warnSoft, icon: '📄' },
  SYSTEM: { label: '系统公告', color: COLOR.brand, bg: COLOR.brandSoft, icon: '🔔' },
  BILL_PAID: { label: '账单到账', color: COLOR.success, bg: COLOR.successSoft, icon: '💵' }
};

export default function Messages() {
  const [msgList, setMsgList] = useState([]);
  const [expandedId, setExpandedId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [unread, setUnread] = useState(0);

  // 加载消息列表
  const loadMessages = async () => {
    try {
      setLoading(true);
      const res = await getNotifications({ page: 1, pageSize: 50 });
      if (res.success && res.data) {
        setMsgList(res.data.list || []);
        setUnread(res.data.unreadCount || 0);
      }
    } catch (error) {
      console.error('加载消息失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMessages();
  }, []);

  const handleMarkRead = async (id) => {
    try {
      await markAsRead(id);
      setMsgList(prev => prev.map(m => 
        m.notificationId === id ? { ...m, status: 'READ' } : m
      ));
      setUnread(prev => Math.max(0, prev - 1));
    } catch (error) {
      console.error('标记已读失败:', error);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
      setMsgList(prev => prev.map(m => ({ ...m, status: 'READ' })));
      setUnread(0);
    } catch (error) {
      console.error('标记全部已读失败:', error);
    }
  };

  const toggleExpand = (id) => {
    if (expandedId === id) {
      setExpandedId(null);
    } else {
      setExpandedId(id);
      // 自动标记已读
      const msg = msgList.find(m => m.notificationId === id);
      if (msg && msg.status !== 'READ') {
        handleMarkRead(id);
      }
    }
  };

  return (
    <div className="min-h-screen pb-24" style={{ background: COLOR.gray1 }}>
      {loading && (
        <div className="flex items-center justify-center py-20">
          <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
        </div>
      )}
      <div className="px-5 pt-8 pb-4 flex items-center justify-between" style={{ background: 'white' }}>
        <div>
          <h1 className="text-[20px] font-bold" style={{ color: COLOR.gray4 }}>消息中心</h1>
          <p className="text-[12px] mt-1" style={{ color: COLOR.gray3 }}>账单 · 合约 · 系统公告</p>
        </div>
        {unread > 0 && (
          <button 
            onClick={handleMarkAllRead}
            className="text-[11px] px-2.5 py-1 rounded-full font-semibold"
            style={{ background: COLOR.brandSoft, color: COLOR.brand }}
          >
            全部已读
          </button>
        )}
      </div>

      <div className="px-5 pt-4 space-y-3">
        {msgList.length === 0 && !loading && (
          <div className="text-center py-20" style={{ color: COLOR.gray3 }}>
            暂无消息
          </div>
        )}
        {msgList.map((m) => {
          const tp = TYPE_MAP[m.type] || TYPE_MAP.system;
          const isExpanded = expandedId === m.notificationId;
          const isUnread = m.status !== 'READ';
          return (
            <div
              key={m.notificationId}
              className="bg-white rounded-[20px] p-4 shadow-[0_2px_8px_rgba(15,23,42,0.04)] tap-feedback"
              onClick={() => toggleExpand(m.notificationId)}
              style={{ cursor: 'pointer' }}
            >
              <div className="flex items-start gap-3">
                <div className="w-9 h-9 rounded-[12px] flex items-center justify-center text-base flex-shrink-0" style={{ background: tp.bg }}>{tp.icon}</div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <h3 className="text-[13.5px] font-bold truncate" style={{ color: COLOR.gray4 }}>{m.title}</h3>
                    {isUnread && <span className="w-2 h-2 rounded-full bg-red-500 flex-shrink-0" />}
                  </div>
                  <div className="text-[11px] mt-1 leading-relaxed" style={{ color: COLOR.gray3 }}>{m.content}</div>
                  <div className="text-[10px] mt-2" style={{ color: COLOR.gray3 }}>{m.createTime ? new Date(m.createTime).toLocaleString('zh-CN') : ''}</div>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
