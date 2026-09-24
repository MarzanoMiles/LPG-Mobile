const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT r.*, p.ProductName, s.SupplierName
     FROM restockrecommendation r
     JOIN product p ON p.ProductID = r.ProductID
     JOIN supplier s ON s.SupplierID = r.SupplierID
     WHERE r.Status != 'Converted'
     ORDER BY r.ForecastDate DESC`
  );
  res.json(rows);
}));

// Simple heuristic AI-style recommendation: flags anything at/below reorder level
router.post('/generate', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [lowStock] = await pool.query(
    `SELECT i.ProductID, i.StockOnHand, p.ReorderLevel, p.SupplierID
     FROM inventory i JOIN product p ON p.ProductID = i.ProductID
     WHERE i.StockOnHand <= p.ReorderLevel`
  );

  const created = [];
  for (const item of lowStock) {
    const predictedDemand = item.ReorderLevel * 2;
    const recommendedQuantity = Math.max(predictedDemand - item.StockOnHand, item.ReorderLevel);
    const [result] = await pool.query(
      `INSERT INTO restockrecommendation (ProductID, SupplierID, StockOnHand, PredictedDemand, RecommendedQuantity, ForecastDate, Status)
       VALUES (?, ?, ?, ?, ?, CURDATE(), 'Pending')`,
      [item.ProductID, item.SupplierID, item.StockOnHand, predictedDemand, recommendedQuantity]
    );
    created.push(result.insertId);
  }
  res.status(201).json({ createdCount: created.length, ids: created });
}));

module.exports = router;