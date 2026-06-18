/**
 * 消息通知 API
 */
import { get, put } from './client.js';

/**
 * 查询消息列表
 * @param {object} params 查询参数
 */
export async function getNotifications(params = {}) {
  return await get('/v1/notifications', params);
}

/**
 * 查询未读消息列表
 * @param {number} page 页码
 * @param {number} pageSize 每页数量
 */
export async function getUnreadNotifications(page = 1, pageSize = 20) {
  return await get('/v1/notifications/unread', { page, pageSize });
}

/**
 * 获取未读消息数量
 */
export async function getUnreadCount() {
  return await get('/v1/notifications/count');
}

/**
 * 获取消息详情
 * @param {string} notificationId 消息ID
 */
export async function getNotificationDetail(notificationId) {
  return await get(`/v1/notifications/${notificationId}`);
}

/**
 * 标记单条消息已读
 * @param {string} notificationId 消息ID
 */
export async function markAsRead(notificationId) {
  return await put(`/v1/notifications/${notificationId}/read`);
}

/**
 * 标记全部消息已读
 */
export async function markAllAsRead() {
  return await put('/v1/notifications/read-all');
}

export default {
  getNotifications,
  getUnreadNotifications,
  getUnreadCount,
  getNotificationDetail,
  markAsRead,
  markAllAsRead
};
