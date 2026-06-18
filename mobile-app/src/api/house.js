/**
 * 房源管理 API
 */
import { get, post, put } from './client.js';

/**
 * 获取房源列表
 * @param {object} params 查询参数
 * @param {string} params.status 状态筛选
 * @param {string} params.type 类型筛选（WHOLE整租/SHARED合租）
 * @param {string} params.city 城市筛选
 */
export async function getHouseSources(params = {}) {
  return await get('/house-sources', params);
}

/**
 * 获取房源详情
 * @param {string} houseSourceId 房源ID
 */
export async function getHouseSourceDetail(houseSourceId) {
  return await get(`/house-sources/${houseSourceId}`);
}

/**
 * 新增房源
 * @param {object} data 房源信息
 */
export async function createHouseSource(data) {
  return await post('/house-sources', data);
}

/**
 * 编辑房源
 * @param {string} houseSourceId 房源ID
 * @param {object} data 房源信息
 */
export async function updateHouseSource(houseSourceId, data) {
  return await put(`/house-sources/${houseSourceId}`, data);
}

/**
 * 停用房源
 * @param {string} houseSourceId 房源ID
 * @param {string} status 停用原因状态
 */
export async function deactivateHouseSource(houseSourceId, status) {
  return await put(`/house-sources/${houseSourceId}/status`, { status });
}

/**
 * 获取房源下的房间列表
 * @param {string} houseSourceId 房源ID
 */
export async function getRoomsByHouse(houseSourceId) {
  return await get(`/house-sources/${houseSourceId}/rooms`);
}

export default {
  getHouseSources,
  getHouseSourceDetail,
  createHouseSource,
  updateHouseSource,
  deactivateHouseSource,
  getRoomsByHouse
};
