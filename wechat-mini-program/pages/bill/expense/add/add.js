/**
 * 添加支出账单页面 - 控制器
 */

const app = getApp();

Page({
  data: {
    // 房源列表
    propertyList: [
      { id: 'P001', name: '星河湾花园A栋' },
      { id: 'P002', name: '万科城市花园' },
      { id: 'P003', name: '珠江帝景苑' }
    ],

    // 当前选中的房源索引
    selectedPropertyIndex: -1,

    // 支出费用类型枚举
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

    // 当前选中的费用类型索引
    selectedFeeTypeIndex: -1,

    // 表单数据
    formData: {
      propertyId: '',
      propertyName: '',
      feeTypeId: '',
      feeTypeName: '',
      amount: '',
      costDate: '',
      remark: ''
    },

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

  // 房源选择变化
  onPropertyChange(e) {
    const index = e.detail.value;
    const property = this.data.propertyList[index];
    this.setData({
      selectedPropertyIndex: index,
      'formData.propertyId': property.id,
      'formData.propertyName': property.name
    });
    this.checkCanSubmit();
  },

  // 费用类型选择变化
  onFeeTypeChange(e) {
    const index = e.detail.value;
    const feeType = this.data.expenseFeeTypes[index];
    this.setData({
      selectedFeeTypeIndex: index,
      'formData.feeTypeId': feeType.id,
      'formData.feeTypeName': feeType.name
    });
    this.checkCanSubmit();
  },

  // 日期选择变化
  onDateChange(e) {
    const date = e.detail.value;
    this.setData({
      'formData.costDate': date
    });
    this.checkCanSubmit();
  },

  // 备注输入变化
  onRemarkInput(e) {
    this.setData({
      'formData.remark': e.detail.value
    });
  },

  // 检查能否提交
  checkCanSubmit() {
    const { propertyId, feeTypeId, amount, costDate } = this.data.formData;

    const canSubmit = !!(
      propertyId &&
      feeTypeId &&
      amount &&
      parseFloat(amount) > 0 &&
      costDate
    );

    this.setData({ canSubmit });
  },

  // 金额输入变化
  onAmountInput(e) {
    this.setData({
      'formData.amount': e.detail.value
    });
    this.checkCanSubmit();
  },

  // 提交表单
  onSubmit(e) {
    if (!this.data.canSubmit) {
      wx.showToast({
        title: '请完善支出信息',
        icon: 'none'
      });
      return;
    }

    const formData = this.data.formData;

    // 校验金额
    const amount = parseFloat(formData.amount);
    if (isNaN(amount) || amount <= 0) {
      wx.showToast({
        title: '请输入有效金额',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    // TODO: 调用后端API创建支出账单
    // app.request({
    //   url: '/api/bill/expense/create',
    //   method: 'POST',
    //   data: formData,
    //   success: (res) => {
    //     wx.hideLoading();
    //     wx.showToast({
    //       title: '录入成功',
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
        title: '录入成功',
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
