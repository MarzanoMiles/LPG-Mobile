const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query('SELECT * FROM warehouse ORDER BY WarehouseName');
  res.json(rows);
}));

router.post('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { companyId, warehouseName, location } = req.body;
  const [result] = await pool.query(
    `INSERT INTO warehouse (CompanyID, WarehouseName, Location, Status) VALUES (?, ?, ?, 'Active')`,
    [companyId || 1, warehouseName, location]
  );
  res.status(201).json({ warehouseId: result.insertId });
}));

module.exports = router;