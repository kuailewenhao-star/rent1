/**
 * 解约弹窗组件 - 控制器
 */

Component({
  properties: {
    // 是否显示
    show: {
      type: Boolean,
      value: false
    },
    // 标题
    title: {
      type: String,
      value: '合约解约'
    },
    // 合约信息
    contract: {
      type: Object,
      value: null
    }
  },

  data: {
    // 解约类型
    terminationTypes: [
      { id: 'early', name: '提前解约' },
      { id: 'expired', name: '到期解约' }
    ],
    selectedType: 'early',

    // 解约原因
    reasons: [
      { id: 'job_change', name: '工作变动' },
      { id: 'family_reason', name: '家庭原因' },
      { id: 'relocation', name: '搬迁' },
      { id: 'rent_increase', name: '租金涨幅过大' },
      { id: 'maintenance_issue', name: '房屋维修问题' },
      { id: 'other', name: '其他原因' }
    ],
    selectedReasonIndex: -1,

    // 解约日期
    terminationDate: '',

    // 备注
    remark: ''
  },

  computed: {
    canConfirm() {
      return (
        this.data.selectedType &&
        this.data.selectedReasonIndex >= 0 &&
        this.data.terminationDate
      );
    }
  },

  methods: {
    // 阻止事件冒泡
    preventBubble() {},

    // 关闭弹窗
    onClose() {
      this.triggerEvent('close');
      this.resetData();
    },

    // 选择解约类型
    onSelectType(e) {
      const type = e.currentTarget.dataset.id;
      this.setData({ selectedType: type });
    },

    // 选择解约原因
    onReasonChange(e) {
      this.setData({ selectedReasonIndex: e.detail.value });
    },

    // 选择解约日期
    onDateChange(e) {
      this.setData({ terminationDate: e.detail.value });
    },

    // 备注输入
    onRemarkInput(e) {
      this.setData({ remark: e.detail.value });
    },

    // 确认解约
    onConfirm() {
      if (!this.data.canConfirm) {
        wx.showToast({
          title: '请完善解约信息',
          icon: 'none'
        });
        return;
      }

      const reason = this.data.reasons[this.data.selectedReasonIndex];

      this.triggerEvent('confirm', {
        terminationType: this.data.selectedType,
        reasonId: reason.id,
        reasonName: reason.name,
        terminationDate: this.data.terminationDate,
        remark: this.data.remark
      });

      this.resetData();
    },

    // 重置数据
    resetData() {
      this.setData({
        selectedType: 'early',
        selectedReasonIndex: -1,
        terminationDate: '',
        remark: ''
      });
    }
  }
});
