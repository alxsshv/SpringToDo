INSERT INTO service_users (username, email, password)
VALUES ('testuser', 'testuser@email.com', 'testpassword'),
('user2', 'user2@email.com', 'pass2'),
('user3', 'user3@email.com', 'pass3');

INSERT INTO security_roles (user_id, security_role)
VALUES (1, 'ROLE_ADMIN'), (1, 'ROLE_USER'), (2, 'ROLE_USER');

INSERT INTO boards (user_id, title)
VALUES (1, 'work'),
(1, 'work'),
(1, 'home'),
(2, 'meets');
