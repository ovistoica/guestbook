-- START:posts
-- START:save-message!
-- :name save-message! :<! :1
-- :doc creates a new message using the name and message keys
INSERT INTO posts
(author, name, message)
VALUES (:author, :name, :message)
RETURNING *;
-- END:save-message!

-- START:get-messages
-- :name get-messages :? :*
-- :doc selects all available messages
SELECT
  p.id                 as id,
  p.timestamp          as timestamp,
  p.message            as message,
  p.name               as name,
  p.author             as author,
  a.profile->>'avatar' as avatar
from posts as p join users as a
on a.login = p.author
-- END:get-messages
-- END:posts

-- START:author
-- :name get-messages-by-author :? :*
-- :doc selects all messages posted by a user
SELECT
  p.id                 as id,
  p.timestamp          as timestamp,
  p.message            as message,
  p.name               as name,
  p.author             as author,
  a.profile->>'avatar' as avatar
from posts as p join users as a
on a.login = p.author
WHERE author = :author
--END:author

-- START:users
-- :name create-user!* :! :n
-- :doc creates a new user with the provided login and hashed password
INSERT INTO users
(login, password)
VALUES (:login, :password)

-- START:password
-- :name set-password-for-user!* :! :n
UPDATE users
SET password = :password
where login = :login
-- END:password

-- START:delete
-- :name delete-user!* :! :n
DELETE FROM users
where login = :login
-- END:delete

-- :name get-user-for-auth* :? :1
-- :doc selects a user for authentication
SELECT * FROM users
WHERE login = :login
-- END:users

-- START:profile
-- :name set-profile-for-user*  :<! :1
-- :doc sets a profile map for the specified user
UPDATE users
SET profile = :profile
where :login = login
RETURNING *;

-- :name get-user* :? :1
-- :doc gets a user's publicly available information
SELECT login, created_at, profile from users
WHERE login = :login
-- END:profile

-- START:media
-- :name save-file! :! :n
-- saves a file to the database
INSERT INTO media
(name, type, owner, data)
VALUES (:name, :type, :owner, :data)
ON CONFLICT (name) DO UPDATE
SET type = :type,
    data = :data
WHERE media.owner = :owner

-- :name get-file :? :1
-- Gets a file from the database
select * from media
where name = :name
-- END:media