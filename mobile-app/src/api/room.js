/**
 * 房间管理 API
 */
import { get, post, put, del } from './client.js';

/**
 * 获取房间列表
 * @param {object} params 查询参数
 * @param {string} params.status 状态筛选
 * @param {string} params.houseSourceId 房源筛选
 * @param {number} params.minRent 最小租金
 * @param {number} params.maxRent 最大租金
 */
export async function getRooms(params = {}) {
  return await get('/rooms', params);
}

/**
 * 获取房间详情
 * @param {string} roomId 房间ID
 */
export async function getRoomDetail(roomId) {
  return await get(`/rooms/${roomId}`);
}

/**
 * 新增房间（合租房源）
 * @param {string} houseSourceId 房源ID
 * @param {object} data 房间信息
 */
export async function createRoom(houseSourceId, data) {
  return await post(`/house-sources/${houseSourceId}/rooms`, data);
}

/**
 * 编辑房间
 * @param {string} roomId 房间ID
 * @param {object} data 房间信息
 */
export async function updateRoom(roomId, data) {
  return await put(`/rooms/${roomId}`, data);
}

/**
 * 上传房间图片
 * @param {string} roomId 房间ID
 * @param {string} imageUrl 图片URL
 */
export async function uploadRoomImage(roomId, imageUrl) {
  return await post(`/rooms/${roomId}/images`, { imageUrl });
}

/**
 * 删除房间图片
 * @param {string} roomId 房间ID
 * @param {string} imageUrl 图片URL
 */
export async function deleteRoomImage(roomId, imageUrl) {
  return await del(`/rooms/${roomId}/images`, { imageUrl });
}

/**
 * 获取房间计费规则
 * @param {string} roomId 房间ID
 */
export async function getBillingRules(roomId) {
  return await get(`/rooms/${roomId}/billing-rules`);
}

export default {
  getRooms,
  getRoomDetail,
  createRoom,
  updateRoom,
  uploadRoomImage,
  deleteRoomImage,
  getBillingRules
};
