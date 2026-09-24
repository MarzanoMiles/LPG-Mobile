const express = require('express');
const bcrypt = require('bcryptjs');
const pool = require('../config/db');
const { signToken } = require('../utils/jwt');
const asyncHandler = require('../utils/asyncHandler');
const { logActivity } = require('../utils/logger');
const { authenticate } = require('../middleware/auth');

const router = express.Router();

// ---------- EMPLOYEE ----------

router.post('/employee/login', asyncHandler(async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) return res.status(400).json({ message: 'Email and password required' });

  const [rows] = await pool.query(
    `SELECT u.*, r.RoleName FROM user u
     JOIN role r ON r.RoleID = u.RoleID
     WHERE u.Email = ?`,
    [email]
  );
  const user = rows[0];
  if (!user) return res.status(401).json({ message: 'Invalid credentials' });
  if (user.Status !== 'Active') return res.status(403).json({ message: 'Account is inactive' });

  const match = await bcrypt.compare(password, user.PasswordHash);
  if (!match) return res.status(401).json({ message: 'Invalid credentials' });

  const token = signToken({
    sub: user.UserID,
    type: 'employee',
    email: user.Email,
    roleId: user.RoleID,
    roleName: user.RoleName,
    companyId: user.CompanyID,
    warehouseId: user.WarehouseID,
  });

  await logActivity({ userId: user.UserID, activityType: 'Login', module: 'Auth', description: 'User logged in' });

  res.json({
    token,
    user: {
      userId: user.UserID,
      firstName: user.FirstName,
      lastName: user.LastName,
      email: user.Email,
      role: user.RoleName,
      companyId: user.CompanyID,
      warehouseId: user.WarehouseID,
      moduleAccess: user.ModuleAccess,
    },
  });
}));

router.post('/employee/register', asyncHandler(async (req, res) => {
  const { firstName, lastName, email, password, roleId, warehouseId, companyId } = req.body;
  if (!firstName || !lastName || !email || !password) {
    return res.status(400).json({ message: 'Missing required fields' });
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const [result] = await pool.query(
    `INSERT INTO user (CompanyID, RoleID, WarehouseID, FirstName, LastName, Email, PasswordHash, Status)
     VALUES (?, ?, ?, ?, ?, ?, ?, 'Active')`,
    [companyId || 1, roleId || 3, warehouseId || null, firstName, lastName, email, passwordHash]
  );

  await logActivity({ userId: result.insertId, activityType: 'Create', module: 'Users', recordId: result.insertId, description: 'Account activated' });

  const token = signToken({ sub: result.insertId, type: 'employee', email, roleId: roleId || 3, companyId: companyId || 1, warehouseId: warehouseId || null });
  res.status(201).json({ token, userId: result.insertId });
}));

// ---------- CUSTOMER ----------

router.post('/customer/register', asyncHandler(async (req, res) => {
  const { fullName, email, password, contactNo, address, customerType } = req.body;
  if (!fullName || !email || !password || !contactNo || !address) {
    return res.status(400).json({ message: 'Missing required fields' });
  }

  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();

    const [custResult] = await conn.query(
      `INSERT INTO customer (CustomerType, CustomerName, ContactNo, Address, Status)
       VALUES (?, ?, ?, ?, 'Active')`,
      [customerType || 'Residential', fullName, contactNo, address]
    );

    const passwordHash = await bcrypt.hash(password, 10);
    await conn.query(
      `INSERT INTO customerauth (CustomerID, Email, PasswordHash) VALUES (?, ?, ?)`,
      [custResult.insertId, email, passwordHash]
    );

    await conn.commit();

    const token = signToken({ sub: custResult.insertId, type: 'customer', email });
    res.status(201).json({ token, customerId: custResult.insertId });
  } catch (err) {
    await conn.rollback();
    throw err;
  } finally {
    conn.release();
  }
}));

router.post('/customer/login', asyncHandler(async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) return res.status(400).json({ message: 'Email and password required' });

  const [rows] = await pool.query(
    `SELECT ca.*, c.CustomerName, c.ContactNo, c.Address, c.CustomerType, c.Status
     FROM customerauth ca
     JOIN customer c ON c.CustomerID = ca.CustomerID
     WHERE ca.Email = ?`,
    [email]
  );
  const account = rows[0];
  if (!account) return res.status(401).json({ message: 'Invalid credentials' });
  if (account.Status !== 'Active') return res.status(403).json({ message: 'Account is inactive' });

  const match = await bcrypt.compare(password, account.PasswordHash);
  if (!match) return res.status(401).json({ message: 'Invalid credentials' });

  const token = signToken({ sub: account.CustomerID, type: 'customer', email: account.Email });
  res.json({
    token,
    customer: {
      customerId: account.CustomerID,
      fullName: account.CustomerName,
      contactNo: account.ContactNo,
      address: account.Address,
      customerType: account.CustomerType,
    },
  });
}));

// ---------- SHARED ----------

router.get('/me', authenticate, asyncHandler(async (req, res) => {
  if (req.user.type === 'employee') {
    const [rows] = await pool.query(
      `SELECT u.UserID, u.FirstName, u.LastName, u.Email, u.WarehouseID, r.RoleName
       FROM user u JOIN role r ON r.RoleID = u.RoleID WHERE u.UserID = ?`,
      [req.user.sub]
    );
    return res.json(rows[0] || null);
  }
  const [rows] = await pool.query(
    `SELECT c.CustomerID, c.CustomerName, c.ContactNo, c.Address, c.CustomerType, ca.Email
     FROM customer c JOIN customerauth ca ON ca.CustomerID = c.CustomerID WHERE c.CustomerID = ?`,
    [req.user.sub]
  );
  res.json(rows[0] || null);
}));

module.exports = router;