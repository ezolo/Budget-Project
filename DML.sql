-- DML
-- users mock data
INSERT INTO users (name, username, email, password) VALUES
('Default User', 'defaultuser', 'default@example.com', 'defaultpass'),
('John Smith', 'jSmith', 'john@example.com', 'password123'),
('Jane Smith', 'jSmith1', 'jane@example.com', 'password321');

-- records mock data (for username jSmith)
INSERT INTO expenses VALUES 
(27,2,3,11,'2024-04-01',NULL,100.00,'Income','part-time'),
(28,2,3,8,'2025-04-01',NULL,100.00,'Income','travel fund'),
(29,2,4,11,'2025-04-01',NULL,1000.00,'Income','Salary'),
(30,2,4,3,'2025-04-30',NULL,150.00,'Expense','monthly groceries'),
(31,2,4,6,'2025-04-03',NULL,200.00,'Expense','Education Loan'),
(32,2,4,5,'2025-04-30',NULL,100.00,'Expense','medicines'),
(33,2,4,9,'2025-04-30',NULL,150.00,'Expense','Macy\'s'),
(34,2,2,10,'2025-04-03','April 2025',15.00,'expense','Netflix'),
(35,2,3,8,'2025-03-01',NULL,100.00,'Income','Travel funds'),
(39,2,3,8,'2025-02-01',NULL,100.00,'Income','Travel funds'),
(40,2,4,11,'2025-03-01',NULL,1000.00,'Income','income'),
(41,2,4,11,'2025-02-01',NULL,1000.00,'Income','income'),
(42,2,4,3,'2025-02-05',NULL,500.00,'Expense','overal monthly expense'),
(43,2,4,7,'2025-03-18',NULL,100.00,'Expense','fun day out'),
(44,2,4,3,'2025-03-11',NULL,50.00,'Expense','grocery');

-- accounts mock data
INSERT INTO accounts (id, user_id, account_name, balance) VALUES
('1', '1', 'savings', '100'),
('2', '1', 'checking', '200'),
('3', '2', 'savings', '100'),
('4', '2', 'checking', '100');

-- categories mock data
INSERT INTO categories (user_id, name, is_predefined, image_path, is_custom_image, description, is_essential)
VALUES 
-- Essential Expenses
(2, 'Housing', TRUE, 'resources/categories/house_image.jpg', FALSE, "Rent or mortgage payments, property taxes, and home maintenance costs", TRUE),
(2, 'Utilities', TRUE, 'resources/categories/utilities_image.jpg', FALSE, "Monthly bills for electricity, water, gas, internet, and phone services", TRUE),
(2, 'Groceries', TRUE, 'resources/categories/groceries_image.jpg', FALSE, "Monthly bills for electricity, water, gas, internet, and phone services", TRUE),
(2, 'Transportation', TRUE, 'resources/categories/car_image.jpg', FALSE, "Costs for fuel, public transit, car payments, insurance, and maintenance", TRUE),
(2, 'Healthcare', TRUE, 'resources/categories/healthcare_image.jpg', FALSE, "Expenses for insurance premiums, doctor visits, prescriptions, and medical supplies", TRUE),
(2, 'Loans', TRUE, 'resources/categories/loans_image.jpg', FALSE, "Payments toward student loans, personal loans, credit card debt, or other borrowed money", TRUE),
-- Non-Essential Expenses
(2, 'Entertainment', TRUE, 'resources/categories/entertainment_image.jpg', FALSE, "Spending on movies, games, dining out, streaming, and other leisure activities", FALSE),
(2, 'Travel', TRUE, 'resources/categories/travel_image.jpg', FALSE, "Spending on movies, games, dining out, streaming, and other leisure activities", FALSE),
(2, 'Shopping', TRUE, 'resources/categories/shopping_image.jpg', FALSE, "Purchases of clothing, electronics, gifts, and non-essential items", FALSE),
(2, 'Subscriptions', TRUE, 'resources/categories/subscriptions_image.jpg', FALSE, "Recurring charges for services like Netflix, Spotify, gym memberships, or apps", FALSE);

INSERT INTO badges (challenge_name, badge_image_path) 
VALUES 
('No Spend Day', 'resources/badges/No_Spend_Day_Badge.JPG');
