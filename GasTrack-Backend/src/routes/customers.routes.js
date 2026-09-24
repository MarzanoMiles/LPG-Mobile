const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { search } = req.query;
  const clauses = [];
  const params = [];
  if (search) {
    clauses.push('(CustomerName LIKE ? OR ContactNo LIKE ?)');
    params.push(`%${search}%`, `%${search}%`);
  }
  const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';
  const [rows] = await pool.query(`SELECT * FROM customer ${where} ORDER BY CustomerName`, params);
  res.json(rows);
}));

router.post('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { customerType, customerName, contactNo, address } = req.body;
  const [result] = await pool.query(
    `INSERT INTO customer (CustomerType, CustomerName, ContactNo, Address, Status) VALUES (?, ?, ?, ?, 'Active')`,
    [customerType || 'Residential', customerName || '', contactNo, address]
  );
  res.status(201).json({ customerId: result.insertId });
}));

router.put('/:id', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { customerType, customerName, contactNo, address, status } = req.body;
  await pool.query(
    `UPDATE customer SET CustomerType=?, CustomerName=?, ContactNo=?, Address=?, Status=? WHERE CustomerID=?`,
    [customerType, customerName, contactNo, address, status, req.params.id]
  );
  res.json({ message: 'Updated' });
}));

// Own profile (customer app)
router.get('/me/profile', authenticate, asyncHandler(async (req, res) => {
  if (req.user.type !== 'customer') return res.status(403).json({ message: 'Customer access only' });
  const [rows] = await pool.query(
    `SELECT c.*, ca.Email FROM customer c JOIN customerauth ca ON ca.CustomerID = c.CustomerID WHERE c.CustomerID = ?`,
    [req.user.sub]
  );
  res.json(rows[0] || null);
}));

router.put('/me/profile', authenticate, asyncHandler(async (req, res) => {
  if (req.user.type !== 'customer') return res.status(403).json({ message: 'Customer access only' });
  const { customerName, contactNo, address, customerType } = req.body;
  await pool.query(
    `UPDATE customer SET CustomerName=?, ContactNo=?, Address=?, CustomerType=? WHERE CustomerID=?`,
    [customerName, contactNo, address, customerType, req.user.sub]
  );
  res.json({ message: 'Updated' });
}));

module.exports = router;