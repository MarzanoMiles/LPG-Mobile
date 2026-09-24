const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/', asyncHandler(async (req, res) => {
  const [rows] = await pool.query('SELECT * FROM brand ORDER BY Brand');
  res.json(rows);
}));

router.post('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { brand } = req.body;
  const [result] = await pool.query('INSERT INTO brand (Brand) VALUES (?)', [brand]);
  res.status(201).json({ brandId: result.insertId });
}));

router.delete('/:id', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  await pool.query('DELETE FROM brand WHERE BrandID = ?', [req.params.id]);
  res.status(204).send();
}));

module.exports = router;