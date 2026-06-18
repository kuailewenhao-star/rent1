// 统一账号主体架构 - 类型定义

// 会员类型枚举
export enum MemberType {
  Landlord = 1,      // 房东会员
  Tenant = 2,        // 租客会员
  Admin = 3          // 平台管理员会员
}

// 会员类型名称映射
export const MemberTypeName: Record<number, string> = {
  [MemberType.Landlord]: '房东',
  [MemberType.Tenant]: '租客',
  [MemberType.Admin]: '平台管理员'
}

// 账号状态枚举
export enum AccountStatus {
  Normal = 1,        // 正常
  Disabled = 2,      // 已禁用
  Frozen = 3         // 已冻结
}

// 账号状态名称映射
export const AccountStatusName: Record<number, string> = {
  [AccountStatus.Normal]: '正常',
  [AccountStatus.Disabled]: '已禁用',
  [AccountStatus.Frozen]: '已冻结'
}

// 房源类型枚举
export enum PropertyType {
  Whole = 1,         // 整租房源
  Shared = 2         // 合租房源
}

// 房源类型名称映射
export const PropertyTypeName: Record<number, string> = {
  [PropertyType.Whole]: '整租',
  [PropertyType.Shared]: '合租'
}

// 房源业务状态枚举
export enum PropertyBusinessStatus {
  Normal = 1,        // 正常经营
  Expired = 2,       // 租期到期停用
  Terminated = 3    // 主动终止经营
}

// 房源业务状态名称映射
export const PropertyBusinessStatusName: Record<number, string> = {
  [PropertyBusinessStatus.Normal]: '正常经营',
  [PropertyBusinessStatus.Expired]: '租期到期停用',
  [PropertyBusinessStatus.Terminated]: '主动终止经营'
}

// 房间状态枚举
export enum RoomStatus {
  Vacant = 1,        // 空置中
  Rented = 2         // 已出租
}

// 房间状态名称映射
export const RoomStatusName: Record<number, string> = {
  [RoomStatus.Vacant]: '空置中',
  [RoomStatus.Rented]: '已出租'
}

// 合约状态枚举
export enum ContractStatus {
  Active = 1,           // 履约中
  Expired = 2,          // 已到期完结
  Terminated = 3,      // 提前解约
  Cancelled = 4        // 作废
}

// 合约状态名称映射
export const ContractStatusName: Record<number, string> = {
  [ContractStatus.Active]: '履约中',
  [ContractStatus.Expired]: '已到期完结',
  [ContractStatus.Terminated]: '提前解约',
  [ContractStatus.Cancelled]: '作废'
}

// 账单状态枚举
export enum BillStatus {
  Pending = 1,       // 待支付
  Paid = 2,          // 已支付
  Overdue = 3,       // 逾期未付
  Deposited = 4      // 押金已收
}

// 账单状态名称映射
export const BillStatusName: Record<number, string> = {
  [BillStatus.Pending]: '待支付',
  [BillStatus.Paid]: '已支付',
  [BillStatus.Overdue]: '逾期未付',
  [BillStatus.Deposited]: '押金已收'
}

// 账单类型枚举（收入账单）
export enum BillType {
  Rent = 1,          // 租金
  Deposit = 2,       // 押金
  Water = 3,         // 水费
  Electric = 4,      // 电费
  Gas = 5,           // 燃气费
  Broadband = 6,     // 宽带费
  Property = 7,      // 物业费
  Trash = 8,         // 垃圾清运费
  Other = 9          // 其他杂费
}

// 账单类型名称映射
export const BillTypeName: Record<number, string> = {
  [BillType.Rent]: '租金',
  [BillType.Deposit]: '押金',
  [BillType.Water]: '水费',
  [BillType.Electric]: '电费',
  [BillType.Gas]: '燃气费',
  [BillType.Broadband]: '宽带费',
  [BillType.Property]: '物业费',
  [BillType.Trash]: '垃圾清运费',
  [BillType.Other]: '其他杂费'
}

// 支出账单类型枚举
export enum ExpenseType {
  PropertyRent = 1,    // 房源租金支出
  PropertyDeposit = 2, // 房源押金支出
  Water = 3,           // 水费支出
  Electric = 4,        // 电费支出
  Gas = 5,             // 燃气费支出
  Broadband = 6,       // 宽带费支出
  PropertyFee = 7,     // 物业费支出
  TrashFee = 8,        // 垃圾清运费支出
  Repair = 9,          // 房屋维修支出
  Other = 10           // 其他支出
}

// 支出账单类型名称映射
export const ExpenseTypeName: Record<number, string> = {
  [ExpenseType.PropertyRent]: '房源租金支出',
  [ExpenseType.PropertyDeposit]: '房源押金支出',
  [ExpenseType.Water]: '水费支出',
  [ExpenseType.Electric]: '电费支出',
  [ExpenseType.Gas]: '燃气费支出',
  [ExpenseType.Broadband]: '宽带费支出',
  [ExpenseType.PropertyFee]: '物业费支出',
  [ExpenseType.TrashFee]: '垃圾清运费支出',
  [ExpenseType.Repair]: '房屋维修支出',
  [ExpenseType.Other]: '其他支出'
}

// 计费方式枚举
export enum ChargeType {
  Fixed = 'fixed',      // 固定值计费
  Ratio = 'ratio'       // 比例分摊计费
}

// 计费方式名称映射
export const ChargeTypeName: Record<string, string> = {
  [ChargeType.Fixed]: '固定值',
  [ChargeType.Ratio]: '比例分摊'
}

// 计费周期枚举
export enum ChargeCycle {
  Monthly = '每月',           // 每月
  Quarterly = '每三月',       // 每三月（季度）
  SemiAnnual = '每六月',       // 每六月（半年）
  Annual = '每年'             // 每年
}

// 界面可见性枚举
export enum ViewAccess {
  AdminOnly = 1,   // 仅管理员可见
  All = 2          // 全部可见
}

// 基础分页参数
export interface PageParams {
  page: number
  pageSize: number
}

// 基础分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// 基础响应结构
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
