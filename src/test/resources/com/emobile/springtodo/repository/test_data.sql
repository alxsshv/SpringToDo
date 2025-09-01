INSERT INTO service_users (id, username, email, password)
VALUES (1, 'testuser', 'testuser@email.com', 'testpassword'),
(2,'user2', 'user2@email.com', 'pass2'),
(3, 'user3', 'user3@email.com', 'pass3');
ALTER SEQUENCE service_users_id_seq RESTART WITH 4;

INSERT INTO security_roles (id, user_id, security_role)
VALUES (1, 1, 'ROLE_ADMIN'), (2, 1, 'ROLE_USER'), (3, 3, 'ROLE_USER');
ALTER SEQUENCE security_roles_id_seq RESTART WITH 4;

INSERT INTO refresh_tokens (id, token, user_id, expire_date)
VALUES (1, 'USER2_TOKEN1', 2, '2025-07-23 00:00:00'),
 (2, 'USER2_TOKEN2', 2, '2926-07-23 00:00:00'),
 (3, 'USER1_TOKEN1', 1, '2025-07-23 00:00:00');
ALTER SEQUENCE refresh_tokens_id_seq RESTART WITH 4;

INSERT INTO boards (id, user_id, title)
VALUES (1, 1, 'work'),
(2, 1, 'home'),
(3, 2, 'meets');
ALTER SEQUENCE boards_id_seq RESTART WITH 4;

INSERT INTO tasks (id, title, body, task_priority, task_status, complete_before, complete_date, board_id, create_date, update_date)
VALUES (1, 'Мероприятие', 'Посетить мероприятие', 'MEDIUM', 'IN_PROGRESS', '2025-07-23 12:22:00', null, 1, '2025-07-22 00:00:00', '2025-07-22 00:00:00'),
(2, 'Тесты', 'Написать тесты', 'MEDIUM', 'COMPLETED', '2025-07-10 00:00:00', '2025-07-09 12:00:00', 1, '2025-07-05 00:00:00', '2025-07-09 12:00:00'),
(3, 'Купить сок', 'Купить апельсиновый сок', 'HIGH', 'IN_WAITING', '2025-07-23 00:00:00', null, 2, '2025-07-23 00:00:00', '2025-07-23 00:00:00');
ALTER SEQUENCE tasks_id_seq RESTART WITH 4;