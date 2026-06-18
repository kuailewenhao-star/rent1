// 本地数据管理 Context — 替代模块级可变状态
import { createContext, useContext, useState, useCallback } from 'react';
import {
  houseSources, rooms, tenants, incomeBills, expenseBills, contracts, todayISO
} from '../data/mock.js';

const DataContext = createContext(null);

export function DataProvider({ children }) {
  const [houses, setHouses] = useState([...houseSources]);
  const [roomsList, setRooms] = useState([...rooms]);
  const [tenantsList, setTenants] = useState([...tenants]);
  const [incomeBillsList, setIncomeBills] = useState([...incomeBills]);
  const [expenseBillsList, setExpenseBills] = useState([...expenseBills]);
  const [contractsList, setContracts] = useState([...contracts]);

  // 房源操作
  const deleteHouse = useCallback((houseId) => {
    setHouses(prev => prev.filter(h => h.id !== houseId));
    setRooms(prev => prev.filter(r => r.houseId !== houseId));
  }, []);

  const addHouse = useCallback((house) => {
    const newHouse = { ...house, id: 'hs_' + Date.now() };
    setHouses(prev => [...prev, newHouse]);
    return newHouse;
  }, []);

  const updateHouse = useCallback((houseId, data) => {
    setHouses(prev => prev.map(h => h.id === houseId ? { ...h, ...data } : h));
  }, []);

  // 房间操作
  const deleteRoom = useCallback((roomId) => {
    setRooms(prev => prev.filter(r => r.id !== roomId));
  }, []);

  const addRoom = useCallback((room) => {
    const newRoom = { ...room, id: 'rm_' + Date.now() };
    setRooms(prev => [...prev, newRoom]);
    return newRoom;
  }, []);

  const updateRoom = useCallback((roomId, data) => {
    setRooms(prev => prev.map(r => r.id === roomId ? { ...r, ...data } : r));
  }, []);

  const deleteTenant = useCallback((tenantId) => {
    setTenants(prev => prev.filter(t => t.id !== tenantId));
  }, []);

  // 租客操作（含通行证/主体信息）
  const addTenant = useCallback((tenant) => {
    const newTenant = {
      ...tenant,
      id: 'tn_' + Date.now(),
      maskName: tenant.name && tenant.name.length > 0
        ? (tenant.name.length === 1 ? tenant.name : tenant.name[0] + '*'.repeat(Math.min(tenant.name.length - 1, 2)))
        : '',
    };
    setTenants(prev => [...prev, newTenant]);
    return newTenant;
  }, []);

  const updateTenant = useCallback((tenantId, data) => {
    setTenants(prev => prev.map(t => t.id === tenantId ? { ...t, ...data } : t));
  }, []);

  // 合约操作
  const addContract = useCallback((contract) => {
    const newContract = { ...contract, id: 'ct_' + Date.now() };
    setContracts(prev => [...prev, newContract]);
    return newContract;
  }, []);

  // 账单操作（新增）
  const addIncomeBill = useCallback((bill) => {
    const newBill = { ...bill, id: 'ib_' + Date.now() + '_' + Math.floor(Math.random() * 1000) };
    setIncomeBills(prev => [...prev, newBill]);
    return newBill;
  }, []);

  // 根据合约生成租金账单（按周期）
  const generateRentBills = useCallback((contract, tenant) => {
    const bills = [];
    const start = new Date(contract.startDate);
    const end = new Date(contract.endDate);
    const cycle = contract.chargeCycle || 'monthly';
    const cycleMonths = { 'monthly': 1, 'quarterly': 3, 'half-yearly': 6, 'yearly': 12 }[cycle] || 1;

    // 押金账单（一次性）
    if (contract.deposit > 0) {
      bills.push({
        tenantId: tenant.id,
        roomId: contract.roomId,
        houseId: contract.houseId,
        contractId: contract.id,
        billType: 'deposit',
        billName: '押金',
        amount: contract.deposit,
        status: 'deposit_paid',
        periodStart: '',
        periodEnd: '',
        dueDate: contract.startDate,
        paidDate: contract.startDate,
        createTime: contract.startDate,
        remark: '租客租房履约保证金，一次性收入'
      });
    }

    // 租金账单（按周期生成）
    let cur = new Date(start);
    let idx = 0;
    while (cur < end) {
      const periodStart = new Date(cur);
      const periodEnd = new Date(cur);
      periodEnd.setMonth(periodEnd.getMonth() + cycleMonths);
      periodEnd.setDate(periodEnd.getDate() - 1);
      const realEnd = periodEnd > end ? end : periodEnd;

      // 到期日期 = periodStart + 15 天
      const dueDate = new Date(periodStart);
      dueDate.setDate(dueDate.getDate() + 15);

      const fmt = (d) => d.toISOString().slice(0, 10);
      bills.push({
        tenantId: tenant.id,
        roomId: contract.roomId,
        houseId: contract.houseId,
        contractId: contract.id,
        billType: 'rent',
        billName: '租金',
        amount: contract.monthRent,
        status: 'pending',
        periodStart: fmt(periodStart),
        periodEnd: fmt(realEnd),
        dueDate: fmt(dueDate),
        paidDate: '',
        createTime: contract.startDate,
        remark: `${cycleMonths === 1 ? '月' : cycleMonths === 3 ? '季' : cycleMonths === 6 ? '半年' : '年'}度租金收入`
      });
      cur = new Date(periodEnd);
      cur.setDate(cur.getDate() + 1);
      idx++;
      if (idx > 240) break; // 安全上限
    }

    bills.forEach(b => {
      const newBill = { ...b, id: 'ib_' + Date.now() + '_' + Math.floor(Math.random() * 10000) + idx };
      setIncomeBills(prev => [...prev, newBill]);
    });
    return bills;
  }, []);

  // 退租：将扣留押金转为收入，并结算合约
  // 退租时需要正确释放房间（通过合约找到房间）
  const moveOutByRoom = useCallback((contractId, refundAmount, deductionAmount, remark) => {
    const contract = contractsList.find(c => c.id === contractId);
    if (!contract) return;
    const tenant = tenantsList.find(t => t.roomId === contract.roomId);
    if (tenant) {
      setTenants(prev => prev.map(t => t.id === tenant.id ? { ...t, status: '已退租', moveOutDate: todayISO() } : t));
    }
    setContracts(prev => prev.map(c => c.id === contractId ? { ...c, status: 'ended' } : c));
    setRooms(prev => prev.map(r => r.id === contract.roomId ? { ...r, status: 'vacant' } : r));
    if (deductionAmount > 0) {
      const newBill = {
        id: 'ib_' + Date.now(),
        tenantId: tenant?.id,
        roomId: contract.roomId,
        houseId: contract.houseId,
        contractId,
        billType: 'rent',
        billName: '扣留押金收入',
        amount: deductionAmount,
        status: 'paid',
        periodStart: '',
        periodEnd: '',
        dueDate: todayISO(),
        paidDate: todayISO(),
        createTime: todayISO(),
        remark: remark || '退租扣留押金'
      };
      setIncomeBills(prev => [...prev, newBill]);
    }
  }, [contractsList, tenantsList]);

  // 账单操作
  const markBillPaid = useCallback((billId, paidDate) => {
    setIncomeBills(prev => prev.map(b =>
      b.id === billId ? { ...b, status: 'paid', paidDate } : b
    ));
  }, []);

  const deleteIncomeBill = useCallback((billId) => {
    setIncomeBills(prev => prev.filter(b => b.id !== billId));
  }, []);

  const deleteExpenseBill = useCallback((billId) => {
    setExpenseBills(prev => prev.filter(b => b.id !== billId));
  }, []);

  // 合约操作
  const updateContractStatus = useCallback((contractId, status) => {
    setContracts(prev => prev.map(c =>
      c.id === contractId ? { ...c, status } : c
    ));
  }, []);

  const deleteContract = useCallback((contractId) => {
    setContracts(prev => prev.filter(c => c.id !== contractId));
  }, []);

  const value = {
    houses, rooms: roomsList, tenants: tenantsList,
    incomeBills: incomeBillsList, expenseBills: expenseBillsList,
    contracts: contractsList,
    deleteHouse, addHouse, updateHouse,
    deleteRoom, addRoom, updateRoom,
    deleteTenant, addTenant, updateTenant,
    addContract, addIncomeBill, generateRentBills, moveOutByRoom,
    markBillPaid, deleteIncomeBill, deleteExpenseBill,
    updateContractStatus, deleteContract,
  };

  return <DataContext.Provider value={value}>{children}</DataContext.Provider>;
}

export function useData() {
  const ctx = useContext(DataContext);
  if (!ctx) throw new Error('useData must be used within DataProvider');
  return ctx;
}
