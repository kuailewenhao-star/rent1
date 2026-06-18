/**
 * 添加房源页面 - 控制器
 */

const app = getApp();

Page({
  data: {
    // 表单数据
    formData: {
      name: '',
      type: '',           // whole: 整租, shared: 合租
      province: '',
      city: '',
      district: '',
      address: '',
      layout: '',
      rentStartDate: '',
      rentEndDate: ''
    },

    // 区域选择值 [省, 市, 区]
    region: [],

    // 房源类型选项
    propertyTypes: [
      {
        id: 'whole',
        name: '整租房源',
        icon: '🏠',
        desc: '整套出租，默认生成1间房间'
      },
      {
        id: 'shared',
        name: '合租房源',
        icon: '🏢',
        desc: '分间出租，默认生成2间房间'
      }
    ],

    // 能否提交
    canSubmit: false
  },

  onLoad() {
    // 检查角色权限
    if (app.globalData.currentRole === 'tenant') {
      wx.showToast({
        title: '无访问权限',
        icon: 'none'
      });
      wx.navigateBack();
      return;
    }
  },

  // 房源类型选择
  onTypeSelect(e) {
    const type = e.currentTarget.dataset.id;
    this.setData({
      'formData.type': type
    });
    this.checkCanSubmit();
  },

  // 区域选择变化
  onRegionChange(e) {
    const region = e.detail.value;
    this.setData({
      region: region,
      'formData.province': region[0],
      'formData.city': region[1],
      'formData.district': region[2]
    });
    this.checkCanSubmit();
  },

  // 承租开始时间选择
  onRentStartChange(e) {
    const date = e.detail.value;
    this.setData({
      'formData.rentStartDate': date
    });
    this.checkCanSubmit();
  },

  // 承租结束时间选择
  onRentEndChange(e) {
    const date = e.detail.value;
    this.setData({
      'formData.rentEndDate': date
    });
    this.checkCanSubmit();
  },

  // 表单输入变化
  onFormInput(e) {
    const field = e.currentTarget.dataset.field;
    const value = e.detail.value;
    this.setData({
      [`formData.${field}`]: value
    });
    this.checkCanSubmit();
  },

  // 检查能否提交
  checkCanSubmit() {
    const { name, type, province, city, district, address, rentStartDate, rentEndDate } = this.data.formData;

    const canSubmit = !!(
      name.trim() &&
      type &&
      province &&
      city &&
      district &&
      address.trim() &&
      rentStartDate &&
      rentEndDate
    );

    this.setData({ canSubmit });
  },

  // 提交表单
  onSubmit(e) {
    if (!this.data.canSubmit) {
      wx.showToast({
        title: '请完善房源信息',
        icon: 'none'
      });
      return;
    }

    const formData = this.data.formData;

    // 校验承租结束时间必须大于开始时间
    if (new Date(formData.rentEndDate) <= new Date(formData.rentStartDate)) {
      wx.showToast({
        title: '承租结束时间必须晚于开始时间',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '创建中...' });

    // TODO: 调用后端API创建房源
    // app.request({
    //   url: '/api/property/create',
    //   method: 'POST',
    //   data: formData,
    //   success: (res) => {
    //     wx.hideLoading();
    //     wx.showToast({
    //       title: '创建成功',
    //       icon: 'success',
    //       success: () => {
    //         setTimeout(() => {
    //           wx.navigateBack();
    //         }, 1500);
    //       }
    //     });
    //   },
    //   fail: () => {
    //     wx.hideLoading();
    //   }
    // });

    // 模拟成功
    setTimeout(() => {
      wx.hideLoading();
      wx.showToast({
        title: '创建成功',
        icon: 'success',
        success: () => {
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        }
      });
    }, 1000);
  }
});
