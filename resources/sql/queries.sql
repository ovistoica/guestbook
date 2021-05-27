-- :name save-message! :<! :1
-- :doc creates a new message using the name message and author keys
INSERT INTO posts
(name, message)
VALUES (:author, :name, :message)
RETURNING *;

-- :name get-messages :? :*
-- :doc selects all available messages
SELECT * from posts;

-- :name create-user!* :! :n
-- :doc creates a new user with the provided login and hashed password
INSERT INTO users
(login, password)
VALUES (:login, :password);

-- :name get-user-for-auth* :? :1
-- :doc selects a user for authentication
SELECT * FROM users
WHERE login = :login;

-- :name get-messages-by-author :? :*
-- :doc selects all messages posted by a user SELECT * from posts
SELECT * FROM posts
WHERE author = :author;