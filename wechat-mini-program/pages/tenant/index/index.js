/**
 * 租客首页 - 控制器
 * 后端接口：
 *   GET /member/profile - 个人信息
 *   GET /contracts - 我的合约（DataScopeContext 按 tenant memberId 过滤）
 *   GET /deposits/tenant/deposit - 租客当前有效押金
 *   GET /income-invoices - 我的账单（status=PENDING/OVERDUE 用于状态统计）
 */

const app = getApp();

Page({
  data: {
    showRoleTip: false,

    tenantInfo: {
      name: '',
      phone: ''
    },

    propertyInfo: {
      name: '',
      address: '',
      type: ''
    },

    roomInfo: {
      name: '',
      monthlyRent: '0.00',
      deposit: '0.00',
      area: ''
    },

    contract: {
      id: '',
      status: 'active',
      statusName: '履约中',
      startDate: '',
      endDate: '',
      remainingDays: 0,
      monthlyRent: '0.00'
    },

    billStats: {
      pendingCount: 0,
      pendingAmount: '0.00',
      overdueCount: 0,
      overdueAmount: '0.00'
    },

    landlordContact: '',
    unreadCount: 0,
    loading: false
  },

  onLoad() {
    if (app.globalData.currentRole !== 'tenant') {
      app.setCurrentRole('tenant');
    }
    this._init();
  },

  onShow() {
    if (app.globalData.currentRole === 'tenant') {
      this._init();
    }
  },

  _init() {
    const userInfo = app.globalData.userInfo;
    this.setData({
      showRoleTip: userInfo && userInfo.hasMultipleRoles
    });

    this.loadTenantData();
  },

  loadTenantData() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    const page = this;

    // 并行加载：个人信息 / 合约 / 押金 / 账单 / 消息
    let profile = null;
    let contracts = [];
    let deposit = null;
    let invoices = [];
    let unread = 0;

    const checkDone = () => {
      if (profile && contracts !== null && deposit !== null &&
          invoices !== null && unread !== null) {
        page._applyAll(profile, contracts, deposit, invoices, unread);
      }
    };

    // 1. 个人信息
    app.request({
      url: '/member/profile',
      method: 'GET',
      showLoading: false,
      success: (data) => { profile = data; checkDone(); },
      fail: () => { profile = {}; checkDone(); }
    });

    // 2. 我的合约
    app.request({
      url: '/contracts',
      method: 'GET',
      data: { page: 1, pageSize: 10 },
      showLoading: false,
      success: (data) => { contracts = data || []; checkDone(); },
      fail: () => { contracts = []; checkDone(); }
    });

    // 3. 押金
    app.request({
      url: '/deposits/tenant/deposit',
      method: 'GET',
      showLoading: false,
      success: (data) => { deposit = data; checkDone(); },
      fail: () => { deposit = {}; checkDone(); }
    });

    // 4. 我的账单（仅查询一次用于统计 pending 与 overdue）
    app.request({
      url: '/income-invoices',
      method: 'GET',
      data: { page: 1, pageSize: 50 },
      showLoading: false,
      success: (data) => { invoices = data || []; checkDone(); },
      fail: () => { invoices = []; checkDone(); }
    });

    // 5. 未读消息数
    app.request({
      url: '/v1/notifications/count',
      method: 'GET',
      showLoading: false,
      success: (data) => {
        unread = (data && (data.unreadCount != null)) ? data.unreadCount : 0;
        checkDone();
      },
      fail: () => { unread = 0; checkDone(); }
    });
  },

  _applyAll(profile, contracts, deposit, invoices, unread) {
    const info = profile || {};

    // 脱敏展示
    const tenantName = info.realName || info.name || '';
    const tenantPhone = info.phone || '';

    // 取最新一条有效合约
    const contract = (contracts && contracts.length > 0)
      ? this._pickPrimaryContract(contracts)
      : null;

    // 押金金额（BigDecimal 或 number）
    const depositAmount = deposit && deposit.validDeposit != null
      ? deposit.validDeposit
      : (deposit && deposit.amount != null ? deposit.amount : null);

    // 账单统计
    const stats = this._calcBillStats(invoices);

    // 房间/房源信息
    const roomName = (contract && contract.roomName) || '';
    const houseName = (contract && contract.houseSourceName) || '';

    this.setData({
      loading: false,
      tenantInfo: {
        name: tenantName ? app.maskPrivacy('name', tenantName) : '',
        phone: tenantPhone ? app.maskPrivacy('phone', tenantPhone) : ''
      },
      propertyInfo: {
        name: houseName,
        address: (contract && contract.address) || '',
        type: (contract && contract.houseSourceType) || ''
      },
      roomInfo: {
        name: roomName,
        monthlyRent: this._formatAmount(contract ? contract.monthlyRent : null),
        deposit: this._formatAmount(depositAmount),
        area: (contract && contract.roomArea) || ''
      },
      contract: this._normalizeContract(contract),
      billStats: stats,
      landlordContact: (contract && contract.landlordPhone)
        ? app.maskPrivacy('phone', contract.landlordPhone)
        : '',
      unreadCount: unread
    });
  },

  _pickPrimaryContract(list) {
    // 优先挑选 ACTIVE / 履约中
    const active = list.find(c => (c.status || '').toUpperCase() === 'ACTIVE');
    return active || list[0];
  },

  _normalizeContract(c) {
    if (!c) {
      return {
        id: '',
        status: 'none',
        statusName: '暂无合约',
        startDate: '',
        endDate: '',
        remainingDays: 0,
        monthlyRent: '0.00'
      };
    }
    const status = (c.status || '').toLowerCase();
    const statusName = c.statusDesc || c.statusName ||
      (status === 'active' ? '履约中' :
       status === 'expired' ? '已到期' :
       status === 'terminated' ? '已解约' : c.status);

    // 计算剩余天数
    let remainingDays = c.remainingDays;
    if (remainingDays == null && c.endDate) {
      try {
        const diff = new Date(c.endDate) - new Date();
        remainingDays = Math.max(0, Math.floor(diff / (24 * 3600 * 1000)));
      } catch (e) { remainingDays = 0; }
    }

    return {
      id: c.contractId || c.id || '',
      status: status,
      statusName: statusName,
      startDate: c.startDate || '',
      endDate: c.endDate || '',
      remainingDays: remainingDays || 0,
      monthlyRent: this._formatAmount(c.monthlyRent)
    };
  },

  _calcBillStats(invoices) {
    const list = invoices || [];
    let pendingCount = 0;
    let pendingAmount = 0;
    let overdueCount = 0;
    let overdueAmount = 0;

    list.forEach(inv => {
      const status = (inv.status || '').toUpperCase();
      const amt = this._parseAmount(inv.amount);
      if (status === 'PENDING') {
        pendingCount++;
        pendingAmount += amt;
      } else if (status === 'OVERDUE') {
        overdueCount++;
        overdueAmount += amt;
      }
    });

    return {
      pendingCount,
      pendingAmount: this._formatAmount(pendingAmount),
      overdueCount,
      overdueAmount: this._formatAmount(overdueAmount)
    };
  },

  _parseAmount(val) {
    if (val === null || val === undefined || val === '') return 0;
    const num = typeof val === 'number' ? val : parseFloat(String(val));
    return isNaN(num) ? 0 : num;
  },

  _formatAmount(val) {
    const num = this._parseAmount(val);
    const fixed = Math.abs(num).toFixed(2);
    const parts = fixed.split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return parts.join('.');
  },

  // 切换到房东身份（真实调用后端 /auth/switch）
  switchToLandlord() {
    wx.showLoading({ title: '切换中...', mask: true });
    app.request({
      url: '/auth/switch',
      method: 'POST',
      data: { targetMemberType: 'LANDLORD' },
      showLoading: false,
      success: (data) => {
        app.saveLoginState(data, 'landlord');
        wx.hideLoading();
        wx.redirectTo({ url: '/pages/index/index' });
      },
      fail: () => {
        // 后端切换失败时，仍在前端本地切换（便于调试）
        app.setCurrentRole('landlord');
        wx.hideLoading();
        wx.redirectTo({ url: '/pages/index/index' });
      }
    });
  },

  goToContractDetail() {
    if (!this.data.contract || !this.data.contract.id) return;
    wx.navigateTo({ url: `/pages/contract/detail/detail?id=${this.data.contract.id}` });
  },

  goToBillList() {
    wx.navigateTo({ url: '/pages/tenant/bill/list/list' });
  },

  goToPendingBills() {
    wx.navigateTo({ url: '/pages/tenant/bill/list/list?status=pending' });
  },

  goToOverdueBills() {
    wx.navigateTo({ url: '/pages/tenant/bill/list/list?status=overdue' });
  },

  goToContact() {
    if (!this.data.landlordContact) {
      wx.showToast({ title: '暂未获取房东联系方式', icon: 'none' });
      return;
    }
    wx.showModal({
      title: '房东联系方式',
      content: `房东电话：${this.data.landlordContact}`,
      showCancel: false,
      confirmText: '知道了'
    });
  },

  goToMessages() {
    wx.navigateTo({ url: '/pages/message/list/list' });
  }
});
