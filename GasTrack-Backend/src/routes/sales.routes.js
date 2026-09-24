const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate } = require('../middleware/auth');

const router = express.Router();

function genNo(prefix) {
  const year = new Date().getFullYear();
  const rand = Math.floor(Math.random() * 900 + 100);
  return `${prefix}-${year}-${Date.now().toString().slice(-6)}${rand}`;
}

// Full checkout: creates order + orderdetails + sale + payment, deducts stock.
// Used by both the customer OrderTab/Checkout flow and employee POS.
router.post('/checkout', authenticate, asyncHandler(async (req, res) => {
  const {
    customerId, warehouseId, orderType, items, // items: [{ productId, quantity }]
    paymentMethod, amountPaid, salesDiscount, deliveryAddress, deliveryCharge,
  } = req.body;

  if (!customerId || !warehouseId || !Array.isArray(items) || items.length === 0) {
    return res.status(400).json({ message: 'customerId, warehouseId and items are required' });
  }

  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();

    // Price + stock check
    let subtotal = 0;
    const priced = [];
    for (const it of items) {
      const [[product]] = await conn.query(`SELECT * FROM product WHERE ProductID = ?`, [it.productId]);
      if (!product) throw Object.assign(new Error(`Product ${it.productId} not found`), { status: 404 });

      const [[inv]] = await conn.query(
        `SELECT * FROM inventory WHERE WarehouseID = ? AND ProductID = ? FOR UPDATE`,
        [warehouseId, it.productId]
      );
      if (!inv || inv.StockOnHand < it.quantity) {
        throw Object.assign(new Error(`Insufficient stock for ${product.ProductName}`), { status: 400 });
      }
      const lineSubtotal = Number(product.UnitPrice) * it.quantity;
      subtotal += lineSubtotal;
      priced.push({ ...it, unitPrice: product.UnitPrice, subtotal: lineSubtotal, inventoryId: inv.InventoryID });
    }

    const discount = salesDiscount || 0;
    const total = subtotal - discount + (deliveryCharge || 0);
    const orderNo = genNo('ORD');
    const saleNo = genNo('SALE');

    const [orderResult] = await conn.query(
      `INSERT INTO \`order\` (CustomerID, OrderNo, OrderType, OrderStatus, TotalAmount) VALUES (?, ?, ?, 'Completed', ?)`,
      [customerId, orderNo, orderType || 'Walk-in', total]
    );
    const orderId = orderResult.insertId;

    for (const it of priced) {
      await conn.query(
        `INSERT INTO orderdetails (OrderID, ProductID, Quantity, UnitPrice, Subtotal) VALUES (?, ?, ?, ?, ?)`,
        [orderId, it.productId, it.quantity, it.unitPrice, it.subtotal]
      );
      const [[inv]] = await conn.query(`SELECT StockOnHand FROM inventory WHERE InventoryID = ?`, [it.inventoryId]);
      await conn.query(`UPDATE inventory SET StockOnHand = ? WHERE InventoryID = ?`, [inv.StockOnHand - it.quantity, it.inventoryId]);
      await conn.query(
        `INSERT INTO inventorytransaction (InventoryID, UserID, TransactionType, Quantity, Reason, ReferenceNo)
         VALUES (?, ?, 'Stock Out', ?, 'Sale', ?)`,
        [it.inventoryId, req.user.type === 'employee' ? req.user.sub : 1, it.quantity, orderNo]
      );
    }

    const [saleResult] = await conn.query(
      `INSERT INTO sales (OrderID, CustomerID, UserID, SaleNo, SalesDiscount, TotalAmount)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [orderId, customerId, req.user.type === 'employee' ? req.user.sub : 1, saleNo, discount, total]
    );
    const saleId = saleResult.insertId;

    await conn.query(
      `INSERT INTO payment (PaymentType, SaleID, PaymentMethod, AmountPaid) VALUES ('Sale', ?, ?, ?)`,
      [saleId, paymentMethod || 'Cash', amountPaid ?? total]
    );

    if (orderType === 'Delivery' && deliveryAddress) {
      await conn.query(
        `INSERT INTO delivery (SaleID, DRNo, DeliveryCharge, DeliveryAddress, DeliveryStatus)
         VALUES (?, ?, ?, ?, 'Pending')`,
        [saleId, genNo('DR'), deliveryCharge || 0, deliveryAddress]
      );
    }

    await conn.commit();
    res.status(201).json({ orderId, orderNo, saleId, saleNo, totalAmount: total });
  } catch (err) {
    await conn.rollback();
    throw err;
  } finally {
    conn.release();
  }
}));

// Void a sale — restores stock, matches the "VOID-SALE-<id>" pattern in your data
router.post('/:saleId/void', authenticate, asyncHandler(async (req, res) => {
  const saleId = req.params.saleId;
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();

    const [[sale]] = await conn.query(`SELECT * FROM sales WHERE SaleID = ?`, [saleId]);
    if (!sale) throw Object.assign(new Error('Sale not found'), { status: 404 });

    const [items] = await conn.query(`SELECT * FROM orderdetails WHERE OrderID = ?`, [sale.OrderID]);
    for (const item of items) {
      const [[inv]] = await conn.query(
        `SELECT * FROM inventory WHERE ProductID = ? LIMIT 1 FOR UPDATE`,
        [item.ProductID]
      );
      if (inv) {
        await conn.query(`UPDATE inventory SET StockOnHand = StockOnHand + ? WHERE InventoryID = ?`, [item.Quantity, inv.InventoryID]);
        await conn.query(
          `INSERT INTO inventorytransaction (InventoryID, UserID, TransactionType, Quantity, Reason, ReferenceNo, Remarks)
           VALUES (?, ?, 'Stock In', ?, 'Adjustment', ?, 'Sale voided — stock restored')`,
          [inv.InventoryID, req.user.sub, item.Quantity, `VOID-SALE-${saleId}`]
        );
      }
    }

    await conn.query(`UPDATE \`order\` SET OrderStatus = 'Cancelled' WHERE OrderID = ?`, [sale.OrderID]);
    await conn.query(`DELETE FROM payment WHERE SaleID = ?`, [saleId]);
    await conn.query(`DELETE FROM sales WHERE SaleID = ?`, [saleId]);

    await conn.commit();
    res.json({ message: 'Sale voided, stock restored' });
  } catch (err) {
    await conn.rollback();
    throw err;
  } finally {
    conn.release();
  }
}));

router.get('/', authenticate, asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT s.*, c.CustomerName, u.FirstName, u.LastName
     FROM sales s
     JOIN customer c ON c.CustomerID = s.CustomerID
     JOIN user u ON u.UserID = s.UserID
     ORDER BY s.SaleDate DESC LIMIT 200`
  );
  res.json(rows);
}));

router.get('/:id', authenticate, asyncHandler(async (req, res) => {
  const [[sale]] = await pool.query(`SELECT * FROM sales WHERE SaleID = ?`, [req.params.id]);
  if (!sale) return res.status(404).json({ message: 'Sale not found' });
  const [items] = await pool.query(
    `SELECT od.*, p.ProductName FROM orderdetails od JOIN product p ON p.ProductID = od.ProductID WHERE od.OrderID = ?`,
    [sale.OrderID]
  );
  const [payments] = await pool.query(`SELECT * FROM payment WHERE SaleID = ?`, [req.params.id]);
  res.json({ ...sale, items, payments });
}));

module.exports = router;