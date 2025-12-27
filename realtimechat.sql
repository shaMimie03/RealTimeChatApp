-- 1. Select the Database
USE realtimechat;

-- 2. Create the Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL
    );

-- 3. Create the Messages Table
CREATE TABLE IF NOT EXISTS messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(50),
    content TEXT,
    timestamp BIGINT
    );

-- 4. Insert Your Group Members (Users)
-- Password for everyone is '1234' for easy testing
INSERT INTO users (username, password) VALUES ('admin', 'admin');
INSERT INTO users (username, password) VALUES ('tammie', '1234');
INSERT INTO users (username, password) VALUES ('wan', '1234');
INSERT INTO users (username, password) VALUES ('amirah', '1234');
INSERT INTO users (username, password) VALUES ('shamimie', '1234');

-- 5. Insert Initial Chat History
-- This ensures the chat window isn't empty when you first log in
INSERT INTO messages (sender, content, timestamp)
VALUES ('admin', 'Welcome to the Real-Time Chat System!', 1735200000000);

INSERT INTO messages (sender, content, timestamp)
VALUES ('tammie', 'Hello! I am testing the connection.', 1735200050000);

INSERT INTO messages (sender, content, timestamp)
VALUES ('wan', 'The server looks stable.', 1735200100000);

INSERT INTO messages (sender, content, timestamp)
VALUES ('amirah', 'I can see the message history.', 1735200150000);

INSERT INTO messages (sender, content, timestamp)
VALUES ('shamimie', 'Great job team!', 1735200200000);

-- 6. Verify Data (Optional: Shows you what was inserted)
SELECT * FROM users;
SELECT * FROM messages;