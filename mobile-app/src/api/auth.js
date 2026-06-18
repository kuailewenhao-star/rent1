/**
 * 认证相关 API
 */
import { get, post, setToken, setMemberId, setMemberType, clearAuth, getToken } from './client.js';

/**
 * 微信登录
 * @param {string} code 微信授权码
 * @param {string} memberType 会员类型 LANDLORD | TENANT
 */
export async function wechatLogin(code, memberType) {
  const result = await post('/auth/wechat/login', {
    code,
    memberType
  });
  
  if (result.data) {
    setToken(result.data.token);
    setMemberId(result.data.memberId);
    setMemberType(result.data.memberType);
  }
  
  return result.data;
}

/**
 * 身份切换
 * @param {string} targetMemberType 目标会员类型
 */
export async function switchMemberType(targetMemberType) {
  const result = await post('/auth/switch', {
    targetMemberType
  });
  
  if (result.data) {
    setToken(result.data.token);
    setMemberType(result.data.memberType);
  }
  
  return result.data;
}

/**
 * 获取当前用户信息
 */
export async function getProfile() {
  return await get('/member/profile');
}

/**
 * 更新个人信息
 * @param {object} data { realName, idCard, avatar }
 */
export async function updateProfile(data) {
  return await post('/member/profile', data);
}

/**
 * 获取联系人列表
 */
export async function getContacts() {
  return await get('/member/contacts');
}

/**
 * 创建联系人
 * @param {object} data 联系人信息
 */
export async function createContact(data) {
  return await post('/member/contacts', data);
}

/**
 * 删除联系人
 * @param {string} contactId 联系人ID
 */
export async function deleteContact(contactId) {
  return await get(`/member/contacts/${contactId}`);
}

/**
 * 检查是否已登录
 */
export function isLoggedIn() {
  return !!getToken();
}

/**
 * 登出
 */
export function logout() {
  clearAuth();
}

export { setMemberType };

export default {
  wechatLogin,
  switchMemberType,
  getProfile,
  updateProfile,
  getContacts,
  createContact,
  deleteContact,
  isLoggedIn,
  logout,
  setMemberType
};
