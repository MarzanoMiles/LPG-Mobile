const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireRole } = require('../middleware/auth');

const router = express.Router();

router.get('/:companyId', authenticate, asyncHandler(async (req, res) => {
  const [rows] = await pool.query('SELECT * FROM companysettings WHERE CompanyID = ?', [req.params.companyId]);
  res.json(rows[0] || null);
}));

router.put('/:companyId', authenticate, requireRole('Admin'), asyncHandler(async (req, res) => {
  const fields = req.body;
  const columns = Object.keys(fields);
  if (columns.length === 0) return res.status(400).json({ message: 'No fields provided' });

  const setClause = columns.map((c) => `\`${c}\` = ?`).join(', ');
  const values = columns.map((c) => fields[c]);
  await pool.query(`UPDATE companysettings SET ${setClause} WHERE CompanyID = ?`, [...values, req.params.companyId]);
  res.json({ message: 'Updated' });
}));

module.exports = router;