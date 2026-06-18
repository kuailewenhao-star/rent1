/**
 * 租客账单列表页 - 控制器
 * 后端接口：GET /income-invoices（DataScopeContext 自动按 memberId 过滤为本人账单）
 * 权限控制：仅 TENANT 角色可见
 */

const app = getApp();

Page({
  data: {
    statusFilters: [
      { id: 'all', name: '全部', count: 0 },
      { id: 'pending', name: '待支付', count: 0 },
      { id: 'paid', name: '已支付', count: 0 },
      { id: 'overdue', name: '逾期', count: 0 }
    ],
    currentStatus: 'all',

    summary: {
      pendingAmount: '0.00',
      overdueAmount: '0.00'
    },

    billList: [],
    loading: false
  },

  onLoad(options) {
    if (app.globalData.currentRole !== 'tenant') {
      wx.showToast({ title: '无访问权限', icon: 'none' });
      setTimeout(() => { wx.redirectTo({ url: '/pages/index/index' }); }, 1200);
      return;
    }

    if (options.status) {
      this.setData({ currentStatus: options.status });
    }

    this.loadBillList();
  },

  onShow() {
    if (app.globalData.currentRole === 'tenant') {
      this.loadBillList();
    }
  },

  loadBillList() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    const page = this;
    const statusParam = this._buildStatusParam();
    const query = { page: 1, pageSize: 50 };
    if (statusParam) query.status = statusParam;

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
    if (s === 'pending') return 'PENDING';
    if (s === 'paid') return 'PAID';
    if (s === 'overdue') return 'OVERDUE';
    return null;
  },

  _applyBillData(invoices) {
    const normalized = invoices.map(inv => this._toBillItem(inv));

    const counts = { all: normalized.length, pending: 0, paid: 0, overdue: 0 };
    let pendingTotal = 0;
    let overdueTotal = 0;

    normalized.forEach(b => {
      const st = b.status;
      if (st in counts) counts[st]++;
      const amt = this._parseAmount(b.rawAmount);
      if (st === 'pending') pendingTotal += amt;
      if (st === 'overdue') overdueTotal += amt;
    });

    const statusFilters = this.data.statusFilters.map(f => ({
      ...f, count: counts[f.id] || 0
    }));

    // 仅当前筛选的列表
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
      rawAmount: inv.amount,
      periodText: inv.cycleDescription ||
        (inv.cycleStart ? `${inv.cycleStart}${inv.cycleEnd && inv.cycleEnd !== inv.cycleStart ? ' 至 ' + inv.cycleEnd : ''}` : ''),
      periodStart: inv.cycleStart,
      periodEnd: inv.cycleEnd,
      dueDate: inv.dueDate,
      status: rawStatus,
      statusName: statusNameMap[rawStatus] || '待支付',
      statusClass: rawStatus,
      isManual: !!inv.isManual,
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

  onStatusChange(e) {
    const status = e.currentTarget.dataset.id;
    if (status === this.data.currentStatus) return;
    this.setData({ currentStatus: status });
    this.loadBillList();
  },

  goToDetail(e) {
    const billId = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/bill/income/detail/detail?id=${billId}` });
  },

  onPayBill(e) {
    const billId = e.currentTarget.dataset.id;
    const bill = this.data.billList.find(i => i.id === billId);
    if (!bill) return;

    wx.showModal({
      title: '确认缴费',
      content: `账单金额 ¥${bill.amount}，确认已支付？`,
      confirmText: '确认',
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
            wx.showToast({ title: '已申请核销', icon: 'success' });
            this.loadBillList();
          },
          fail: () => {
            wx.hideLoading();
            wx.showToast({ title: '操作失败，请联系房东', icon: 'none' });
          }
        });
      }
    });
  }
});
