const express = require('express');
const pool = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');
const { authenticate, requireEmployee } = require('../middleware/auth');

const router = express.Router();

router.get('/compliance', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query('SELECT * FROM compliancereport ORDER BY DueDate ASC');
  res.json(rows);
}));

router.put('/compliance/:id/submit', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const { fileName } = req.body;
  await pool.query(
    `UPDATE compliancereport SET Status='Submitted', SubmittedAt=NOW(), SubmittedByUserID=?, FileName=? WHERE ReportID=?`,
    [req.user.sub, fileName || null, req.params.id]
  );
  res.json({ message: 'Submitted' });
}));

router.get('/data-activity', authenticate, requireEmployee, asyncHandler(async (req, res) => {
  const [rows] = await pool.query(
    `SELECT dal.*, u.FirstName, u.LastName FROM dataactivitylog dal JOIN user u ON u.UserID = dal.UserID ORDER BY dal.ActivityDate DESC LIMIT 200`
  );
  res.json(rows);
}));

module.exports = router;