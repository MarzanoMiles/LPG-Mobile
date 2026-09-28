const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { warehouseId } = req.query;
  const clauses = [];
  const params = [];
  if (warehouseId) { clauses.push('i.WarehouseID = ?'); params.push(warehouseId); }
  const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';

  const [rows] = await pool.query(
    `SELECT i.*, p.ProductName, p.Unit, p.ReorderLevel, p.UnitPrice, w.WarehouseName
     FROM inventory i
     JOIN product p ON p.ProductID = i.ProductID
     JOIN warehouse w ON w.WarehouseID = i.WarehouseID
     ${where}
     ORDER BY p.ProductName`,
    params
  );
  res.json(rows);
}));

router.get('/low-stock', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT i.*, p.ProductName, p.ReorderLevel, w.WarehouseName
     FROM inventory i
     JOIN product p ON p.ProductID = i.ProductID
     JOIN warehouse w ON w.WarehouseID = i.WarehouseID
     WHERE i.StockOnHand <= p.ReorderLevel`
  );
  res.json(rows);
}));

router.post('/transactions', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { warehouseId, productId, transactionType, quantity, reason, referenceNo, remarks } = req.body;
  if (!['Stock In', 'Stock Out'].includes(transactionType)) {
    return res.status(400).json({ message: 'transactionType must be "Stock In" or "Stock Out"' });
  }

  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();

    const [invRows] = await conn.query(
      `SELECT * FROM inventory WHERE WarehouseID = ? AND ProductID = ? FOR UPDATE`,
      [warehouseId, productId]
    );
    let inventory = invRows[0];
    if (!inventory) {
      const [ins] = await conn.query(
        `INSERT INTO inventory (WarehouseID, ProductID, StockOnHand) VALUES (?, ?, 0)`,
        [warehouseId, productId]
      );
      inventory = { InventoryID: ins.insertId, StockOnHand: 0 };
    }

    const delta = transactionType === 'Stock In' ? quantity : -quantity;
    const newStock = inventory.StockOnHand + delta;
    if (newStock < 0) throw Object.assign(new Error('Insufficient stock'), { status: 400 });

    await conn.query(`UPDATE inventory SET StockOnHand = ? WHERE InventoryID = ?`, [newStock, inventory.InventoryID]);
    await conn.query(
      `INSERT INTO inventorytransaction (InventoryID, UserID, TransactionType, Quantity, Reason, ReferenceNo, Remarks)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [inventory.InventoryID, req.user.sub, transactionType, quantity, reason || 'Adjustment', referenceNo || null, remarks || null]
    );

    await conn.commit();
    res.status(201).json({ message: 'Recorded', stockOnHand: newStock });
  } catch (err) {
    await conn.rollback();
    throw err;
  } finally {
    conn.release();
  }
}));

router.get('/transactions', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT it.*, p.ProductName, w.WarehouseName
     FROM inventorytransaction it
     JOIN inventory i ON i.InventoryID = it.InventoryID
     JOIN product p ON p.ProductID = i.ProductID
     JOIN warehouse w ON w.WarehouseID = i.WarehouseID
     ORDER BY it.TransactionDate DESC LIMIT 200`
  );
  res.json(rows);
}));

module.exports = router;