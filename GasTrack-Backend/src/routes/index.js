const express = require('express');
const router = express.Router();

router.use('/auth', require('./auth.routes'));
router.use('/users', require('./users.routes'));
router.use('/products', require('./products.routes'));
router.use('/categories', require('./categories.routes'));
router.use('/brands', require('./brands.routes'));
router.use('/suppliers', require('./suppliers.routes'));
router.use('/warehouses', require('./warehouses.routes'));
router.use('/inventory', require('./inventory.routes'));
router.use('/customers', require('./customers.routes'));
router.use('/orders', require('./orders.routes'));
router.use('/sales', require('./sales.routes'));
router.use('/deliveries', require('./deliveries.routes'));
router.use('/purchase-orders', require('./purchaseorders.routes'));
router.use('/restock', require('./restock.routes'));
router.use('/dashboard', require('./dashboard.routes'));
router.use('/company-settings', require('./companysettings.routes'));
router.use('/reports', require('./reports.routes'));

module.exports = router;