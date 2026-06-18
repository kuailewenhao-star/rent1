/**
 * 收入账单列表页面 - 控制器
 * 后端接口：
 *   GET /income-invoices - 收入账单列表（参数：status, feeType, billMonth, page, pageSize）
 *   PUT /income-invoices/{invoiceId}/pay - 账单核销
 * 后端响应 IncomeInvoiceResponse 字段：
 *   { invoiceId, contractId, feeType, feeTypeName, amount,
 *     cycleStart, cycleEnd, cycleDescription, dueDate, paidTime,
 *     status, isManual, remark, createTime, tenantName, roomName, houseSourceName,
 *     isOverdue, overdueDays }
 */

const app = getApp();

// 后端状态枚举 → 前端筛选值映射
const STATUS_MAP = {
  PENDING: 'pending',
  PAID: 'paid',
  OVERDUE: 'overdue',
  VOID: 'void'
};

Page({
  data: {
    isLandlord: true,

    statusFilters: [
      { id: 'all', name: '全部', count: 0 },
      { id: 'pending', name: '待支付', count: 0 },
      { id: 'paid', name: '已支付', count: 0 },
      { id: 'overdue', name: '逾期', count: 0 }
    ],
    currentStatus: 'all',

    timeFilters: [
      { id: 'month', name: '本月', billMonth: null },
      { id: 'quarter', name: '本季度', billMonth: null },
      { id: 'year', name: '本年度', billMonth: null },
      { id: 'all', name: '全部时间', billMonth: null }
    ],
    currentTimeFilter: 'all',
    currentPeriodText: '全部时间',

    summary: {
      pendingAmount: '0.00',
      overdueAmount: '0.00'
    },

    billList: [],

    loading: false
  },

  onLoad(options) {
    const currentRole = app.globalData.currentRole;
    this.setData({ isLandlord: currentRole === 'landlord' });

    if (options.status) {
      this.setData({ currentStatus: options.status });
    }

    this.loadBillList();
  },

  onShow() {
    this.loadBillList();
  },

  loadBillList() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    const page = this;
    const statusParam = this._buildStatusParam();
    const monthParam = this._buildMonthParam();

    const query = { page: 1, pageSize: 50 };
    if (statusParam) query.status = statusParam;
    if (monthParam) query.billMonth = monthParam;

    app.request({
      url: '/income-invoices',
      method: 'GET',
      data: query,
      showLoading: false,
      success: (data) => {
        page._applyBillData(data || []);
      },
      fail: () => {
        page._applyBillData([]);
      }
    });
  },

  _buildStatusParam() {
    const s = this.data.currentStatus;
    if (s === 'all' || !s) return null;
    // 前端筛选值 → 后端枚举值
    if (s === 'pending') return 'PENDING';
    if (s === 'paid') return 'PAID';
    if (s === 'overdue') return 'OVERDUE';
    return null;
  },

  _buildMonthParam() {
    const tf = this.data.currentTimeFilter;
    if (tf === 'all' || !tf) return null;
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    if (tf === 'month') return `${year}-${month}`;
    if (tf === 'year') return `${year}`;
    return null;
  },

  _applyBillData(invoices) {
    const normalized = invoices.map(inv => this._toBillItem(inv));

    const counts = { all: normalized.length, pending: 0, paid: 0, overdue: 0 };
    let pendingTotal = 0;
    let overdueTotal = 0;

    normalized.forEach(b => {
      const s = b.status;
      if (s in counts) counts[s]++;
      const amt = this._parseAmount(b.amount);
      if (s === 'pending') pendingTotal += amt;
      if (s === 'overdue') overdueTotal += amt;
    });

    const statusFilters = this.data.statusFilters.map(f => ({
      ...f, count: counts[f.id] || 0
    }));

    // 根据当前筛选值过滤列表
    const filter = this.data.currentStatus;
    const filteredList = filter === 'all'
      ? normalized
      : normalized.filter(b => b.status === filter);

    this.setData({
      loading: false,
      statusFilters,
      billList: filteredList,
      summary: {
        pendingAmount: this._formatAmount(pendingTotal),
        overdueAmount: this._formatAmount(overdueTotal)
      }
    });
  },

  _toBillItem(inv) {
    const statusLower = (inv.status || '').toLowerCase();
    const rawStatus = statusLower === 'overdue' ? 'overdue'
      : statusLower === 'paid' ? 'paid'
      : statusLower === 'pending' ? 'pending'
      : 'pending';

    const feeType = inv.feeType || '';
    const iconMap = {
      RENT: '🏠', DEPOSIT: '💰', WATER: '💧',
      ELECTRIC: '⚡', GAS: '🔥', BROADBAND: '📶',
      PROPERTY: '🏢', TRASH: '🗑️', OTHER: '📋'
    };
    const typeClass = feeType ? feeType.toLowerCase().replace(/_/g, '-') : 'other';

    const cycleStart = inv.cycleStart || '';
    const cycleEnd = inv.cycleEnd || '';
    let periodText = inv.cycleDescription || '';
    if (!periodText) {
      if (cycleStart && cycleEnd && cycleStart !== cycleEnd) {
        periodText = `${cycleStart} 至 ${cycleEnd}`;
      } else if (cycleStart) {
        periodText = cycleStart;
      }
    }

    const statusNameMap = {
      pending: '待支付', paid: '已支付',
      overdue: '逾期未付', void: '已作废'
    };

    return {
      id: inv.invoiceId || '',
      type: feeType.toLowerCase(),
      typeName: inv.feeTypeName || feeType || '其他',
      typeIcon: iconMap[feeType] || '📋',
      typeClass: `type-${typeClass}`,
      amount: this._formatAmount(inv.amount),
      roomName: inv.roomName || '',
      propertyName: inv.houseSourceName || '',
      tenantName: inv.tenantName || '',
      periodText: periodText,
      periodStart: cycleStart,
      periodEnd: cycleEnd,
      status: rawStatus,
      statusName: statusNameMap[rawStatus] || '待支付',
      statusClass: rawStatus,
      createTime: inv.createTime ? String(inv.createTime).slice(0, 10) : '',
      isManual: !!inv.isManual,
      remark: inv.remark || ''
    };
  },

  _parseAmount(val) {
    if (val === null || val === undefined || val === '') return 0;
    const num = typeof val === 'number' ? val : parseFloat(String(val));
    return isNaN(num) ? 0 : num;
  },

  _formatAmount(val) {
    const num = this._parseAmount(val);
    const sign = num < 0 ? '-' : '';
    const abs = Math.abs(num);
    const fixed = abs.toFixed(2);
    const parts = fixed.split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return sign + parts.join('.');
  },

  onStatusChange(e) {
    const status = e.currentTarget.dataset.id;
    if (status === this.data.currentStatus) return;
    this.setData({ currentStatus: status });
    this.loadBillList();
  },

  onTimeFilterChange(e) {
    const filterId = e.currentTarget.dataset.id;
    const periodMap = {
      month: '本月', quarter: '本季度', year: '本年度', all: '全部时间'
    };
    this.setData({
      currentTimeFilter: filterId,
      currentPeriodText: periodMap[filterId] || '全部时间'
    });
    this.loadBillList();
  },

  goToDetail(e) {
    const billId = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/bill/income/detail/detail?id=${billId}` });
  },

  goToAddBill() {
    wx.navigateTo({ url: '/pages/bill/income/add/add' });
  },

  onPayBill(e) {
    const billId = e.currentTarget.dataset.id;
    if (!billId) return;
    wx.showModal({
      title: '确认核销',
      content: '确认该账单已收到款项？',
      success: (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '处理中...', mask: true });
        app.request({
          url: `/income-invoices/${billId}/pay`,
          method: 'PUT',
          data: {},
          showLoading: false,
          success: () => {
            wx.hideLoading();
            wx.showToast({ title: '核销成功', icon: 'success' });
            this.loadBillList();
          },
          fail: (err) => {
            wx.hideLoading();
            wx.showToast({
              title: (err && err.message) || '核销失败',
              icon: 'none'
            });
          }
        });
      }
    });
  }
});
