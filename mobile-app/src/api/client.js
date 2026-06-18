/**
 * API 客户端封装
 * 统一处理请求拦截、响应处理、Token管理、错误处理等
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

// 存储Token的Key
const TOKEN_KEY = 'auth_token';
const MEMBER_ID_KEY = 'member_id';
const MEMBER_TYPE_KEY = 'member_type';

/**
 * 获取存储的Token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

/**
 * 设置Token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

/**
 * 清除Token
 */
export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(MEMBER_ID_KEY);
  localStorage.removeItem(MEMBER_TYPE_KEY);
}

/**
 * 获取当前会员ID
 */
export function getMemberId() {
  return localStorage.getItem(MEMBER_ID_KEY);
}

/**
 * 设置当前会员ID
 */
export function setMemberId(memberId) {
  localStorage.setItem(MEMBER_ID_KEY, memberId);
}

/**
 * 获取当前会员类型
 */
export function getMemberType() {
  return localStorage.getItem(MEMBER_TYPE_KEY);
}

/**
 * 设置当前会员类型
 */
export function setMemberType(memberType) {
  localStorage.setItem(MEMBER_TYPE_KEY, memberType);
}

/**
 * 生成签名头（防重放攻击）
 */
function generateIntegrityHeaders() {
  const timestamp = Date.now();
  const nonce = Math.random().toString(36).substring(2, 15);
  const signature = `ts=${timestamp},nonce=${nonce}`;
  return {
    'X-Timestamp': timestamp.toString(),
    'X-Nonce': nonce,
    'X-Signature': signature
  };
}

/**
 * 检查响应状态
 */
async function checkResponse(response) {
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '网络错误' }));
    throw new ApiError(response.status, error.code || 'NETWORK_ERROR', error.message || '网络请求失败');
  }
  return response.json();
}

/**
 * API错误类
 */
export class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
    this.name = 'ApiError';
  }
}

/**
 * 核心请求方法
 */
async function request(method, path, data = null, params = null) {
  const url = new URL(`${BASE_URL}${path}`, window.location.origin);
  
  // 添加查询参数
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        url.searchParams.append(key, value);
      }
    });
  }

  const headers = {
    'Content-Type': 'application/json',
    ...generateIntegrityHeaders()
  };

  // 添加Token
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const options = {
    method,
    headers
  };

  if (data && (method === 'POST' || method === 'PUT' || method === 'DELETE')) {
    options.body = JSON.stringify(data);
  }

  try {
    const response = await fetch(url.toString(), options);
    const result = await checkResponse(response);
    
    // 处理业务错误
    if (result.success === false) {
      throw new ApiError(response.status, result.code || 'BUSINESS_ERROR', result.message || '业务处理失败');
    }
    
    return result;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(0, 'NETWORK_ERROR', error.message || '网络请求失败');
  }
}

/**
 * GET请求
 */
export async function get(path, params) {
  return request('GET', path, null, params);
}

/**
 * POST请求
 */
export async function post(path, data) {
  return request('POST', path, data);
}

/**
 * PUT请求
 */
export async function put(path, data) {
  return request('PUT', path, data);
}

/**
 * DELETE请求
 */
export async function del(path, data) {
  return request('DELETE', path, data);
}

// 导出API方法
export const api = {
  get,
  post,
  put,
  delete: del,
  
  // 便捷方法
  request,
  getToken,
  setToken,
  clearAuth,
  getMemberId,
  setMemberId,
  getMemberType,
  setMemberType
};

export default api;
