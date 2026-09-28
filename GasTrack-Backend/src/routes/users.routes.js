const express = require('express');
const bcrypt = require('bcryptjs');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireRole } = require('../middleware/auth');
const { logActivity } = require('../utils/logger');

const router = express.Router();

router.get('/', authenticate, requireRole('Admin', 'Manager'), asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT u.UserID, u.FirstName, u.LastName, u.Email, u.Status, u.ModuleAccess,
            u.RoleID, u.WarehouseID, r.RoleName, w.WarehouseName
     FROM user u
     JOIN role r ON r.RoleID = u.RoleID
     LEFT JOIN warehouse w ON w.WarehouseID = u.WarehouseID
     ORDER BY u.FirstName`
  );
  res.json(rows);
}));

router.post('/', authenticate, requireRole('Admin'), asyncHandler(async (req, res) => {
  const { firstName, lastName, email, password, roleId, warehouseId, moduleAccess } = req.body;
  if (!firstName || !lastName || !email || !password || !roleId) {
    return res.status(400).json({ message: 'firstName, lastName, email, password and roleId are required' });
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const [result] = await pool.query(
    `INSERT INTO user (CompanyID, RoleID, WarehouseID, FirstName, LastName, Email, PasswordHash, Status, ModuleAccess)
     VALUES (1, ?, ?, ?, ?, ?, ?, 'Active', ?)`,
    [roleId, warehouseId || null, firstName, lastName, email, passwordHash, moduleAccess ? JSON.stringify(moduleAccess) : null]
  );
  await logActivity({ userId: req.user.sub, activityType: 'Create', module: 'Users', recordId: result.insertId, description: 'Created a new user' });
  res.status(201).json({ userId: result.insertId });
}));

// Edit a user. ModuleAccess and Status are only changed when explicitly provided.
router.put('/:id', authenticate, requireRole('Admin'), asyncHandler(async (req, res) => {
  const { firstName, lastName, roleId, warehouseId, moduleAccess, status } = req.body;

  if (!firstName || !lastName || !roleId) {
    return res.status(400).json({ message: 'firstName, lastName and roleId are required' });
  }
  if (status && !['Active', 'Inactive'].includes(status)) {
    return res.status(400).json({ message: 'status must be "Active" or "Inactive"' });
  }
  if (status === 'Inactive' && Number(req.params.id) === Number(req.user.sub)) {
    return res.status(400).json({ message: 'You cannot deactivate your own account' });
  }

  await pool.query(
    `UPDATE user
        SET FirstName = ?, LastName = ?, RoleID = ?, WarehouseID = ?,
            ModuleAccess = COALESCE(?, ModuleAccess),
            Status = COALESCE(?, Status)
      WHERE UserID = ?`,
    [
      firstName,
      lastName,
      roleId,
      warehouseId ?? null,
      moduleAccess ? JSON.stringify(moduleAccess) : null,
      status ?? null,
      req.params.id,
    ]
  );
  await logActivity({ userId: req.user.sub, activityType: 'Update', module: 'Users', recordId: req.params.id, description: 'Updated a user' });
  res.json({ message: 'Updated' });
}));

router.delete('/:id', authenticate, requireRole('Admin'), asyncHandler(async (req, res) => {
  if (Number(req.params.id) === Number(req.user.sub)) {
    return res.status(400).json({ message: 'You cannot deactivate your own account' });
  }
  await pool.query(`UPDATE user SET Status='Inactive' WHERE UserID=?`, [req.params.id]);
  await logActivity({ userId: req.user.sub, activityType: 'Delete', module: 'Users', recordId: req.params.id, description: 'Deactivated a user' });
  res.status(204).send();
}));

router.get('/activity-log', authenticate, requireRole('Admin', 'Manager'), asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT ua.*, u.FirstName, u.LastName, r.RoleName
     FROM useractivity ua JOIN user u ON u.UserID = ua.UserID JOIN role r ON r.RoleID = u.RoleID
     ORDER BY ua.ActivityDate DESC LIMIT 200`
  );
  res.json(rows);
}));

router.get('/roles', authenticate, asyncHandler(async (req, res) => {
  const [rows] = await pool.query('SELECT * FROM role ORDER BY RoleID');
  res.json(rows);
}));

module.exports = router;