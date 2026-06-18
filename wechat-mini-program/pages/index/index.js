/**
 * 房东首页 - 控制器
 * 后端接口：GET /landlord/dashboard
 * 请求参数：timeRange (TODAY / THIS_MONTH / THIS_QUARTER / THIS_YEAR / CUSTOM)
 *           startDate / endDate (timeRange=CUSTOM 时必填)
 * 响应体：{
 *   pendingBills: { count, amount },
 *   overdueBills: { count, amount },
 *   effectiveDepositAmount: BigDecimal,
 *   rooms: { total, vacant, occupied, expiringSoon },
 *   profit: { income, expense, profit },
 *   timeRange: String
 * }
 */

const app = getApp();

Page({
  data: {
    // 是否显示角色切换提示
    showRoleTip: true,
    // 当前角色
    currentRole: 'landlord',

    // 时间筛选器选项（与后端枚举对齐）
    timeFilters: [
      { id: 'today', name: '今日', backendValue: 'TODAY' },
      { id: 'month', name: '本月', backendValue: 'THIS_MONTH' },
      { id: 'quarter', name: '季度', backendValue: 'THIS_QUARTER' },
      { id: 'year', name: '年度', backendValue: 'THIS_YEAR' }
    ],
    // 当前选中的时间筛选
    currentTimeFilter: 'month',
    // 当前时间周期文字
    currentPeriodText: '本月',

    // 账单统计数据
    stats: {
      pendingCount: 0,
      pendingAmount: '0.00',
      overdueCount: 0,
      overdueAmount: '0.00',
      currentDeposit: '0.00'
    },

    // 房间统计数据
    roomStats: {
      totalCount: 0,
      rentedCount: 0,
      vacantCount: 0,
      expiringCount: 0
    },

    // 盈利统计数据
    profitStats: {
      totalIncome: '0.00',
      totalExpense: '0.00',
      profit: '0.00'
    },

    // 未读消息数量
    unreadCount: 0,

    // 自定义时间范围
    customDateRange: null,

    // 数据加载状态
    loading: false
  },

  // 页面加载
  onLoad(options) {
    this.setData({
      currentRole: app.globalData.currentRole,
      showRoleTip: app.globalData.userInfo && app.globalData.userInfo.hasMultipleRoles
    });

    if (this.data.currentRole === 'tenant') {
      wx.redirectTo({ url: '/pages/tenant/index/index' });
      return;
    }

    // 加载首页数据 + 未读消息数
    this.loadDashboardData();
    this.loadUnreadCount();
  },

  // 页面显示
  onShow() {
    if (app.globalData.currentRole === 'landlord') {
      this.loadDashboardData();
      this.loadUnreadCount();
    }
  },

  // 加载看板数据（真实API）
  loadDashboardData() {
    if (this.data.loading) return;

    const timeFilter = this.data.currentTimeFilter;
    const periodMap = {
      'today': '今日',
      'month': '本月',
      'quarter': '本季度',
      'year': '本年度',
      'custom': '自定义周期'
    };

    // 查找后端枚举值
    const filterItem = this.data.timeFilters.find(f => f.id === timeFilter);
    const backendTimeRange = filterItem ? filterItem.backendValue : 'THIS_MONTH';

    // 自定义时间范围处理
    const isCustom = timeFilter === 'custom';
    let startDate = null;
    let endDate = null;
    if (isCustom && this.data.customDateRange) {
      startDate = this.data.customDateRange.startDate;
      endDate = this.data.customDateRange.endDate;
    }

    this.setData({
      loading: true,
      currentPeriodText: periodMap[timeFilter] || '本月'
    });

    const page = this;
    app.request({
      url: '/landlord/dashboard',
      method: 'GET',
      data: this._buildQueryData(backendTimeRange, startDate, endDate),
      showLoading: false,
      success: (data) => {
        console.log('看板数据:', data);
        page._applyDashboardData(data);
      },
      fail: (err) => {
        console.error('获取看板数据失败:', err);
        // 失败时保留 mock 数据展示（保证界面可看）
        page._applyMockData();
      }
    });
  },

  // 加载未读消息数量
  loadUnreadCount() {
    app.request({
      url: '/v1/notifications/count',
      method: 'GET',
      showLoading: false,
      success: (data) => {
        this.setData({
          unreadCount: data && data.unreadCount != null ? data.unreadCount : 0
        });
      },
      fail: () => {
        // 失败时不阻塞，保持默认 0
      }
    });
  },

  // 构建查询参数（GET 请求，所有参数放入 data）
  _buildQueryData(backendTimeRange, startDate, endDate) {
    const query = { timeRange: backendTimeRange };
    if (backendTimeRange === 'CUSTOM' && startDate && endDate) {
      query.startDate = startDate;
      query.endDate = endDate;
    }
    return query;
  },

  // 将后端响应映射到页面 data
  _applyDashboardData(data) {
    if (!data) {
      this._applyMockData();
      return;
    }

    // 解析各字段（后端字段可能为数字或 BigDecimal 字符串）
    const pendingBills = data.pendingBills || {};
    const overdueBills = data.overdueBills || {};
    const rooms = data.rooms || {};
    const profit = data.profit || {};

    this.setData({
      loading: false,
      stats: {
        pendingCount: pendingBills.count || 0,
        pendingAmount: this._formatAmount(pendingBills.amount),
        overdueCount: overdueBills.count || 0,
        overdueAmount: this._formatAmount(overdueBills.amount),
        currentDeposit: this._formatAmount(data.effectiveDepositAmount)
      },
      roomStats: {
        totalCount: rooms.total != null ? rooms.total : 0,
        rentedCount: rooms.occupied != null ? rooms.occupied : 0,
        vacantCount: rooms.vacant != null ? rooms.vacant : 0,
        expiringCount: rooms.expiringSoon != null ? rooms.expiringSoon : 0
      },
      profitStats: {
        totalIncome: this._formatAmount(profit.income),
        totalExpense: this._formatAmount(profit.expense),
        profit: this._formatAmount(profit.profit)
      }
    });
  },

  // 失败时展示示例数据（保证界面非空）
  _applyMockData() {
    this.setData({
      loading: false,
      stats: {
        pendingCount: 0,
        pendingAmount: '0.00',
        overdueCount: 0,
        overdueAmount: '0.00',
        currentDeposit: '0.00'
      },
      roomStats: {
        totalCount: 0,
        rentedCount: 0,
        vacantCount: 0,
        expiringCount: 0
      },
      profitStats: {
        totalIncome: '0.00',
        totalExpense: '0.00',
        profit: '0.00'
      }
    });
  },

  // 金额格式化：Number/String → 'x,xxx.xx'
  _formatAmount(val) {
    if (val === null || val === undefined || val === '') return '0.00';
    const num = typeof val === 'number' ? val : parseFloat(String(val));
    if (isNaN(num)) return '0.00';
    const sign = num < 0 ? '-' : '';
    const abs = Math.abs(num);
    const fixed = abs.toFixed(2);
    const parts = fixed.split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return sign + parts.join('.');
  },

  // 时间筛选变化
  onTimeFilterChange(e) {
    const filterId = e.currentTarget.dataset.id;
    if (filterId === this.data.currentTimeFilter) return;

    this.setData({ currentTimeFilter: filterId });
    this.loadDashboardData();
  },

  // 自定义时间选择（使用 wx.showModal 替代不存在的 showDatePicker）
  onCustomTimeSelect() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    const endDateStr = `${year}-${month}-${day}`;
    const startDateStr = `${year}-01-01`;

    wx.showModal({
      title: '自定义时间范围',
      content: `请输入起止日期（格式 YYYY-MM-DD），默认：年初至今`,
      confirmText: '使用默认',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          this.setData({
            currentTimeFilter: 'custom',
            customDateRange: { startDate: startDateStr, endDate: endDateStr },
            currentPeriodText: `${startDateStr} 至 ${endDateStr}`
          });
          this.loadDashboardData();
        }
      }
    });
  },

  // 切换到租客身份（调用后端切换接口，再跳转）
  switchToTenant() {
    wx.showLoading({ title: '切换中...', mask: true });
    app.request({
      url: '/auth/switch',
      method: 'POST',
      showLoading: false,
      data: { targetMemberType: 'TENANT' },
      success: (data) => {
        // 后端返回新的 token/memberId/memberType
        app.saveLoginState(data, 'tenant');
        wx.hideLoading();
        wx.redirectTo({ url: '/pages/tenant/index/index' });
      },
      fail: () => {
        // 后端切换失败时，仍在前端本地切换（便于调试）
        app.setCurrentRole('tenant');
        wx.hideLoading();
        wx.redirectTo({ url: '/pages/tenant/index/index' });
      }
    });
  },

  // ===== 快捷跳转方法 =====

  goToPendingBills() {
    wx.navigateTo({ url: '/pages/bill/income/list/list?status=pending' });
  },

  goToOverdueBills() {
    wx.navigateTo({ url: '/pages/bill/income/list/list?status=overdue' });
  },

  goToRoomList() {
    wx.switchTab({ url: '/pages/room/list/list' });
  },

  goToRentedRooms() {
    wx.navigateTo({ url: '/pages/room/list/list?status=rented' });
  },

  goToVacantRooms() {
    wx.navigateTo({ url: '/pages/room/list/list?status=vacant' });
  },

  goToExpiringRooms() {
    wx.navigateTo({ url: '/pages/room/list/list?expiringSoon=true' });
  },

  goToAddProperty() {
    wx.navigateTo({ url: '/pages/property/add/add' });
  },

  goToAddRoom() {
    wx.navigateTo({ url: '/pages/room/add/add' });
  },

  goToAddContract() {
    wx.navigateTo({ url: '/pages/contract/add/add' });
  },

  goToAddExpense() {
    wx.navigateTo({ url: '/pages/bill/expense/add/add' });
  },

  goToMessages() {
    wx.navigateTo({ url: '/pages/message/list/list' });
  }
});
