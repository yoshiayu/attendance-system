DROP TABLE IF EXISTS fix_request CASCADE;
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    email VARCHAR(255) UNIQUE,
    slack_webhook VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    record_date DATE NOT NULL,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    break_start_time TIMESTAMP,
    break_end_time TIMESTAMP,
    location VARCHAR(255),
    status VARCHAR(50) DEFAULT 'normal',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE fix_request (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    attendance_id INT,
    request_date DATE NOT NULL,
    new_check_in_time TIMESTAMP,
    new_check_out_time TIMESTAMP,
    new_break_start_time TIMESTAMP,
    new_break_end_time TIMESTAMP,
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (attendance_id) REFERENCES attendance(id)
);
