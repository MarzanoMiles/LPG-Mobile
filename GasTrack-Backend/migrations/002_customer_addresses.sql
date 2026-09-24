-- Customer delivery addresses. Kept separate from `customer.Address`
-- (which remains the legacy single-address field for walk-in/POS records)
-- so the app can support multiple saved addresses per customer.
CREATE TABLE IF NOT EXISTS `customeraddress` (
  `AddressID` int NOT NULL AUTO_INCREMENT,
  `CustomerID` int NOT NULL,
  `Label` varchar(50) NOT NULL,
  `AddressLine` varchar(255) NOT NULL,
  `AddressType` varchar(20) NOT NULL DEFAULT 'Other',
  `IsPrimary` tinyint(1) NOT NULL DEFAULT '0',
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`AddressID`),
  KEY `CustomerID` (`CustomerID`),
  CONSTRAINT `customeraddress_ibfk_1` FOREIGN KEY (`CustomerID`) REFERENCES `customer` (`CustomerID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;