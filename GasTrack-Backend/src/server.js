require('dotenv').config();
const app = require('./app');
const pool = require('./config/db');

const PORT = process.env.PORT || 4000;

(async () => {
  try {
    await pool.query('SELECT 1');
    console.log('✅ Connected to MySQL database:', process.env.DB_NAME);
  } catch (err) {
    console.error('❌ Could not connect to MySQL. Check XAMPP is running and .env is correct.');
    console.error(err.message);
    process.exit(1);
  }

  app.listen(PORT, () => {
    console.log(`🚀 GasTrack API running at http://localhost:${PORT}`);
  });
})();