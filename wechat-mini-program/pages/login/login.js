/**
 * 登录页面 - 控制器
 * 后端接口：POST /auth/wechat/login
 * 请求体：{ code: '微信登录code', memberType: 'LANDLORD' / 'TENANT' }
 * 响应体：{ token: 'JWT token', memberId: 'xxx', memberType: 'LANDLORD' / 'TENANT', hasAccount: true/false }
 */

const app = getApp();

Page({
  data: {
    // 是否显示身份选择弹窗
    showRolePicker: false,
    // 微信授权code
    wxCode: ''
  },

  onLoad() {
    // 检查是否已登录（有 token 即视为已登录）
    if (app.isLoggedIn()) {
      const currentRole = app.globalData.currentRole || 'landlord';
      if (currentRole === 'tenant') {
        wx.redirectTo({ url: '/pages/tenant/index/index' });
      } else {
        wx.redirectTo({ url: '/pages/index/index' });
      }
    }
  },

  // 获取手机号授权 - 点击按钮后先通过 wx.login 获取 code，再弹出角色选择
  onGetPhoneNumber(e) {
    // 注意：真实小程序需要调用 wx.login 获取 code。
    // 本地开发时，若后端未配置真实微信 AppID/Secret，也可以传 'test_login_code'
    wx.login({
      success: (res) => {
        this.setData({ wxCode: res.code || 'test_login_code' });
        // 弹出角色选择弹窗
        this.setData({ showRolePicker: true });
      },
      fail: () => {
        // wx.login 失败时，提供一个测试code（本地开发场景）
        this.setData({ wxCode: 'test_login_code' });
        this.setData({ showRolePicker: true });
      }
    });
  },

  // 关闭身份选择弹窗
  onCloseRolePicker() {
    this.setData({ showRolePicker: false });
  },

  // 选择身份 - 调用后端微信登录接口
  onSelectRole(e) {
    const role = e.currentTarget.dataset.role; // 'landlord' / 'tenant'
    const memberType = role === 'tenant' ? 'TENANT' : 'LANDLORD';

    this.setData({ showRolePicker: false });

    wx.showLoading({ title: '登录中...', mask: true });

    // 调用后端登录接口
    app.request({
      url: '/auth/wechat/login',
      method: 'POST',
      showLoading: false, // 已手动显示 loading
      data: {
        code: this.data.wxCode,
        memberType: memberType
      },
      success: (data) => {
        // data: { token, memberId, memberType, hasAccount }
        console.log('登录成功:', data);

        // 保存登录态
        app.saveLoginState(data, role);

        // 保存用户信息（脱敏后）
        const userInfo = {
          name: app.maskPrivacy('name', '用户' + (data.memberId || '').slice(-4)),
          phone: '138****' + (data.memberId ? String(data.memberId).slice(-4) : '0000'),
          memberId: data.memberId,
          memberType: data.memberType,
          hasAccount: data.hasAccount
        };
        app.globalData.userInfo = userInfo;
        wx.setStorageSync('userInfo', userInfo);

        wx.hideLoading();

        wx.showToast({
          title: '登录成功',
          icon: 'success',
          duration: 1000,
          success: () => {
            // 根据登录角色跳转
            setTimeout(() => {
              const finalRole = app.globalData.currentRole;
              if (finalRole === 'tenant') {
                wx.redirectTo({ url: '/pages/tenant/index/index' });
              } else {
                wx.redirectTo({ url: '/pages/index/index' });
              }
            }, 1000);
          }
        });
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('登录失败:', err);
        // 后端未配置微信接口时，走 mock 登录（方便本地调试UI功能）
        wx.showModal({
          title: '登录提示',
          content: '后端登录接口未准备好，是否使用测试数据登录？\n（用于调试界面，非真实登录）',
          confirmText: '测试登录',
          cancelText: '取消',
          success: (modalRes) => {
            if (modalRes.confirm) {
              this.mockLogin(role);
            }
          }
        });
      }
    });
  },

  // 测试模式：mock 登录（仅用于后端接口未准备好时的调试）
  mockLogin(role) {
    wx.showLoading({ title: '登录中...', mask: true });
    setTimeout(() => {
      const mockMemberId = role === 'tenant' ? 'T-TEST-0001' : 'L-TEST-0001';
      const mockMemberType = role === 'tenant' ? 'TENANT' : 'LANDLORD';

      const mockData = {
        token: 'mock-jwt-token-' + Date.now(),
        memberId: mockMemberId,
        memberType: mockMemberType,
        hasAccount: true
      };

      app.saveLoginState(mockData, role);

      const userInfo = {
        name: app.maskPrivacy('name', '测试' + (role === 'tenant' ? '租客' : '房东')),
        phone: '138****' + String(mockMemberId).slice(-4),
        memberId: mockMemberId,
        memberType: mockMemberType,
        hasAccount: true
      };
      app.globalData.userInfo = userInfo;
      wx.setStorageSync('userInfo', userInfo);

      wx.hideLoading();
      wx.showToast({
        title: '测试登录成功',
        icon: 'success',
        duration: 1000,
        success: () => {
          setTimeout(() => {
            if (role === 'tenant') {
              wx.redirectTo({ url: '/pages/tenant/index/index' });
            } else {
              wx.redirectTo({ url: '/pages/index/index' });
            }
          }, 1000);
        }
      });
    }, 800);
  },

  // 显示用户协议
  onShowAgreement() {
    wx.showModal({
      title: '用户服务协议',
      content: '这里是用户服务协议的内容...\n\n1. 服务条款的确认和接纳\n2. 服务内容\n3. 使用规则\n4. 隐私保护\n5. 免责声明',
      showCancel: false,
      confirmText: '知道了'
    });
  },

  // 显示隐私政策
  onShowPrivacy() {
    wx.showModal({
      title: '隐私政策',
      content: '这里是隐私政策的内容...\n\n1. 信息收集\n2. 信息使用\n3. 信息共享\n4. 信息保护\n5. 您的权利',
      showCancel: false,
      confirmText: '知道了'
    });
  }
});
