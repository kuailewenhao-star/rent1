// ========== 工具函数 ==========
export const maskPhone = (phone) => {
  if (!phone) return '';
  const clean = phone.replace(/\D/g, '');
  if (clean.length !== 11) return phone;
  return clean.slice(0, 3) + '****' + clean.slice(7);
};
export const maskName = (name) => {
  if (!name) return '';
  if (name.length <= 1) return name;
  return name[0] + '*'.repeat(Math.min(name.length - 1, 2));
};
export const fmtMoney = (n) => Number(n || 0).toLocaleString('zh-CN');
export const todayISO = () => {
  const d = new Date();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mm}-${dd}`;
};
// 距今天多少天 diffDays("2024-05-01") — 过期返回 0 而非负数
export const diffDays = (iso) => {
  const end = new Date(iso);
  const start = new Date();
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
  return Math.max(0, diff);
};

// ========== 常量枚举 ==========
export const INCOME_FEE_TYPES = [
  { id: 'rent', label: '租金', color: 'blue' },
  { id: 'deposit', label: '押金', color: 'indigo' }
];
export const EXPENSE_TYPES = [
  { id: 'house_rent', label: '房源租金支出', color: 'blue' },
  { id: 'house_deposit', label: '房源押金支出', color: 'indigo' }
];
export const BILL_STATUSES = [
  { id: 'pending', label: '待支付', color: 'orange' },
  { id: 'paid', label: '已支付', color: 'green' },
  { id: 'overdue', label: '逾期未付', color: 'red' },
  { id: 'deposit_paid', label: '押金已收', color: 'indigo' }
];
export const ROOM_STATUSES = [
  { id: 'vacant', label: '空置中', color: 'slate' },
  { id: 'rented', label: '已出租', color: 'green' }
];
export const CONTRACT_STATUSES = [
  { id: 'active', label: '履约中', color: 'green' },
  { id: 'ended', label: '已到期完结', color: 'slate' },
  { id: 'cancelled', label: '提前解约', color: 'orange' },
  { id: 'void', label: '作废', color: 'red' }
];

// ========== 用户 & 房东 ==========
export const profile = {
  name: '李房东',
  maskName: '李**',
  phone: '13800005678',
  role: 'landlord',
  avatarInitial: '李'
};

// ========== 房源（房屋整套） ==========
// 房源业务状态：正常经营、即将到期、停用
export const houseSources = [
  {
    id: 'hs_01',
    name: '阳光小区3-201',
    type: '整租',
    layout: '2室1厅1卫',
    area: 78,
    province: '广东省',
    city: '广州市',
    district: '天河区',
    address: '建国路88号阳光小区3号楼2单元201',
    status: '正常经营',
    leaseStart: '2024-01-01',
    leaseEnd: '2026-12-31',
    monthlyCost: 2500,
    depositCost: 5000,
    remark: '房东与大房东承租的两房一厅，可分租可整租',
    createTime: '2024-01-01'
  },
  {
    id: 'hs_02',
    name: '碧桂园5-102',
    type: '合租',
    layout: '3室2厅2卫',
    area: 118,
    province: '广东省',
    city: '广州市',
    district: '海珠区',
    address: '中关村大街123号碧桂园5号楼102',
    status: '正常经营',
    leaseStart: '2024-03-01',
    leaseEnd: '2027-02-28',
    monthlyCost: 3800,
    depositCost: 7600,
    remark: '大三房，分主卧/次卧A/次卧B三间合租',
    createTime: '2024-03-01'
  },
  {
    id: 'hs_03',
    name: '万科城2-305',
    type: '合租',
    layout: '2室2厅1卫',
    area: 89,
    province: '广东省',
    city: '广州市',
    district: '番禺区',
    address: '南三环路56号万科城2号楼305',
    status: '正常经营',
    leaseStart: '2023-09-01',
    leaseEnd: '2026-08-31',
    monthlyCost: 2800,
    depositCost: 5600,
    remark: '两房一厅合租房源',
    createTime: '2023-09-01'
  },
  {
    id: 'hs_04',
    name: '恒大华府1-401',
    type: '整租',
    layout: '3室1厅2卫',
    area: 106,
    province: '广东省',
    city: '广州市',
    district: '越秀区',
    address: '金融街15号恒大华府1号楼401',
    status: '正常经营',
    leaseStart: '2025-01-01',
    leaseEnd: '2027-12-31',
    monthlyCost: 3500,
    depositCost: 7000,
    remark: '金融街新签约房源',
    createTime: '2025-01-01'
  }
];

// ========== 房间（最小出租单元，按房源聚合） ==========
// 整租房源自动生成 1 间房间；合租房源自动生成多间
export const rooms = [
  // 阳光小区3-201：整租，1间
  { id: 'rm_01_01', houseId: 'hs_01', roomNo: '整套', area: 78, rent: 3200, deposit: 6400, status: 'rented', chargeCycle: '每月', feeItems: [
    { feeType: '租金', chargeType: 'fixed', chargeValue: 3200, chargeCycle: '每月', remark: '月度固定房租租金收入' },
    { feeType: '押金', chargeType: 'fixed', chargeValue: 6400, chargeCycle: '', remark: '租客租房履约保证金，一次性收入，退租无违约可退还' },
    { feeType: '水费', chargeType: 'fixed', chargeValue: 50, chargeCycle: '每月', remark: '月度固定水费包干收入' },
    { feeType: '电费', chargeType: 'fixed', chargeValue: 80, chargeCycle: '每月', remark: '月度固定电费包干收入' },
    { feeType: '物业费', chargeType: 'fixed', chargeValue: 200, chargeCycle: '每六月', remark: '半年结算一次物业费收入' }
  ]},
  // 碧桂园5-102：合租，3间
  { id: 'rm_02_01', houseId: 'hs_02', roomNo: '主卧', area: 22, rent: 2000, deposit: 4000, status: 'rented', chargeCycle: '每月', feeItems: [
    { feeType: '租金', chargeType: 'fixed', chargeValue: 2000, chargeCycle: '每月', remark: '主卧月租收入' },
    { feeType: '押金', chargeType: 'fixed', chargeValue: 4000, chargeCycle: '', remark: '主卧押金' },
    { feeType: '水费', chargeType: 'ratio', chargeValue: 0.4, chargeCycle: '每月', remark: '合租水费按40%比例分摊' },
    { feeType: '电费', chargeType: 'fixed', chargeValue: 120, chargeCycle: '每月', remark: '主卧月度固定电费包干' },
    { feeType: '宽带费', chargeType: 'fixed', chargeValue: 50, chargeCycle: '每月', remark: '主卧宽带分摊' }
  ]},
  { id: 'rm_02_02', houseId: 'hs_02', roomNo: '次卧A', area: 15, rent: 1500, deposit: 3000, status: 'rented', chargeCycle: '每月', feeItems: [
    { feeType: '租金', chargeType: 'fixed', chargeValue: 1500, chargeCycle: '每月', remark: '次卧A月租收入' },
    { feeType: '押金', chargeType: 'fixed', chargeValue: 3000, chargeCycle: '', remark: '次卧A押金' },
    { feeType: '水费', chargeType: 'ratio', chargeValue: 0.3, chargeCycle: '每月', remark: '合租水费按30%比例分摊' },
    { feeType: '电费', chargeType: 'fixed', chargeValue: 80, chargeCycle: '每月', remark: '次卧A月度电费包干' },
    { feeType: '燃气费', chargeType: 'ratio', chargeValue: 0.4, chargeCycle: '每三月', remark: '次卧A燃气费按40%比例季度分摊' }
  ]},
  { id: 'rm_02_03', houseId: 'hs_02', roomNo: '次卧B', area: 12, rent: 1100, deposit: 2200, status: 'vacant', chargeCycle: '每月', feeItems: [
    { feeType: '租金', chargeType: 'fixed', chargeValue: 1100, chargeCycle: '每月', remark: '次卧B月租收入' },
    { feeType: '押金', chargeType: 'fixed', chargeValue: 2200, chargeCycle: '', remark: '次卧B押金' },
    { feeType: '水费', chargeType: 'ratio', chargeValue: 0.3, chargeCycle: '每月', remark: '合租水费按30%比例分摊' }
  ]},
  // 万科城2-305：合租，2间
  { id: 'rm_03_01', houseId: 'hs_03', roomNo: '主卧', area: 18, rent: 1800, deposit: 3600, status: 'rented', chargeCycle: '每月', feeItems: [
    { feeType: '租金', chargeType: 'fixed', chargeValue: 1800, chargeCycle: '每月', remark: '主卧月租收入' },
    { feeType: '押金', chargeType: 'fixed', chargeValue: 3600, chargeCycle: '', remark: '主卧押金' },
    { feeType: '电费', chargeType: 'ratio', chargeValue: 0.55, chargeCycle: '每月', remark: '合租电费按55%比例分摊' },
    { feeType: '物业费', chargeType: 'fixed', chargeValue: 150, chargeCycle: '每月', remark: '主卧月度物业费收入' }
  ]},
  { id: 'rm_03_02', houseId: 'hs_03', roomNo: '次卧', area: 13, rent: 1200, deposit: 2400, status: 'vacant', chargeCycle: '每月', feeItems: [
    { feeType: '租金', chargeType: 'fixed', chargeValue: 1200, chargeCycle: '每月', remark: '次卧月租收入' },
    { feeType: '押金', chargeType: 'fixed', chargeValue: 2400, chargeCycle: '', remark: '次卧押金' }
  ]},
  // 恒大华府1-401：整租，1间
  { id: 'rm_04_01', houseId: 'hs_04', roomNo: '整套', area: 106, rent: 4500, deposit: 9000, status: 'vacant', chargeCycle: '每月', feeItems: [
    { feeType: '租金', chargeType: 'fixed', chargeValue: 4500, chargeCycle: '每月', remark: '整套月租收入' },
    { feeType: '押金', chargeType: 'fixed', chargeValue: 9000, chargeCycle: '', remark: '整套押金' },
    { feeType: '水费', chargeType: 'fixed', chargeValue: 80, chargeCycle: '每月', remark: '月度水费包干收入' },
    { feeType: '物业费', chargeType: 'fixed', chargeValue: 320, chargeCycle: '每六月', remark: '半年物业费收入' },
    { feeType: '垃圾清运费', chargeType: 'fixed', chargeValue: 30, chargeCycle: '每年', remark: '年度固定垃圾清运费收入' }
  ]}
];

// ========== 租客会员 ==========
export const tenants = [
  { id: 'tn_01', name: '张三', maskName: '张**', phone: '13800001111', roomId: 'rm_01_01', contractId: 'ct_01', status: '正常', joinDate: '2025-01-01', emergencyContact: '张父', emergencyPhone: '13900001111' },
  { id: 'tn_02', name: '李四', maskName: '李**', phone: '13800002222', roomId: 'rm_02_01', contractId: 'ct_02', status: '正常', joinDate: '2025-03-15', emergencyContact: '李母', emergencyPhone: '13900002222' },
  { id: 'tn_03', name: '王五', maskName: '王**', phone: '13800003333', roomId: 'rm_02_02', contractId: 'ct_03', status: '欠费', joinDate: '2025-05-10', emergencyContact: '王姐', emergencyPhone: '13900003333' },
  { id: 'tn_04', name: '赵六', maskName: '赵**', phone: '13800004444', roomId: 'rm_03_01', contractId: 'ct_04', status: '正常', joinDate: '2024-09-20', emergencyContact: '赵哥', emergencyPhone: '13900004444' }
];

// ========== 合约 ==========
export const contracts = [
  { id: 'ct_01', tenantId: 'tn_01', roomId: 'rm_01_01', houseId: 'hs_01', startDate: '2025-01-01', endDate: '2026-12-31', status: 'active', monthRent: 3200, deposit: 6400, remark: '两年期整租合约，纸质合约已上传' },
  { id: 'ct_02', tenantId: 'tn_02', roomId: 'rm_02_01', houseId: 'hs_02', startDate: '2025-03-15', endDate: '2027-03-15', status: 'active', monthRent: 2000, deposit: 4000, remark: '主卧合租合约，季度结算水电杂费' },
  { id: 'ct_03', tenantId: 'tn_03', roomId: 'rm_02_02', houseId: 'hs_02', startDate: '2025-07-10', endDate: '2026-07-10', status: 'active', monthRent: 1500, deposit: 3000, remark: '次卧A合租合约，租客存在缴费滞后情况' },
  { id: 'ct_04', tenantId: 'tn_04', roomId: 'rm_03_01', houseId: 'hs_03', startDate: '2024-09-20', endDate: '2026-09-20', status: 'active', monthRent: 1800, deposit: 3600, remark: '万科城主卧合租合约，租客缴费正常' }
];

// ========== 收入账单 ==========
// billType 对应 INCOME_FEE_TYPES 的 id
export const incomeBills = [
  // 张*（阳光小区3-201）
  { id: 'ib_01', tenantId: 'tn_01', roomId: 'rm_01_01', houseId: 'hs_01', contractId: 'ct_01', billType: 'rent', billName: '租金', amount: 3200, status: 'paid', periodStart: '2026-06-01', periodEnd: '2026-06-30', dueDate: '2026-06-15', paidDate: '2026-06-15', createTime: '2026-06-01' },
  { id: 'ib_02', tenantId: 'tn_01', roomId: 'rm_01_01', houseId: 'hs_01', contractId: 'ct_01', billType: 'deposit', billName: '押金', amount: 6400, status: 'deposit_paid', periodStart: '', periodEnd: '', dueDate: '2025-01-01', paidDate: '2025-01-01', createTime: '2025-01-01' },
  { id: 'ib_03', tenantId: 'tn_01', roomId: 'rm_01_01', houseId: 'hs_01', contractId: 'ct_01', billType: 'water', billName: '水费', amount: 50, status: 'paid', periodStart: '2026-06-01', periodEnd: '2026-06-30', dueDate: '2026-06-15', paidDate: '2026-06-15', createTime: '2026-06-01' },
  { id: 'ib_04', tenantId: 'tn_01', roomId: 'rm_01_01', houseId: 'hs_01', contractId: 'ct_01', billType: 'electric', billName: '电费', amount: 80, status: 'paid', periodStart: '2026-06-01', periodEnd: '2026-06-30', dueDate: '2026-06-15', paidDate: '2026-06-15', createTime: '2026-06-01' },
  // 李*（碧桂园5-102主卧）
  { id: 'ib_05', tenantId: 'tn_02', roomId: 'rm_02_01', houseId: 'hs_02', contractId: 'ct_02', billType: 'rent', billName: '租金', amount: 2000, status: 'paid', periodStart: '2026-06-01', periodEnd: '2026-06-30', dueDate: '2026-06-15', paidDate: '2026-06-15', createTime: '2026-06-01' },
  { id: 'ib_06', tenantId: 'tn_02', roomId: 'rm_02_01', houseId: 'hs_02', contractId: 'ct_02', billType: 'deposit', billName: '押金', amount: 4000, status: 'deposit_paid', periodStart: '', periodEnd: '', dueDate: '2025-03-15', paidDate: '2025-03-15', createTime: '2025-03-15' },
  { id: 'ib_07', tenantId: 'tn_02', roomId: 'rm_02_01', houseId: 'hs_02', contractId: 'ct_02', billType: 'water', billName: '水费', amount: 60, status: 'paid', periodStart: '2026-06-01', periodEnd: '2026-06-30', dueDate: '2026-06-15', paidDate: '2026-06-15', createTime: '2026-06-01' },
  // 王*（碧桂园5-102次卧A）
  { id: 'ib_08', tenantId: 'tn_03', roomId: 'rm_02_02', houseId: 'hs_02', contractId: 'ct_03', billType: 'rent', billName: '租金', amount: 1500, status: 'overdue', periodStart: '2026-06-01', periodEnd: '2026-06-30', dueDate: '2026-06-13', paidDate: '', createTime: '2026-06-01' },
  { id: 'ib_09', tenantId: 'tn_03', roomId: 'rm_02_02', houseId: 'hs_02', contractId: 'ct_03', billType: 'deposit', billName: '押金', amount: 3000, status: 'deposit_paid', periodStart: '', periodEnd: '', dueDate: '2025-07-10', paidDate: '2025-07-10', createTime: '2025-07-10' },
  // 赵*（万科城2-305主卧）
  { id: 'ib_10', tenantId: 'tn_04', roomId: 'rm_03_01', houseId: 'hs_03', contractId: 'ct_04', billType: 'rent', billName: '租金', amount: 1800, status: 'pending', periodStart: '2026-06-01', periodEnd: '2026-06-30', dueDate: '2026-06-20', paidDate: '', createTime: '2026-06-01' },
  { id: 'ib_11', tenantId: 'tn_04', roomId: 'rm_03_01', houseId: 'hs_03', contractId: 'ct_04', billType: 'deposit', billName: '押金', amount: 3600, status: 'deposit_paid', periodStart: '', periodEnd: '', dueDate: '2024-09-20', paidDate: '2024-09-20', createTime: '2024-09-20' },
  // 更多月度流水
  { id: 'ib_12', tenantId: 'tn_01', roomId: 'rm_01_01', houseId: 'hs_01', contractId: 'ct_01', billType: 'rent', billName: '租金', amount: 3200, status: 'paid', periodStart: '2026-05-01', periodEnd: '2026-05-31', dueDate: '2026-05-15', paidDate: '2026-05-15', createTime: '2026-05-01' },
  { id: 'ib_13', tenantId: 'tn_02', roomId: 'rm_02_01', houseId: 'hs_02', contractId: 'ct_02', billType: 'rent', billName: '租金', amount: 2000, status: 'paid', periodStart: '2026-05-01', periodEnd: '2026-05-31', dueDate: '2026-05-15', paidDate: '2026-05-15', createTime: '2026-05-01' },
  { id: 'ib_14', tenantId: 'tn_04', roomId: 'rm_03_01', houseId: 'hs_03', contractId: 'ct_04', billType: 'property', billName: '物业费', amount: 150, status: 'paid', periodStart: '2026-06-01', periodEnd: '2026-11-30', dueDate: '2026-06-15', paidDate: '2026-06-15', createTime: '2026-06-01' }
];

// ========== 支出账单（全新体系，关联合约但只关房源） ==========
export const expenseBills = [
  { id: 'eb_01', houseId: 'hs_01', expenseType: 'house_rent', expenseName: '房源租金', amount: 2500, costDate: '2026-06-01', remark: '6月份整屋承租租金', createTime: '2026-06-01' },
  { id: 'eb_02', houseId: 'hs_02', expenseType: 'house_rent', expenseName: '房源租金', amount: 3800, costDate: '2026-06-01', remark: '6月份整屋承租租金', createTime: '2026-06-01' },
  { id: 'eb_03', houseId: 'hs_02', expenseType: 'water', expenseName: '水费支出', amount: 180, costDate: '2026-06-05', remark: '碧桂园6月整屋水费总支出', createTime: '2026-06-05' },
  { id: 'eb_04', houseId: 'hs_02', expenseType: 'electric', expenseName: '电费支出', amount: 320, costDate: '2026-06-08', remark: '碧桂园6月整屋电费总支出', createTime: '2026-06-08' },
  { id: 'eb_05', houseId: 'hs_02', expenseType: 'property', expenseName: '物业费支出', amount: 480, costDate: '2026-06-10', remark: '碧桂园半年物业费支出', createTime: '2026-06-10' },
  { id: 'eb_06', houseId: 'hs_03', expenseType: 'house_rent', expenseName: '房源租金', amount: 2800, costDate: '2026-06-01', remark: '万科城6月整屋承租租金', createTime: '2026-06-01' },
  { id: 'eb_07', houseId: 'hs_03', expenseType: 'repair', expenseName: '房屋维修', amount: 350, costDate: '2026-05-20', remark: '厨房水龙头更换维修支出', createTime: '2026-05-20' },
  { id: 'eb_08', houseId: 'hs_04', expenseType: 'house_rent', expenseName: '房源租金', amount: 3500, costDate: '2026-06-01', remark: '恒大华府6月整屋承租租金', createTime: '2026-06-01' },
  { id: 'eb_09', houseId: 'hs_04', expenseType: 'property', expenseName: '物业费支出', amount: 640, costDate: '2026-06-15', remark: '恒大华府年度物业费支出', createTime: '2026-06-15' },
  { id: 'eb_10', houseId: 'hs_01', expenseType: 'house_deposit', expenseName: '房源押金支出', amount: 5000, costDate: '2024-01-01', remark: '签约时向大房东支付押金', createTime: '2024-01-01' }
];

// ========== 消息中心 ==========
export const messages = [
  { id: 'msg_01', type: 'bill', title: '租金账单逾期提醒', content: '租客王五（碧桂园5-102 · 次卧A）6月租金 ¥1,500 已逾期，请及时催收', time: '2026-06-14 09:00', read: false },
  { id: 'msg_02', type: 'contract', title: '合约到期提醒', content: '合约 ct_04（阳光小区3-201）将于 29 天后到期，请提前与租客沟通续约事宜', time: '2026-06-13 10:30', read: false },
  { id: 'msg_03', type: 'bill', title: '租金账单到账通知', content: '租客张三（阳光小区3-201）已缴纳 2026-06 月租金 ¥3,200，可在账单详情查看', time: '2026-06-12 14:15', read: true },
  { id: 'msg_04', type: 'bill', title: '下月账单已生成', content: '租客李四（碧桂园5-102 · 主卧）2026-07 月租金账单 ¥2,000 已生成', time: '2026-06-28 08:00', read: true },
  { id: 'msg_05', type: 'contract', title: '合约生效通知', content: '新租客张三已成功入住阳光小区3-201，合约已锁定，计费规则不可编辑', time: '2025-01-01 00:00', read: true },
  { id: 'msg_06', type: 'system', title: '数据安全提醒', content: '系统已启用敏感字段全程加密传输，所有租客隐私信息前台自动脱敏展示', time: '2026-04-01 12:00', read: true }
];

// ========== 派生统计数据 ==========
// 房间概况统计
export const getRoomStats = () => {
  const total = rooms.length;
  const rented = rooms.filter(r => r.status === 'rented').length;
  const vacant = rooms.filter(r => r.status === 'vacant').length;
  const expiring = contracts.filter(c => c.status === 'active' && diffDays(c.endDate) <= 30 && diffDays(c.endDate) > 0).length;
  return { total, rented, vacant, expiring };
};

// 账单数据（收入侧）
export const getBillingSummary = () => {
  const pendingBills = incomeBills.filter(b => b.status === 'pending');
  const overdueBills = incomeBills.filter(b => b.status === 'overdue');
  const depositTotal = incomeBills
    .filter(b => b.status === 'deposit_paid')
    .reduce((sum, b) => sum + b.amount, 0);
  return {
    pendingCount: pendingBills.length,
    pendingAmount: pendingBills.reduce((s, b) => s + b.amount, 0),
    overdueCount: overdueBills.length,
    overdueAmount: overdueBills.reduce((s, b) => s + b.amount, 0),
    depositTotal
  };
};

// 经营盈利统计（按时间段）
export const getProfitStats = (period) => {
  // period: month | quarter | year | all
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const startOf = (d) => d.toISOString().slice(0, 10);

  let incomeStart, expenseStart;
  if (period === 'month') {
    incomeStart = `${y}-${String(m + 1).padStart(2, '0')}-01`;
  } else if (period === 'quarter') {
    const qm = (Math.floor(m / 3) * 3);
    incomeStart = `${y}-${String(qm + 1).padStart(2, '0')}-01`;
  } else if (period === 'year') {
    incomeStart = `${y}-01-01`;
  } else {
    incomeStart = '2000-01-01';
  }

  // 收入：账单周期开始在范围之后，且不是作废状态
  // PRD 6.2 口径：包含待支付、已支付、逾期未付所有有效账单状态；仅作废账单不参与
  const totalIncome = incomeBills
    .filter(b => b.periodStart && b.periodStart >= incomeStart && b.status !== 'void')
    .reduce((s, b) => s + b.amount, 0);

  // 支出：仅按发生日期统计
  const totalExpense = expenseBills
    .filter(b => b.costDate >= incomeStart)
    .reduce((s, b) => s + b.amount, 0);

  const netProfit = totalIncome - totalExpense;
  const rate = totalIncome > 0 ? ((netProfit / totalIncome) * 100).toFixed(1) : '0.0';
  return { totalIncome, totalExpense, netProfit, rate };
};

// 最新提醒列表（首页使用）
export const getLatestReminders = () => {
  const list = [];
  // 逾期
  const overdueTns = tenants.filter(t => t.status === '欠费');
  overdueTns.forEach(tn => {
    const house = houseSources.find(h => h.id === (rooms.find(r => r.id === tn.roomId)?.houseId));
    list.push({
      id: 'rem-over-' + tn.id,
      type: 'overdue',
      title: `${tn.maskName}（${house?.name} ${rooms.find(r=>r.id===tn.roomId).roomNo}）租金逾期 ${diffDays('2026-06-13')} 天未缴`,
      date: '今天'
    });
  });
  // 即将到期（30天内）
  contracts
    .filter(c => c.status === 'active')
    .forEach(c => {
      const remain = diffDays(c.endDate);
      if (remain > 0 && remain <= 30) {
        const house = houseSources.find(h => h.id === c.houseId);
        list.push({
          id: 'rem-exp-' + c.id,
          type: 'expiring',
          title: `${house?.name} 合约将于 ${remain} 天后到期`,
          date: '今天'
        });
      }
    });
  // 已缴账单
  list.push({ id: 'rem-paid-01', type: 'paid', title: `${tenants[0].maskName}（${houseSources[0].name}）已缴本月租金 ¥3,200`, date: '昨天' });
  return list;
};

// ========== 辅助：按房源聚合房间/合约 ==========
export const getRoomsByHouse = (houseId) => rooms.filter(r => r.houseId === houseId);
export const getTenantByRoom = (roomId) => tenants.find(t => t.roomId === roomId);
export const getContractByRoom = (roomId) => contracts.find(c => c.roomId === roomId);
export const getHouseById = (id) => houseSources.find(h => h.id === id);
export const getRoomById = (id) => rooms.find(r => r.id === id);
export const getTenantById = (id) => tenants.find(t => t.id === id);
export const getContractById = (id) => contracts.find(c => c.id === id);
export const getIncomeBillsByRoom = (roomId) => incomeBills.filter(b => b.roomId === roomId);
export const getIncomeBillsByHouse = (houseId) => incomeBills.filter(b => b.houseId === houseId);
export const getExpenseBillsByHouse = (houseId) => expenseBills.filter(b => b.houseId === houseId);
