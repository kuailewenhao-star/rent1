/**
 * 我的页面 - 控制器（房东/租客双角色共用）
 * 后端接口：
 *   GET /member/profile - 个人信息
 *   GET /landlord/dashboard - 房东端概览（仅 LANDLORD）
 *   GET /deposits/landlord/total - 房东押金总额（仅 LANDLORD）
 *   GET /deposits/tenant/deposit - 租客押金余额（仅 TENANT）
 *   POST /auth/switch - 角色切换（返回新 token/memberId/memberType）
 */

const app = getApp();

Page({
  data: {
    currentRole: 'landlord',

    userInfo: {
      name: '',
      phone: '',
      avatar: ''
    },

    hasMultipleRoles: false,
    tenantDeposit: '0.00',

    landlordStats: {
      properties: 0,
      rooms: 0,
      contracts: 0
    },

    unreadCount: 0,
    loading: false
  },

  onLoad() {
    const role = app.globalData.currentRole;
    this.setData({ currentRole: role });
    this._refreshProfile();
  },

  onShow() {
    const role = app.globalData.currentRole;
    this.setData({ currentRole: role });
    this._refreshProfile();
  },

  _refreshProfile() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    const page = this;
    let profile = null;
    let extras = {};

    const finalize = () => {
      if (profile === null || extras.role === undefined) return;

      const info = profile || {};
      const name = info.realName || info.name || '';
      const phone = info.phone || '';

      this.setData({
        loading: false,
        userInfo: {
          name: name ? app.maskPrivacy('name', name) : '',
          phone: phone ? app.maskPrivacy('phone', phone) : '',
          avatar: info.avatar || ''
        },
        hasMultipleRoles: !!(info.hasMultipleRoles || (app.globalData.userInfo && app.globalData.userInfo.hasMultipleRoles)),
        tenantDeposit: extras.tenantDeposit || '0.00',
        landlordStats: extras.landlordStats || { properties: 0, rooms: 0, contracts: 0 },
        unreadCount: extras.unreadCount || 0
      });
    };

    // 1. 个人信息（全角色）
    app.request({
      url: '/member/profile',
      method: 'GET',
      showLoading: false,
      success: (data) => { profile = data; finalize(); },
      fail: () => { profile = {}; finalize(); }
    });

    // 2. 角色依赖的补充信息
    const role = app.globalData.currentRole;
    extras.role = role;

    if (role === 'landlord') {
      // 房东：获取看板基础统计
      app.request({
        url: '/landlord/dashboard',
        method: 'GET',
        data: { timeRange: 'THIS_MONTH' },
        showLoading: false,
        success: (data) => {
          const rooms = data && data.rooms ? data.rooms : {};
          extras.landlordStats = {
            properties: rooms.total || 0,
            rooms: rooms.total || 0,
            contracts: (data && data.pendingBills && data.pendingBills.count) || 0
          };
          finalize();
        },
        fail: () => { extras.landlordStats = { properties: 0, rooms: 0, contracts: 0 }; finalize(); }
      });

      // 未读消息
      app.request({
        url: '/v1/notifications/count',
        method: 'GET',
        showLoading: false,
        success: (data) => {
          extras.unreadCount = (data && data.unreadCount != null) ? data.unreadCount : 0;
          finalize();
        },
        fail: () => { extras.unreadCount = 0; finalize(); }
      });
    } else {
      // 租客：押金余额
      app.request({
        url: '/deposits/tenant/deposit',
        method: 'GET',
        showLoading: false,
        success: (data) => {
          const amt = (data && data.validDeposit != null) ? data.validDeposit : (data && data.amount);
          extras.tenantDeposit = page._formatAmount(amt);
          finalize();
        },
        fail: () => { extras.tenantDeposit = '0.00'; finalize(); }
      });

      // 未读消息
      app.request({
        url: '/v1/notifications/count',
        method: 'GET',
        showLoading: false,
        success: (data) => {
          extras.unreadCount = (data && data.unreadCount != null) ? data.unreadCount : 0;
          finalize();
        },
        fail: () => { extras.unreadCount = 0; finalize(); }
      });
    }
  },

  _formatAmount(val) {
    if (val === null || val === undefined || val === '') return '0.00';
    const num = typeof val === 'number' ? val : parseFloat(String(val));
    if (isNaN(num)) return '0.00';
    const fixed = Math.abs(num).toFixed(2);
    const parts = fixed.split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return parts.join('.');
  },

  switchToLandlord() {
    if (this.data.currentRole === 'landlord') return;
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
        app.setCurrentRole('landlord');
        wx.hideLoading();
        wx.redirectTo({ url: '/pages/index/index' });
      }
    });
  },

  switchToTenant() {
    if (this.data.currentRole === 'tenant') return;
    wx.showLoading({ title: '切换中...', mask: true });
    app.request({
      url: '/auth/switch',
      method: 'POST',
      data: { targetMemberType: 'TENANT' },
      showLoading: false,
      success: (data) => {
        app.saveLoginState(data, 'tenant');
        wx.hideLoading();
        wx.redirectTo({ url: '/pages/tenant/index/index' });
      },
      fail: () => {
        app.setCurrentRole('tenant');
        wx.hideLoading();
        wx.redirectTo({ url: '/pages/tenant/index/index' });
      }
    });
  },

  goToProperties() { wx.switchTab({ url: '/pages/property/list/list' }); },
  goToRooms() { wx.switchTab({ url: '/pages/room/list/list' }); },
  goToContracts() { wx.switchTab({ url: '/pages/contract/list/list' }); },
  goToMessages() { wx.navigateTo({ url: '/pages/message/list/list' }); },

  goToDeposit() {
    wx.showModal({
      title: '押金',
      content: `当前有效押金 ¥${this.data.tenantDeposit}`,
      showCancel: false,
      confirmText: '知道了'
    });
  },

  goToContact() {
    wx.showModal({
      title: '联系房东',
      content: `房东联系方式：${this.data.userInfo.phone || '暂未获取'}`,
      showCancel: false,
      confirmText: '知道了'
    });
  },

  onLogout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (!res.confirm) return;
        wx.removeStorageSync('token');
        wx.removeStorageSync('userInfo');
        wx.removeStorageSync('memberId');
        wx.removeStorageSync('currentRole');
        app.globalData.token = '';
        app.globalData.userInfo = null;
        app.globalData.currentRole = 'landlord';
        app.globalData.memberId = '';
        wx.redirectTo({ url: '/pages/login/login' });
      }
    });
  }
});
