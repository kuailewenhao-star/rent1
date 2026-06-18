/**
 * 支出账单 API
 */
import { get, post, put, del } from './client.js';

/**
 * 查询支出账单列表
 * @param {object} params 查询参数
 */
export async function getExpenseInvoices(params = {}) {
  return await get('/expense-invoices', params);
}

/**
 * 获取支出账单详情
 * @param {string} expenseId 账单ID
 */
export async function getExpenseInvoiceDetail(expenseId) {
  return await get(`/expense-invoices/${expenseId}`);
}

/**
 * 新增支出账单
 * @param {object} data 账单信息
 */
export async function createExpenseInvoice(data) {
  return await post('/expense-invoices', data);
}

/**
 * 编辑支出账单
 * @param {string} expenseId 账单ID
 * @param {object} data 账单信息
 */
export async function updateExpenseInvoice(expenseId, data) {
  return await put(`/expense-invoices/${expenseId}`, data);
}

/**
 * 删除支出账单
 * @param {string} expenseId 账单ID
 */
export async function deleteExpenseInvoice(expenseId) {
  return await del(`/expense-invoices/${expenseId}`);
}

export default {
  getExpenseInvoices,
  getExpenseInvoiceDetail,
  createExpenseInvoice,
  updateExpenseInvoice,
  deleteExpenseInvoice
};
