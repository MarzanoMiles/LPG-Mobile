const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');
const { logActivity } = require('../utils/logger');

const router = express.Router();

const BASE_SELECT = `
  SELECT p.*, c.Category, b.Brand, s.SupplierName, s.LeadTimeDays AS SupplierLeadTimeDays
  FROM product p
  JOIN category c ON c.CategoryID = p.CategoryID
  JOIN brand b ON b.BrandID = p.BrandID
  JOIN supplier s ON s.SupplierID = p.SupplierID
`;

router.get('/', asyncHandler(async (req, res) => {
  const { status, supplierId } = req.query;
  const clauses = [];
  const params = [];
  if (status) { clauses.push('p.Status = ?'); params.push(status); }
  if (supplierId) { clauses.push('p.SupplierID = ?'); params.push(supplierId); }
  const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';
  const [rows] = await pool.query(`${BASE_SELECT} ${where} ORDER BY p.ProductName`, params);
  res.json(rows);
}));

router.get('/:id', asyncHandler(async (req, res) => {
  const [rows] = await pool.query(`${BASE_SELECT} WHERE p.ProductID = ?`, [req.params.id]);
  if (!rows[0]) return res.status(404).json({ message: 'Product not found' });
  res.json(rows[0]);
}));

router.post('/', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { productName, categoryId, brandId, supplierId, unit, unitPrice, costPrice, reorderLevel, imageUrl, arModelUrl } = req.body;

  if (!productName || !categoryId || !brandId || !supplierId || !unit || unitPrice == null || costPrice == null) {
    return res.status(400).json({
      message: 'productName, categoryId, brandId, supplierId, unit, unitPrice and costPrice are required',
    });
  }

  const [result] = await pool.query(
    `INSERT INTO product (ProductName, CategoryID, BrandID, SupplierID, Unit, UnitPrice, CostPrice, ReorderLevel, ImageURL, ARModelURL, Status)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active')`,
    [productName, categoryId, brandId, supplierId, unit, unitPrice, costPrice, reorderLevel ?? 0, imageUrl ?? null, arModelUrl ?? null]
  );
  await logActivity({ userId: req.user.sub, activityType: 'Create', module: 'Products', recordId: result.insertId, description: 'Created a new product' });
  res.status(201).json({ productId: result.insertId });
}));

// Optional fields (unit, prices, reorder level, image, AR model, status) keep their
// existing value when omitted, so partial updates from the app can't wipe data.
router.put('/:id', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { productName, categoryId, brandId, supplierId, unit, unitPrice, costPrice, reorderLevel, imageUrl, arModelUrl, status } = req.body;

  if (!productName || !categoryId || !brandId || !supplierId) {
    return res.status(400).json({ message: 'productName, categoryId, brandId and supplierId are required' });
  }
  if (status && !['Active', 'Inactive'].includes(status)) {
    return res.status(400).json({ message: 'status must be "Active" or "Inactive"' });
  }

  await pool.query(
    `UPDATE product
        SET ProductName = ?, CategoryID = ?, BrandID = ?, SupplierID = ?,
            Unit = COALESCE(?, Unit),
            UnitPrice = COALESCE(?, UnitPrice),
            CostPrice = COALESCE(?, CostPrice),
            ReorderLevel = COALESCE(?, ReorderLevel),
            ImageURL = COALESCE(?, ImageURL),
            ARModelURL = COALESCE(?, ARModelURL),
            Status = COALESCE(?, Status)
      WHERE ProductID = ?`,
    [
      productName, categoryId, brandId, supplierId,
      unit ?? null, unitPrice ?? null, costPrice ?? null, reorderLevel ?? null,
      imageUrl ?? null, arModelUrl ?? null, status ?? null,
      req.params.id,
    ]
  );
  await logActivity({ userId: req.user.sub, activityType: 'Update', module: 'Products', recordId: req.params.id, description: 'Updated a product' });
  res.json({ message: 'Updated' });
}));

router.delete('/:id', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  await pool.query(`UPDATE product SET Status='Inactive' WHERE ProductID = ?`, [req.params.id]);
  await logActivity({ userId: req.user.sub, activityType: 'Delete', module: 'Products', recordId: req.params.id, description: 'Deactivated a product' });
  res.status(204).send();
}));

module.exports = router;