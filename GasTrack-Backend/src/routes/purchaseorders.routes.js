const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT po.*, s.SupplierName FROM purchaseorder po JOIN supplier s ON s.SupplierID = po.SupplierID ORDER BY po.OrderDate DESC`
  );
  res.json(rows);
}));

router.get('/:id', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [[po]] = await pool.query(`SELECT * FROM purchaseorder WHERE PurchaseOrderID = ?`, [req.params.id]);
  if (!po) return res.status(404).json({ message: 'Purchase order not found' });
  const [items] = await pool.query(
    `SELECT poi.*, p.ProductName FROM purchaseorderitem poi JOIN product p ON p.ProductID = poi.ProductID WHERE poi.PurchaseOrderID = ?`,
    [req.params.id]
  );
  res.json({ ...po, items });
}));

// Approve a restock recommendation into a PO — matches Restocking(Employee).kt "Approve & Confirm Order"
router.post('/from-restock/:restockId', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();

    const [[restock]] = await conn.query(`SELECT * FROM restockrecommendation WHERE RestockID = ?`, [req.params.restockId]);
    if (!restock) throw Object.assign(new Error('Recommendation not found'), { status: 404 });

    const [[product]] = await conn.query(`SELECT * FROM product WHERE ProductID = ?`, [restock.ProductID]);
    const unitCost = product.CostPrice;
    const totalAmount = unitCost * restock.RecommendedQuantity;
    const poNo = `PO-${new Date().getFullYear()}-${Date.now().toString().slice(-6)}`;

    const [poResult] = await conn.query(
      `INSERT INTO purchaseorder (SupplierID, RestockID, CreatedByUserID, PONo, OrderDate, Status, TotalAmount)
       VALUES (?, ?, ?, ?, NOW(), 'Pending', ?)`,
      [restock.SupplierID, restock.RestockID, req.user.sub, poNo, totalAmount]
    );

    await conn.query(
      `INSERT INTO purchaseorderitem (PurchaseOrderID, ProductID, Quantity, UnitCost, Subtotal) VALUES (?, ?, ?, ?, ?)`,
      [poResult.insertId, restock.ProductID, restock.RecommendedQuantity, unitCost, totalAmount]
    );

    await conn.query(`UPDATE restockrecommendation SET Status = 'Converted' WHERE RestockID = ?`, [restock.RestockID]);

    await conn.commit();
    res.status(201).json({ purchaseOrderId: poResult.insertId, poNo });
  } catch (err) {
    await conn.rollback();
    throw err;
  } finally {
    conn.release();
  }
}));

router.put('/:id/status', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { status } = req.body; // Pending | Approved | Cancelled
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();
    await conn.query(`UPDATE purchaseorder SET Status = ? WHERE PurchaseOrderID = ?`, [status, req.params.id]);

    if (status === 'Approved') {
      const [items] = await conn.query(`SELECT * FROM purchaseorderitem WHERE PurchaseOrderID = ?`, [req.params.id]);
      const [[po]] = await conn.query(`SELECT * FROM purchaseorder WHERE PurchaseOrderID = ?`, [req.params.id]);
      for (const item of items) {
        const [[inv]] = await conn.query(
          `SELECT * FROM inventory WHERE ProductID = ? LIMIT 1 FOR UPDATE`,
          [item.ProductID]
        );
        if (inv) {
          await conn.query(`UPDATE inventory SET StockOnHand = StockOnHand + ? WHERE InventoryID = ?`, [item.Quantity, inv.InventoryID]);
          await conn.query(
            `INSERT INTO inventorytransaction (InventoryID, UserID, TransactionType, Quantity, Reason, ReferenceNo)
             VALUES (?, ?, 'Stock In', ?, 'Adjustment', ?)`,
            [inv.InventoryID, req.user.sub, item.Quantity, po.PONo]
          );
        }
      }
    }

    await conn.commit();
    res.json({ message: 'Updated' });
  } catch (err) {
    await conn.rollback();
    throw err;
  } finally {
    conn.release();
  }
}));

module.exports = router;