/**
 * 房间列表页面 - 控制器
 */

const app = getApp();

Page({
  data: {
    // 是否为房东角色
    isLandlord: true,

    // 统计数据
    stats: {
      totalCount: 12,
      rentedCount: 8,
      vacantCount: 3,
      expiringCount: 1
    },

    // 房间列表
    roomList: [
      {
        id: 'R001',
        roomName: '整套',
        propertyId: 'P001',
        propertyName: '星河湾花园A栋',
        propertyAddress: '天河区天河路123号',
        status: 'rented',
        statusName: '已出租',
        statusClass: 'status-rented',
        isExpiringSoon: false,
        monthlyRent: '4500',
        images: [],
        tenantName: '张***',
        tenantPhone: '138****5678',
        contract: {
          startDate: '2024-03-01',
          endDate: '2025-02-28'
        }
      },
      {
        id: 'R002',
        roomName: '主卧',
        propertyId: 'P002',
        propertyName: '万科城市花园',
        propertyAddress: '白云区白云大道789号',
        status: 'rented',
        statusName: '已出租',
        statusClass: 'status-rented',
        isExpiringSoon: true,
        monthlyRent: '2200',
        images: [],
        tenantName: '李***',
        tenantPhone: '139****8765',
        contract: {
          startDate: '2024-01-15',
          endDate: '2024-07-14'
        }
      },
      {
        id: 'R003',
        roomName: '次卧A',
        propertyId: 'P002',
        propertyName: '万科城市花园',
        propertyAddress: '白云区白云大道789号',
        status: 'vacant',
        statusName: '空置中',
        statusClass: 'status-vacant',
        isExpiringSoon: false,
        monthlyRent: '1800',
        images: []
      },
      {
        id: 'R004',
        roomName: '单间01',
        propertyId: 'P003',
        propertyName: '珠江帝景苑',
        propertyAddress: '海珠区滨江东路456号',
        status: 'vacant',
        statusName: '空置中',
        statusClass: 'status-vacant',
        isExpiringSoon: false,
        monthlyRent: '2800',
        images: []
      }
    ],

    // 房源列表（用于筛选）
    propertyList: [
      { id: '', name: '全部房源' },
      { id: 'P001', name: '星河湾花园A栋' },
      { id: 'P002', name: '万科城市花园' },
      { id: 'P003', name: '珠江帝景苑' }
    ],

    // 当前选中的筛选条件
    selectedPropertyId: '',
    selectedPropertyName: '全部房源',

    // 排序选项
    sortOptions: [
      { id: 'default', name: '默认排序' },
      { id: 'rent_asc', name: '租金从低到高' },
      { id: 'rent_desc', name: '租金从高到低' },
      { id: 'name', name: '房间名称' }
    ],
    currentSort: 'default',
    sortName: '默认排序',

    // 弹窗显示状态
    showPropertyPicker: false,
    showSortPicker: false,

    // 状态筛选
    statusFilter: 'all'
  },

  onLoad(options) {
    // 检查角色权限
    const currentRole = app.globalData.currentRole;
    this.setData({
      isLandlord: currentRole === 'landlord'
    });

    // 如果是租客，加载个人房间
    if (currentRole === 'tenant') {
      this.loadTenantRooms();
    } else {
      // 处理筛选参数
      if (options.status) {
        this.setData({ statusFilter: options.status });
      }
      if (options.propertyId) {
        const property = this.data.propertyList.find(p => p.id === options.propertyId);
        if (property) {
          this.setData({
            selectedPropertyId: property.id,
            selectedPropertyName: property.name
          });
        }
      }
      if (options.expiringSoon) {
        this.setData({ statusFilter: 'expiring' });
      }

      this.loadRoomList();
    }
  },

  onShow() {
    // 刷新列表
    if (this.data.isLandlord) {
      this.loadRoomList();
    }
  },

  // 加载房间列表
  loadRoomList() {
    // TODO: 调用后端API获取房间列表
    // app.request({
    //   url: '/api/room/list',
    //   data: {
    //     propertyId: this.data.selectedPropertyId,
    //     status: this.data.statusFilter,
    //     sort: this.data.currentSort
    //   },
    //   success: (data) => {
    //     this.setData({
    //       roomList: data.list,
    //       stats: data.stats
    //     });
    //   }
    // });
  },

  // 加载租客个人房间
  loadTenantRooms() {
    // TODO: 调用后端API获取租客个人房间
    // app.request({
    //   url: '/api/tenant/rooms',
    //   success: (data) => {
    //     this.setData({
    //       roomList: data.list
    //     });
    //   }
    // });
  },

  // 按状态筛选
  onFilterStatus(e) {
    const status = e.currentTarget.dataset.status;
    if (status === this.data.statusFilter) return;

    this.setData({ statusFilter: status });
    this.loadRoomList();
  },

  // 显示房源选择弹窗
  onShowPropertyPicker() {
    this.setData({ showPropertyPicker: true });
  },

  // 关闭房源选择弹窗
  onClosePropertyPicker() {
    this.setData({ showPropertyPicker: false });
  },

  // 选择房源
  onSelectProperty(e) {
    const { id, name } = e.currentTarget.dataset;
    this.setData({
      selectedPropertyId: id,
      selectedPropertyName: name,
      showPropertyPicker: false
    });
    this.loadRoomList();
  },

  // 显示排序选择弹窗
  onShowSortPicker() {
    this.setData({ showSortPicker: true });
  },

  // 关闭排序选择弹窗
  onCloseSortPicker() {
    this.setData({ showSortPicker: false });
  },

  // 选择排序方式
  onSelectSort(e) {
    const { id, name } = e.currentTarget.dataset;
    this.setData({
      currentSort: id,
      sortName: name,
      showSortPicker: false
    });
    this.loadRoomList();
  },

  // 显示搜索
  onShowSearch() {
    wx.showSearchBar({
      placeholder: '搜索房间名称'
    });
  },

  // 跳转房间详情
  goToDetail(e) {
    const roomId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/room/detail/detail?id=${roomId}`
    });
  },

  // 跳转添加房间
  goToAddRoom() {
    wx.navigateTo({
      url: '/pages/room/add/add'
    });
  }
});
