/**
 * 合约管理 API
 */
import { get, post, put } from './client.js';

/**
 * 查询合约列表
 * @param {object} params 查询参数
 * @param {string} params.status 状态筛选
 * @param {string} params.roomId 房间ID筛选
 * @param {string} params.tenantName 租客姓名筛选
 * @param {number} params.page 页码
 * @param {number} params.pageSize 每页数量
 */
export async function getContracts(params = {}) {
  return await get('/contracts', params);
}

/**
 * 获取合约详情
 * @param {string} contractId 合约ID
 */
export async function getContractDetail(contractId) {
  return await get(`/contracts/${contractId}`);
}

/**
 * 创建合约（房东手动创建）
 * @param {object} data 合约信息
 */
export async function createContract(data) {
  return await post('/contracts', data);
}

/**
 * 生成入驻邀请码
 * @param {string} roomId 房间ID
 */
export async function generateInviteCode(roomId) {
  return await post(`/contracts/rooms/${roomId}/invite`, {});
}

/**
 * 租客自助入驻
 * @param {object} data 入驻信息
 */
export async function selfCheckin(data) {
  return await post('/contracts/self-checkin', data);
}

/**
 * 提前解约
 * @param {string} contractId 合约ID
 * @param {object} data 解约信息
 */
export async function terminateContract(contractId, data) {
  return await put(`/contracts/${contractId}/terminate`, data);
}

/**
 * 作废合约
 * @param {string} contractId 合约ID
 * @param {string} reason 作废原因
 */
export async function voidContract(contractId, reason = null) {
  return await put(`/contracts/${contractId}/void`, { reason });
}

export default {
  getContracts,
  getContractDetail,
  createContract,
  generateInviteCode,
  selfCheckin,
  terminateContract,
  voidContract
};
