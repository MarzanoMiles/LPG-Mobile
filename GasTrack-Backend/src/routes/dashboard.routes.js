const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

// Powers Dashboard(Employee).kt — today's orders, delivered count, low stock alert
router.get('/employee', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [[todaysOrders]] = await pool.query(
    `SELECT COUNT(*) AS count FROM \`order\` WHERE DATE(OrderDate) = CURDATE()`
  );
  const [[delivered]] = await pool.query(
    `SELECT COUNT(*) AS count FROM delivery WHERE DeliveryStatus = 'Delivered' AND DATE(DeliveryDate) = CURDATE()`
  );
  const [lowStock] = await pool.query(
    `SELECT i.*, p.ProductName, p.ReorderLevel
     FROM inventory i JOIN product p ON p.ProductID = i.ProductID
     WHERE i.StockOnHand <= p.ReorderLevel
     ORDER BY i.StockOnHand ASC LIMIT 5`
  );
  const [[todaysSales]] = await pool.query(
    `SELECT COALESCE(SUM(TotalAmount), 0) AS total FROM sales WHERE DATE(SaleDate) = CURDATE()`
  );

  res.json({
    todaysOrders: todaysOrders.count,
    delivered: delivered.count,
    todaysSalesTotal: todaysSales.total,
    lowStock,
  });
}));

// Powers Home(Customer).kt "Smart Refill" card — days since last order
router.get('/customer', authenticate, asyncHandler(async (req, res) => {
  if (req.user.type !== 'customer') return res.status(403).json({ message: 'Customer access only' });
  const [[lastOrder]] = await pool.query(
    `SELECT OrderDate FROM \`order\` WHERE CustomerID = ? ORDER BY OrderDate DESC LIMIT 1`,
    [req.user.sub]
  );
  const daysSince = lastOrder ? Math.floor((Date.now() - new Date(lastOrder.OrderDate)) / 86400000) : null;
  res.json({ daysSinceLastOrder: daysSince });
}));

module.exports = router;