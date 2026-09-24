-- Non-destructive: adds a separate auth table instead of altering `customer`,
-- since existing customer rows are walk-in POS records with no login concept.
CREATE TABLE IF NOT EXISTS `customerauth` (
  `CustomerAuthID` int NOT NULL AUTO_INCREMENT,
  `CustomerID` int NOT NULL,
  `Email` varchar(150) NOT NULL,
  `PasswordHash` varchar(255) NOT NULL,
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`CustomerAuthID`),
  UNIQUE KEY `Email` (`Email`),
  UNIQUE KEY `CustomerID` (`CustomerID`),
  CONSTRAINT `customerauth_ibfk_1` FOREIGN KEY (`CustomerID`) REFERENCES `customer` (`CustomerID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;