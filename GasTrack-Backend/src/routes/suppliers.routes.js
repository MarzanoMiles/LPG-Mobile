const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query('SELECT * FROM supplier ORDER BY SupplierName');
  res.json(rows);
}));

router.get('/:id', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query('SELECT * FROM supplier WHERE SupplierID = ?', [req.params.id]);
  if (!rows[0]) return res.status(404).json({ message: 'Supplier not found' });
  res.json(rows[0]);
}));

router.post('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { supplierName, contactPerson, email, address, contact, leadTimeDays } = req.body;
  const [result] = await pool.query(
    `INSERT INTO supplier (SupplierName, ContactPerson, Email, Address, Contact, LeadTimeDays, Status)
     VALUES (?, ?, ?, ?, ?, ?, 'Active')`,
    [supplierName, contactPerson, email, address, contact, leadTimeDays || 0]
  );
  res.status(201).json({ supplierId: result.insertId });
}));

router.put('/:id', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { supplierName, contactPerson, email, address, contact, leadTimeDays, status } = req.body;
  await pool.query(
    `UPDATE supplier SET SupplierName=?, ContactPerson=?, Email=?, Address=?, Contact=?, LeadTimeDays=?, Status=?
     WHERE SupplierID = ?`,
    [supplierName, contactPerson, email, address, contact, leadTimeDays, status, req.params.id]
  );
  res.json({ message: 'Updated' });
}));

router.delete('/:id', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  await pool.query(`UPDATE supplier SET Status='Inactive' WHERE SupplierID = ?`, [req.params.id]);
  res.status(204).send();
}));

module.exports = router;