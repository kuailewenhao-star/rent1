/**
 * 看板/首页数据 API
 */
import { get } from './client.js';

/**
 * 获取房东首页看板数据
 * @param {string} timeRange 时间范围：TODAY | THIS_MONTH | THIS_QUARTER | THIS_YEAR | CUSTOM
 * @param {string} startDate 自定义起始日期
 * @param {string} endDate 自定义结束日期
 */
export async function getDashboard(timeRange = 'THIS_MONTH', startDate = null, endDate = null) {
  const params = { timeRange };
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  
  return await get('/landlord/dashboard', params);
}

/**
 * 获取房源盈利详情
 * @param {string} houseSourceId 房源ID
 * @param {string} timeRange 时间范围
 * @param {string} startDate 自定义起始日期
 * @param {string} endDate 自定义结束日期
 */
export async function getProfitDetail(houseSourceId, timeRange = 'THIS_MONTH', startDate = null, endDate = null) {
  const params = { timeRange };
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  
  return await get(`/landlord/house-sources/${houseSourceId}/profit-detail`, params);
}

export default {
  getDashboard,
  getProfitDetail
};
