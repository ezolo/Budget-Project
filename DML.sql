-- DML
-- users mock data
INSERT INTO users (name, username, email, password) VALUES
('Default User', 'defaultuser', 'default@example.com', 'defaultpass'),
('John Smith', 'jSmith', 'john@example.com', 'password123'),
('Jane Smith', 'jSmith1', 'jane@example.com', 'password321');

--accounts mock data
INSERT INTO accounts (id, user_id, name, balance) VALUES
('1', '1', 'savings', '100'),
('2', '1', 'checking', '200'),
('3', '2', 'savings', '100'),
('4', '2', 'checking', '100');

--categories mock data
INSERT INTO categories (user_id, name, is_predefined, image_path, is_custom_image)
VALUES 
-- Essential Expenses
(2, 'Housing', TRUE, 'resources/categories/house_image.jpg', FALSE),
(2, 'Utilities', TRUE, 'resources/categories/utilities_image.jpg', FALSE),
(2, 'Groceries', TRUE, 'resources/categories/groceries_image.jpg', FALSE),
(2, 'Transportation', TRUE, 'resources/categories/car_image.jpg', FALSE),
(2, 'Healthcare', TRUE, 'resources/categories/healthcare_image.jpg', FALSE),
(2, 'Loans', TRUE, 'resources/categories/loans_image.jpg', FALSE),

-- Non-Essential Expenses
(2, 'Entertainment', TRUE, 'resources/categories/entertainment_image.jpg', FALSE),
(2, 'Travel', TRUE, 'resources/categories/travel_image.jpg', FALSE),
(2, 'Shopping', TRUE, 'resources/categories/shopping_image.jpg', FALSE),
(2, 'Subscriptions', TRUE, 'resources/categories/subscriptions_image.jpg', FALSE);
