/**
 * 收入账单 API
 */
import { get, post, put } from './client.js';

/**
 * 查询收入账单列表
 * @param {object} params 查询参数
 * @param {string} params.status 状态筛选
 * @param {string} params.roomId 房间ID筛选
 * @param {string} params.feeType 费用类型筛选
 * @param {string} params.billMonth 账单月份筛选
 * @param {number} params.page 页码
 * @param {number} params.pageSize 每页数量
 */
export async function getIncomeInvoices(params = {}) {
  return await get('/income-invoices', params);
}

/**
 * 获取收入账单详情
 * @param {string} invoiceId 账单ID
 */
export async function getIncomeInvoiceDetail(invoiceId) {
  return await get(`/income-invoices/${invoiceId}`);
}

/**
 * 手动录入杂费账单
 * @param {object} data 账单信息
 */
export async function manualCreateInvoice(data) {
  return await post('/income-invoices/manual', data);
}

/**
 * 公摊费用录入与分摊
 * @param {object} data 公摊信息
 */
export async function createSharedInvoice(data) {
  return await post('/income-invoices/shared', data);
}

/**
 * 账单核销（标记已支付）
 * @param {string} invoiceId 账单ID
 * @param {string} paidTime 支付时间
 */
export async function verifyPayment(invoiceId, paidTime = null) {
  return await put(`/income-invoices/${invoiceId}/pay`, { paidTime });
}

export default {
  getIncomeInvoices,
  getIncomeInvoiceDetail,
  manualCreateInvoice,
  createSharedInvoice,
  verifyPayment
};
