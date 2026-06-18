/**
 * 押金结算弹窗组件 - 控制器
 */

Component({
  properties: {
    show: {
      type: Boolean,
      value: false
    },
    contract: {
      type: Object,
      value: null
    }
  },

  data: {
    // 原押金金额
    originalDeposit: '0.00',

    // 结算方式: full-全额退还, partial-扣费退还
    settlementType: 'full',

    // 实际退还金额
    refundAmount: '',

    // 扣费项
    deductionItems: [],

    // 备注
    remark: ''
  },

  methods: {
    preventBubble() {},

    onClose() {
      this.triggerEvent('close');
      this.resetData();
    },

    onSelectType(e) {
      const type = e.currentTarget.dataset.type;
      this.setData({
        settlementType: type,
        refundAmount: type === 'full' ? this.data.originalDeposit : ''
      });
    },

    onAmountInput(e) {
      this.setData({ refundAmount: e.detail.value });
    },

    onAddDeduction() {
      wx.showToast({
        title: '功能开发中',
        icon: 'none'
      });
    },

    onRemarkInput(e) {
      this.setData({ remark: e.detail.value });
    },

    onConfirm() {
      const { settlementType, refundAmount, deductionItems, remark } = this.data;

      if (settlementType === 'partial') {
        const refund = parseFloat(refundAmount) || 0;
        if (refund <= 0 || refund > parseFloat(this.data.originalDeposit)) {
          wx.showToast({
            title: '请输入有效的退还金额',
            icon: 'none'
          });
          return;
        }
      }

      this.triggerEvent('confirm', {
        settlementType,
        refundAmount: settlementType === 'full' ? this.data.originalDeposit : refundAmount,
        deductionItems: settlementType === 'partial' ? deductionItems : [],
        remark
      });

      this.resetData();
    },

    resetData() {
      this.setData({
        settlementType: 'full',
        refundAmount: '',
        deductionItems: [],
        remark: ''
      });
    }
  },

  observers: {
    'contract': function(contract) {
      if (contract && contract.deposit) {
        this.setData({
          originalDeposit: contract.deposit
        });
      }
    }
  }
});
