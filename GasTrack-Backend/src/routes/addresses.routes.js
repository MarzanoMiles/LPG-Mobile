const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate } = require('../middleware/auth');

const router = express.Router();

function requireCustomer(req, res, next) {
  if (req.user.type !== 'customer') {
    return res.status(403).json({ message: 'Customer access only' });
  }
  next();
}

// List all addresses for the logged-in customer, primary first
router.get('/', authenticate, requireCustomer, asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT * FROM customeraddress WHERE CustomerID = ? ORDER BY IsPrimary DESC, CreatedAt DESC`,
    [req.user.sub]
  );
  res.json(rows);
}));

// Get just the primary address (used by Checkout)
router.get('/primary', authenticate, requireCustomer, asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT * FROM customeraddress WHERE CustomerID = ? AND IsPrimary = 1 LIMIT 1`,
    [req.user.sub]
  );
  if (rows[0]) return res.json(rows[0]);

  // Fall back to the most recently added address if none is marked primary yet
  const [fallback] = await pool.query(
    `SELECT * FROM customeraddress WHERE CustomerID = ? ORDER BY CreatedAt DESC LIMIT 1`,
    [req.user.sub]
  );
  res.json(fallback[0] || null);
}));

router.post('/', authenticate, requireCustomer, asyncHandler(async (req, res) => {
  const { label, addressLine, addressType } = req.body;
  if (!label || !addressLine) {
    return res.status(400).json({ message: 'label and addressLine are required' });
  }

  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();

    const [[existingCount]] = await conn.query(
      `SELECT COUNT(*) AS count FROM customeraddress WHERE CustomerID = ?`,
      [req.user.sub]
    );
    const isFirstAddress = existingCount.count === 0;

    const [result] = await conn.query(
      `INSERT INTO customeraddress (CustomerID, Label, AddressLine, AddressType, IsPrimary)
       VALUES (?, ?, ?, ?, ?)`,
      [req.user.sub, label, addressLine, addressType || 'Other', isFirstAddress ? 1 : 0]
    );

    await conn.commit();
    res.status(201).json({ addressId: result.insertId, isPrimary: isFirstAddress });
  } catch (err) {
    await conn.rollback();
    throw err;
  } finally {
    conn.release();
  }
}));

router.put('/:id/set-primary', authenticate, requireCustomer, asyncHandler(async (req, res) => {
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();

    const [[address]] = await conn.query(
      `SELECT * FROM customeraddress WHERE AddressID = ? AND CustomerID = ?`,
      [req.params.id, req.user.sub]
    );
    if (!address) throw Object.assign(new Error('Address not found'), { status: 404 });

    await conn.query(`UPDATE customeraddress SET IsPrimary = 0 WHERE CustomerID = ?`, [req.user.sub]);
    await conn.query(`UPDATE customeraddress SET IsPrimary = 1 WHERE AddressID = ?`, [req.params.id]);

    await conn.commit();
    res.json({ message: 'Primary address updated' });
  } catch (err) {
    await conn.rollback();
    throw err;
  } finally {
    conn.release();
  }
}));

router.delete('/:id', authenticate, requireCustomer, asyncHandler(async (req, res) => {
  const [[address]] = await pool.query(
    `SELECT * FROM customeraddress WHERE AddressID = ? AND CustomerID = ?`,
    [req.params.id, req.user.sub]
  );
  if (!address) return res.status(404).json({ message: 'Address not found' });

  await pool.query(`DELETE FROM customeraddress WHERE AddressID = ?`, [req.params.id]);

  // If we just deleted the primary address, promote the next most recent one
  if (address.IsPrimary) {
    const [remaining] = await pool.query(
      `SELECT * FROM customeraddress WHERE CustomerID = ? ORDER BY CreatedAt DESC LIMIT 1`,
      [req.user.sub]
    );
    if (remaining[0]) {
      await pool.query(`UPDATE customeraddress SET IsPrimary = 1 WHERE AddressID = ?`, [remaining[0].AddressID]);
    }
  }

  res.status(204).send();
}));

module.exports = router;