const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, asyncHandler(async (req, res) => {
  const { status, customerId } = req.query;
  const clauses = [];
  const params = [];
  if (status) { clauses.push('o.OrderStatus = ?'); params.push(status); }
  if (customerId) { clauses.push('o.CustomerID = ?'); params.push(customerId); }
  if (req.user.type === 'customer') { clauses.push('o.CustomerID = ?'); params.push(req.user.sub); }
  const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';

  const [rows] = await pool.query(
    `SELECT o.*, c.CustomerName,
            (SELECT COUNT(*) FROM orderdetails od WHERE od.OrderID = o.OrderID) AS ItemCount
     FROM \`order\` o
     JOIN customer c ON c.CustomerID = o.CustomerID
     ${where}
     ORDER BY o.OrderDate DESC`,
    params
  );
  res.json(rows);
}));

router.get('/:id', authenticate, asyncHandler(async (req, res) => {
  const [orderRows] = await pool.query(`SELECT * FROM \`order\` WHERE OrderID = ?`, [req.params.id]);
  const order = orderRows[0];
  if (!order) return res.status(404).json({ message: 'Order not found' });

  const [items] = await pool.query(
    `SELECT od.*, p.ProductName FROM orderdetails od JOIN product p ON p.ProductID = od.ProductID WHERE od.OrderID = ?`,
    [req.params.id]
  );
  res.json({ ...order, items });
}));

module.exports = router;