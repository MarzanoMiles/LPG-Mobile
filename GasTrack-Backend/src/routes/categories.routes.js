const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/', asyncHandler(async (req, res) => {
  const [rows] = await pool.query('SELECT * FROM category ORDER BY Category');
  res.json(rows);
}));

router.post('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { category } = req.body;
  const [result] = await pool.query('INSERT INTO category (Category) VALUES (?)', [category]);
  res.status(201).json({ categoryId: result.insertId });
}));

router.delete('/:id', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  await pool.query('DELETE FROM category WHERE CategoryID = ?', [req.params.id]);
  res.status(204).send();
}));

module.exports = router;