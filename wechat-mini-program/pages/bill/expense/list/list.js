/**
 * 支出账单列表页面 - 控制器
 * 后端接口：GET /expense-invoices
 * 请求参数：houseSourceId, costType, startDate, endDate, page, pageSize
 * 响应 ExpenseInvoiceResponse 字段：
 *   { expenseId, houseSourceName, costType, costTypeName,
 *     amount, costDate, remark, createTime, updateTime }
 * 权限约束：租客完全不可见，仅房东可视可操作
 */

const app = getApp();

Page({
  data: {
    summary: { totalExpense: '0.00', monthExpense: '0.00' },

    timeFilters: [
      { id: 'month', name: '本月' },
      { id: 'quarter', name: '本季度' },
      { id: 'year', name: '本年度' },
      { id: 'all', name: '全部时间' }
    ],
    currentTimeFilter: 'all',
    currentPeriodText: '全部时间',

    expenseList: [],

    loading: false
  },

  onLoad() {
    if (app.globalData.currentRole !== 'landlord') {
      wx.showToast({ title: '无访问权限', icon: 'none' });
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/tenant/index/index' });
      }, 1200);
      return;
    }
    this.loadExpenseList();
  },

  onShow() {
    if (app.globalData.currentRole === 'landlord') {
      this.loadExpenseList();
    }
  },

  loadExpenseList() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    const page = this;
    const { startDate, endDate } = this._buildDateRange();
    const query = { page: 1, pageSize: 50 };
    if (startDate) query.startDate = startDate;
    if (endDate) query.endDate = endDate;

    app.request({
      url: '/expense-invoices',
      method: 'GET',
      data: query,
      showLoading: false,
      success: (data) => {
        page._applyExpenseData(data || []);
      },
      fail: () => {
        page._applyExpenseData([]);
      }
    });
  },

  _buildDateRange() {
    const tf = this.data.currentTimeFilter;
    if (tf === 'all' || !tf) return {};
    const now = new Date();
    const y = now.getFullYear();
    const m = now.getMonth() + 1;
    const pad = (n) => String(n).padStart(2, '0');
    let start;
    let end = `${y}-${pad(m)}-${pad(now.getDate())}`;

    if (tf === 'month') {
      start = `${y}-${pad(m)}-01`;
    } else if (tf === 'quarter') {
      const qMonth = Math.floor((m - 1) / 3) * 3 + 1;
      start = `${y}-${pad(qMonth)}-01`;
    } else if (tf === 'year') {
      start = `${y}-01-01`;
    }
    return { startDate: start, endDate: end };
  },

  _applyExpenseData(invoices) {
    const list = invoices.map(inv => this._toExpenseItem(inv));

    // 汇总总金额和本月金额
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const monthPrefix = `${y}-${m}`;

    let total = 0;
    let monthTotal = 0;
    list.forEach(item => {
      const amt = this._parseAmount(item.rawAmount);
      total += amt;
      if (item.costDate && String(item.costDate).startsWith(monthPrefix)) {
        monthTotal += amt;
      }
    });

    this.setData({
      loading: false,
      expenseList: list,
      summary: {
        totalExpense: this._formatAmount(total),
        monthExpense: this._formatAmount(monthTotal)
      }
    });
  },

  _toExpenseItem(inv) {
    const costType = inv.costType || '';
    const iconMap = {
      RENT: '🏠', DEPOSIT: '💰', WATER: '💧',
      ELECTRIC: '⚡', GAS: '🔥', BROADBAND: '📶',
      PROPERTY: '🏢', TRASH: '🗑️', REPAIR: '🔧',
      OTHER: '📋'
    };
    const typeClass = costType ? costType.toLowerCase().replace(/_/g, '-') : 'other';

    return {
      id: inv.expenseId || '',
      type: costType.toLowerCase(),
      typeName: inv.costTypeName || costType || '其他支出',
      typeIcon: iconMap[costType] || '📋',
      typeClass: `type-${typeClass}`,
      amount: this._formatAmount(inv.amount),
      rawAmount: inv.amount,
      propertyName: inv.houseSourceName || '',
      costDate: inv.costDate || '',
      remark: inv.remark || '',
      createTime: inv.createTime ? String(inv.createTime).slice(0, 10) : ''
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

  onSelectTime(e) {
    const filterId = e.currentTarget.dataset.id;
    const periodMap = {
      month: '本月', quarter: '本季度', year: '本年度', all: '全部时间'
    };
    this.setData({
      currentTimeFilter: filterId,
      currentPeriodText: periodMap[filterId] || '全部时间'
    });
    this.loadExpenseList();
  },

  goToDetail(e) {
    const expenseId = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/bill/expense/detail/detail?id=${expenseId}` });
  },

  goToAddExpense() {
    wx.navigateTo({ url: '/pages/bill/expense/add/add' });
  }
});
