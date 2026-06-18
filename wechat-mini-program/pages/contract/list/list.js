/**
 * 合约列表页面 - 控制器
 */

const app = getApp();

Page({
  data: {
    // 是否为房东角色
    isLandlord: true,

    // 状态筛选选项
    statusFilters: [
      { id: 'all', name: '全部', count: 0 },
      { id: 'active', name: '履约中', count: 0 },
      { id: 'expired', name: '已到期', count: 0 },
      { id: 'terminated', name: '已解约', count: 0 }
    ],

    // 当前选中的状态
    currentStatus: 'all',

    // 合约列表
    contractList: [
      {
        id: 'C001',
        roomId: 'R001',
        roomName: '整套',
        propertyId: 'P001',
        propertyName: '星河湾花园A栋',
        status: 'active',
        statusName: '履约中',
        statusClass: 'tag-active',
        tenantName: '张***',
        tenantPhone: '138****5678',
        startDate: '2024-03-01',
        endDate: '2025-02-28',
        monthlyRent: '4500',
        remainingDays: 256
      },
      {
        id: 'C002',
        roomId: 'R002',
        roomName: '主卧',
        propertyId: 'P002',
        propertyName: '万科城市花园',
        status: 'active',
        statusName: '履约中',
        statusClass: 'tag-active',
        tenantName: '李***',
        tenantPhone: '139****8765',
        startDate: '2024-01-15',
        endDate: '2024-07-14',
        monthlyRent: '2200',
        remainingDays: 27
      },
      {
        id: 'C003',
        roomId: 'R005',
        roomName: '次卧B',
        propertyId: 'P002',
        propertyName: '万科城市花园',
        status: 'expired',
        statusName: '已到期完结',
        statusClass: 'tag-expired',
        tenantName: '王***',
        tenantPhone: '136****4321',
        startDate: '2023-06-01',
        endDate: '2024-05-31',
        monthlyRent: '2000',
        remainingDays: -17
      },
      {
        id: 'C004',
        roomId: 'R006',
        roomName: '单间03',
        propertyId: 'P001',
        propertyName: '星河湾花园A栋',
        status: 'terminated',
        statusName: '提前解约',
        statusClass: 'tag-terminated',
        tenantName: '赵***',
        tenantPhone: '137****9876',
        startDate: '2024-02-01',
        endDate: '2025-01-31',
        monthlyRent: '2500',
        remainingDays: -45
      }
    ]
  },

  onLoad(options) {
    // 检查角色权限
    const currentRole = app.globalData.currentRole;
    this.setData({
      isLandlord: currentRole === 'landlord'
    });

    // 如果是租客，设置tabBar
    if (!this.data.isLandlord) {
      // 租客房合约列表只展示个人
    }

    // 处理筛选参数
    if (options.status) {
      this.setData({ currentStatus: options.status });
    }

    this.loadContractList();
  },

  onShow() {
    if (this.data.isLandlord) {
      this.loadContractList();
    }
  },

  // 加载合约列表
  loadContractList() {
    // 更新各状态数量
    const statusCounts = {
      all: this.data.contractList.length,
      active: 0,
      expired: 0,
      terminated: 0
    };

    this.data.contractList.forEach(contract => {
      if (statusCounts.hasOwnProperty(contract.status)) {
        statusCounts[contract.status]++;
      }
    });

    const statusFilters = this.data.statusFilters.map(item => ({
      ...item,
      count: statusCounts[item.id] || 0
    }));

    this.setData({ statusFilters });

    // TODO: 调用后端API获取合约列表
    // app.request({
    //   url: '/api/contract/list',
    //   data: { status: this.data.currentStatus },
    //   success: (data) => {
    //     this.setData({ contractList: data });
    //   }
    // });
  },

  // 状态筛选变化
  onStatusChange(e) {
    const status = e.currentTarget.dataset.id;
    if (status === this.data.currentStatus) return;

    this.setData({ currentStatus: status });
    this.loadContractList();
  },

  // 跳转详情
  goToDetail(e) {
    const contractId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/contract/detail/detail?id=${contractId}`
    });
  },

  // 跳转新建合约
  goToAddContract() {
    wx.navigateTo({
      url: '/pages/contract/add/add'
    });
  }
});
