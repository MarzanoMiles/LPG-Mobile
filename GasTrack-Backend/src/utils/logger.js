const pool = require('../config/db');

// Writes a row to useractivity (matches the app's existing audit trail pattern)
async function logActivity({ userId, activityType, module, recordId = null, description = null }) {
  try {
    await pool.query(
      `INSERT INTO useractivity (UserID, ActivityType, Module, RecordID, Description)
       VALUES (?, ?, ?, ?, ?)`,
      [userId, activityType, module, recordId, description]
    );
  } catch (err) {
    console.error('Failed to log activity:', err.message);
  }
}

module.exports = { logActivity };