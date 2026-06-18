/**
 * 房源列表页面 - 控制器
 */

const app = getApp();

Page({
  data: {
    // 视图模式：list / card
    viewMode: 'list',

    // 状态筛选选项
    statusFilters: [
      { id: 'all', name: '全部' },
      { id: 'normal', name: '正常经营' },
      { id: 'expired', name: '到期停用' },
      { id: 'terminated', name: '已终止' }
    ],

    // 当前选中的状态
    currentStatus: 'all',

    // 房源列表
    propertyList: [
      {
        id: 'P001',
        name: '星河湾花园A栋',
        province: '广东省',
        city: '广州市',
        district: '天河区',
        address: '天河路123号',
        type: 'whole',
        typeName: '整租',
        bizStatus: 'normal',
        bizStatusName: '正常经营',
        bizStatusClass: 'tag-normal',
        roomCount: 5,
        rentedCount: 3,
        vacantCount: 2,
        rentStartDate: '2024-01-01',
        rentEndDate: '2026-12-31'
      },
      {
        id: 'P002',
        name: '万科城市花园',
        province: '广东省',
        city: '广州市',
        district: '白云区',
        address: '白云大道789号',
        type: 'shared',
        typeName: '合租',
        bizStatus: 'normal',
        bizStatusName: '正常经营',
        bizStatusClass: 'tag-normal',
        roomCount: 8,
        rentedCount: 6,
        vacantCount: 2,
        rentStartDate: '2023-06-01',
        rentEndDate: '2025-05-31'
      },
      {
        id: 'P003',
        name: '珠江帝景苑',
        province: '广东省',
        city: '广州市',
        district: '海珠区',
        address: '滨江东路456号',
        type: 'whole',
        typeName: '整租',
        bizStatus: 'expired',
        bizStatusName: '到期停用',
        bizStatusClass: 'tag-expired',
        roomCount: 3,
        rentedCount: 0,
        vacantCount: 3,
        rentStartDate: '2022-01-01',
        rentEndDate: '2024-12-31'
      }
    ]
  },

  onLoad(options) {
    // 检查角色权限
    if (app.globalData.currentRole === 'tenant') {
      wx.showToast({
        title: '无访问权限',
        icon: 'none'
      });
      wx.redirectTo({
        url: '/pages/tenant/index/index'
      });
      return;
    }

    // 处理筛选参数
    if (options.status) {
      this.setData({ currentStatus: options.status });
    }

    this.loadPropertyList();
  },

  onShow() {
    // 每次显示时刷新列表
    if (app.globalData.currentRole === 'landlord') {
      this.loadPropertyList();
    }
  },

  // 加载房源列表
  loadPropertyList() {
    // TODO: 调用后端API获取房源列表
    // app.request({
    //   url: '/api/property/list',
    //   data: { status: this.data.currentStatus },
    //   success: (data) => {
    //     this.setData({ propertyList: data });
    //   }
    // });
  },

  // 状态筛选变化
  onStatusChange(e) {
    const status = e.currentTarget.dataset.id;
    if (status === this.data.currentStatus) return;

    this.setData({ currentStatus: status });
    this.loadPropertyList();
  },

  // 视图切换
  onViewChange(e) {
    const mode = e.currentTarget.dataset.mode;
    if (mode === this.data.viewMode) return;

    this.setData({ viewMode: mode });
    wx.setStorageSync('propertyViewMode', mode);
  },

  // 跳转详情页
  goToDetail(e) {
    const propertyId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/property/detail/detail?id=${propertyId}`
    });
  },

  // 跳转添加房源
  goToAddProperty() {
    wx.navigateTo({
      url: '/pages/property/add/add'
    });
  }
});
