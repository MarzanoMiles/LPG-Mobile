const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT d.*, s.SaleNo, s.TotalAmount, s.SaleDate, s.OrderID, c.CustomerName,
            (SELECT COUNT(*) FROM orderdetails od WHERE od.OrderID = s.OrderID) AS ItemCount,
            (SELECT GROUP_CONCAT(CONCAT(od.Quantity, ' x ', p.ProductName) SEPARATOR ', ')
               FROM orderdetails od JOIN product p ON p.ProductID = od.ProductID
              WHERE od.OrderID = s.OrderID) AS ItemSummary
     FROM delivery d
     JOIN sales s ON s.SaleID = d.SaleID
     JOIN customer c ON c.CustomerID = s.CustomerID
     ORDER BY d.DeliveryID DESC`
  );
  res.json(rows);
}));

router.put('/:id/status', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { deliveryStatus, deliveredByUserId } = req.body;
  await pool.query(
    `UPDATE delivery SET DeliveryStatus=?, DeliveredByUserID=?, DeliveryDate=IF(? = 'Delivered', NOW(), DeliveryDate) WHERE DeliveryID=?`,
    [deliveryStatus, deliveredByUserId || req.user.sub, deliveryStatus, req.params.id]
  );
  res.json({ message: 'Updated' });
}));

module.exports = router;