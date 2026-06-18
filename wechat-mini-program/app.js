/**
 * 房东租赁管理系统 V1.0 - 小程序入口文件
 * 角色权限：房东/租客双角色隔离
 * 鉴权方式：JWT Token（请求头 Authorization: Bearer ${token}）
 */

App({
  // 全局数据
  globalData: {
    // 后端 API baseURL（本地开发用 http://localhost:8080/api；上生产替换为 https 域名）
    baseURL: 'http://localhost:8080/api',
    // JWT token（登录后获取，持久化到 storage）
    token: '',
    // 当前用户信息
    userInfo: null,
    // 当前角色：landlord（房东）/ tenant（租客）
    currentRole: 'landlord',
    // 用户ID（旧字段，保留兼容）
    userId: '',
    // 会员ID（后端返回的 memberId）
    memberId: '',

    // 隐私脱敏规则
    privacyMask: {
      // 手机号脱敏：138****5678
      maskPhone: function(phone) {
        if (!phone || phone.length < 7) return phone;
        return phone.substring(0, 3) + '****' + phone.substring(phone.length - 4);
      },
      // 身份证号脱敏：440101********1234
      maskIdCard: function(idCard) {
        if (!idCard || idCard.length < 10) return idCard;
        return idCard.substring(0, 6) + '********' + idCard.substring(idCard.length - 4);
      },
      // 姓名脱敏：张***
      maskName: function(name) {
        if (!name || name.length < 2) return name;
        return name.substring(0, 1) + '***';
      }
    },

    // 收入账单费用类型枚举（9类）
    incomeFeeTypes: [
      { id: 1, name: '租金', isRequired: true },
      { id: 2, name: '押金', isRequired: true },
      { id: 3, name: '水费', isRequired: false },
      { id: 4, name: '电费', isRequired: false },
      { id: 5, name: '燃气费', isRequired: false },
      { id: 6, name: '宽带费', isRequired: false },
      { id: 7, name: '物业费', isRequired: false },
      { id: 8, name: '垃圾清运费', isRequired: false },
      { id: 9, name: '其他杂费', isRequired: false }
    ],

    // 支出账单费用类型枚举（10类）
    expenseFeeTypes: [
      { id: 1, name: '房源租金支出' },
      { id: 2, name: '房源押金支出' },
      { id: 3, name: '水费支出' },
      { id: 4, name: '电费支出' },
      { id: 5, name: '燃气费支出' },
      { id: 6, name: '宽带费支出' },
      { id: 7, name: '物业费支出' },
      { id: 8, name: '垃圾清运费支出' },
      { id: 9, name: '房屋维修支出' },
      { id: 10, name: '其他支出' }
    ],

    // 计费方式枚举
    chargeTypes: [
      { id: 'fixed', name: '固定值计费' },
      { id: 'ratio', name: '比例分摊计费' }
    ],

    // 计费周期枚举
    chargeCycles: [
      { id: 'monthly', name: '每月' },
      { id: 'quarterly', name: '每三月' },
      { id: 'halfYear', name: '每六月' },
      { id: 'yearly', name: '每年' }
    ],

    // 账单状态枚举
    billStatuses: [
      { id: 'pending', name: '待支付', color: '#E6A23C' },
      { id: 'paid', name: '已支付', color: '#67C23A' },
      { id: 'overdue', name: '逾期未付', color: '#F56C6C' }
    ],

    // 房源类型枚举
    propertyTypes: [
      { id: 'whole', name: '整租房源' },
      { id: 'shared', name: '合租房源' }
    ],

    // 房源业务状态枚举
    propertyBizStatuses: [
      { id: 'normal', name: '正常经营' },
      { id: 'expired', name: '租期到期停用' },
      { id: 'terminated', name: '主动终止经营' }
    ],

    // 房间状态枚举
    roomStatuses: [
      { id: 'vacant', name: '空置中' },
      { id: 'rented', name: '已出租' }
    ],

    // 合约状态枚举
    contractStatuses: [
      { id: 'active', name: '履约中', color: '#67C23A' },
      { id: 'expired', name: '已到期完结', color: '#909399' },
      { id: 'terminated', name: '提前解约', color: '#E6A23C' },
      { id: 'cancelled', name: '作废', color: '#F56C6C' }
    ]
  },

  // 小程序初始化
  onLaunch(options) {
    // 检查登录状态（恢复 token、userInfo、role、memberId）
    this.checkLoginStatus();

    // 根据登录后的角色同步 tabBar 状态
    const role = this.globalData.currentRole || 'landlord';
    setTimeout(() => {
      this.updateTabBarByRole(role);
    }, 100);

    console.log('小程序启动', options, 'token已加载:', !!this.globalData.token);
  },

  // 检查登录状态 - 从 storage 恢复 token/userInfo/role/memberId
  checkLoginStatus() {
    const userInfo = wx.getStorageSync('userInfo');
    const token = wx.getStorageSync('token');
    const currentRole = wx.getStorageSync('currentRole') || 'landlord';
    const memberId = wx.getStorageSync('memberId') || '';

    if (userInfo || token) {
      this.globalData.userInfo = userInfo || null;
      this.globalData.token = token || '';
      this.globalData.currentRole = currentRole;
      this.globalData.memberId = memberId;
      this.globalData.userId = memberId; // 兼容旧字段
    }
  },

  // 保存登录状态（登录/切换角色成功后调用）
  saveLoginState(data, role) {
    if (data && data.token) {
      this.globalData.token = data.token;
      wx.setStorageSync('token', data.token);
    }
    if (data && data.memberId) {
      this.globalData.memberId = data.memberId;
      this.globalData.userId = data.memberId;
      wx.setStorageSync('memberId', data.memberId);
    }
    if (data && data.memberType) {
      // 后端返回 LANDLORD / TENANT，统一转为小写 landlord/tenant
      const roleFromBackend = data.memberType.toLowerCase();
      if (roleFromBackend === 'landlord' || roleFromBackend === 'tenant') {
        this.globalData.currentRole = roleFromBackend;
        wx.setStorageSync('currentRole', roleFromBackend);
      }
    } else if (role) {
      this.globalData.currentRole = role;
      wx.setStorageSync('currentRole', role);
    }

    // 更新 tabBar
    this.updateTabBarByRole(this.globalData.currentRole);
  },

  // 清除登录状态（token 过期/退出登录）
  clearLoginState() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    this.globalData.currentRole = 'landlord';
    this.globalData.memberId = '';
    this.globalData.userId = '';
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    wx.removeStorageSync('memberId');
    wx.removeStorageSync('currentRole');
  },

  // 设置当前角色（身份切换）- 切换角色时调用后端接口
  setCurrentRole(role) {
    if (role !== 'landlord' && role !== 'tenant') {
      console.error('无效的角色类型');
      return false;
    }
    this.globalData.currentRole = role;
    wx.setStorageSync('currentRole', role);

    // 根据角色动态控制 tabBar 显隐
    this.updateTabBarByRole(role);
    return true;
  },

  // 根据角色更新 tabBar 显示（房东：全部5个Tab；租客：只保留首页+我的）
  updateTabBarByRole(role) {
    // tabBar 索引：0=首页 1=房源 2=房间 3=合约 4=我的
    try {
      if (role === 'landlord') {
        // 房东：显示所有 Tab
        for (let i = 0; i < 5; i++) {
          wx.showTabBarItem({ index: i, fail: () => {} });
        }
      } else {
        // 租客：隐藏 房源(1)、房间(2)、合约(3)
        [1, 2, 3].forEach(i => {
          wx.hideTabBarItem({ index: i, fail: () => {} });
        });
      }
    } catch (e) {
      console.log('tabBar 动态更新失败', e);
    }
  },

  // 判断是否为房东角色
  isLandlord() {
    return this.globalData.currentRole === 'landlord';
  },

  // 判断是否为租客角色
  isTenant() {
    return this.globalData.currentRole === 'tenant';
  },

  // 检查是否已登录（有 token）
  isLoggedIn() {
    return !!this.globalData.token;
  },

  // 隐私脱敏封装
  maskPrivacy(type, value) {
    const mask = this.globalData.privacyMask;
    switch (type) {
      case 'phone':
        return mask.maskPhone(value);
      case 'idCard':
        return mask.maskIdCard(value);
      case 'name':
        return mask.maskName(value);
      default:
        return value;
    }
  },

  // 通用请求封装（自动注入 JWT Authorization header）
  request(options) {
    const defaultOptions = {
      url: '',
      method: 'GET',
      data: {},
      header: {
        'content-type': 'application/json'
      },
      showLoading: true,
      success: () => {},
      fail: () => {}
    };

    const mergedOptions = { ...defaultOptions, ...options };

    // JWT token 注入：请求头 Authorization: Bearer ${token}
    // 后端 AuthInterceptor 通过解析 token 获取 memberId/memberType
    // 不再在 data 中添加 userId/memberId，避免重复且不符合后端规范
    if (this.globalData.token) {
      mergedOptions.header = mergedOptions.header || {};
      mergedOptions.header['Authorization'] = 'Bearer ' + this.globalData.token;
    }

    if (mergedOptions.showLoading) {
      wx.showLoading({ title: '加载中...', mask: true });
    }

    // 自动拼接 baseURL：如果 url 本身没带 http 开头，就拼上全局 baseURL
    // 注意：baseURL 已经是 http://localhost:8080/api，所以 url 不要以 /api 开头
    const fullUrl = mergedOptions.url.startsWith('http')
      ? mergedOptions.url
      : this.globalData.baseURL + mergedOptions.url;

    const self = this;
    wx.request({
      url: fullUrl,
      method: mergedOptions.method,
      data: mergedOptions.data,
      header: mergedOptions.header,
      success: (res) => {
        if (mergedOptions.showLoading) {
          wx.hideLoading();
        }
        if (!res.data || res.data.code === undefined) {
          wx.showToast({ title: '服务器响应异常', icon: 'none' });
          mergedOptions.fail(res.data);
          return;
        }
        const code = String(res.data.code);
        if (code === '0000') {
          // 成功：返回 data 部分
          mergedOptions.success(res.data.data);
        } else if (code === 'A001') {
          // token 过期/无效：清除登录态并跳转登录页
          wx.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          });
          self.clearLoginState();
          setTimeout(() => {
            wx.redirectTo({ url: '/pages/login/login' });
          }, 1500);
          mergedOptions.fail(res.data);
        } else {
          // 业务错误
          wx.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          });
          mergedOptions.fail(res.data);
        }
      },
      fail: (err) => {
        if (mergedOptions.showLoading) {
          wx.hideLoading();
        }
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
        mergedOptions.fail(err);
      }
    });
  },

  // 通用页面跳转
  navigateTo(url, params = {}) {
    const queryString = Object.keys(params)
      .map(key => `${key}=${encodeURIComponent(params[key])}`)
      .join('&');
    const fullUrl = queryString ? `${url}?${queryString}` : url;
    wx.navigateTo({ url: fullUrl });
  },

  // tabBar页面跳转
  switchTab(url) {
    wx.switchTab({ url });
  },

  // 返回上一页
  navigateBack(delta = 1) {
    wx.navigateBack({ delta });
  },

  // 显示成功提示
  showSuccess(title = '操作成功') {
    wx.showToast({
      title,
      icon: 'success',
      duration: 2000
    });
  },

  // 显示失败提示
  showError(title = '操作失败') {
    wx.showToast({
      title,
      icon: 'none',
      duration: 2000
    });
  },

  // 显示加载中
  showLoading(title = '加载中...') {
    wx.showLoading({
      title,
      mask: true
    });
  },

  // 隐藏加载中
  hideLoading() {
    wx.hideLoading();
  }
});
