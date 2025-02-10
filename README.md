QUERY SQL 
---------
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    username VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    is_admin BOOLEAN DEFAULT FALSE
);

CREATE TABLE vehicles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    type VARCHAR(50),
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE rentals (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    vehicle_id INT,
    rental_date DATETIME,
    return_date DATETIME,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

-------------------------------------------------------------- DUMMIES DATA

-- Users table
INSERT INTO users (name, username, password, is_admin) VALUES
('Eva Lunggita', 'evalunggita', 'loyaLty18!', FALSE),
('Dewi Fortuna', 'dewi', 'dewifortuna', TRUE),
('Eka Satria', 'ekasatria', 'eka17', FALSE),
('Inkra Andini', 'inkrandini', 'inkracantik', FALSE),
('Zahra Cantiabella', 'abel', 'abelle', FALSE),
('Admin User', 'superadmin', 'admin456', TRUE);

-- Vehicles table
INSERT INTO vehicles (name, type, available) VALUES
('Toyota Avanza', 'MPV', TRUE),
('Honda Jazz', 'Hatchback', TRUE),
('Toyota Innova', 'MPV', TRUE),
('Honda PCX', 'Motorcycle', FALSE),
('Yamaha NMAX', 'Motorcycle', FALSE),
('Toyota Fortuner', 'SUV', TRUE),
('Mitsubishi Xpander', 'MPV', TRUE);

-- Rentals table
INSERT INTO rentals (user_id, vehicle_id, rental_date, return_date, active) VALUES
(2, 1, '2024-02-01 10:00:00', '2024-02-03 10:00:00', FALSE),
(3, 2, '2024-02-03 14:00:00', '2024-02-05 14:00:00', FALSE),
(4, 4, '2024-02-05 09:00:00', '2024-02-07 09:00:00', TRUE),
(2, 6, '2024-02-07 13:00:00', '2024-02-09 13:00:00', FALSE),
(3, 5, '2024-02-08 11:00:00', '2024-02-10 11:00:00', TRUE);
