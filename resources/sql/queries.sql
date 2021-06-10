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
SELECT  *
FROM posts_with_meta
-- END:get-messages

-- START:get-message
-- :name get-message :? :1
-- :doc selects a message
SELECT  *
FROM posts_with_meta
WHERE p.id = :id 
-- END:get-message
-- END:posts

-- START:author
-- :name get-messages-by-author :? :*
-- :doc selects all messages posted by a user
SELECT  *
FROM posts_with_meta
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
WHERE login = :login 
-- END:password

-- START:delete
-- :name delete-user!* :! :n
 DELETE
FROM users
WHERE login = :login 
-- END:delete

-- :name get-user-for-auth* :? :1
-- :doc selects a user for authentication
SELECT  *
FROM users
WHERE login = :login  
-- END:users

-- START:profile
-- :name set-profile-for-user*  :<! :1
-- :doc sets a profile map for the specified user
 UPDATE users
SET profile = :profile
WHERE :login = login RETURNING *;  

-- :name get-user* :? :1
-- :doc gets a user's publicly available information
SELECT  login
       ,created_at
       ,profile
FROM users
WHERE login = :login 
-- END:profile

-- START:media
-- :name save-file! :! :n
-- saves a file to the database
INSERT INTO media (name, type, owner, data) 
VALUES (:name, :type, :owner, :data)
ON CONFLICT (name) DO UPDATE

SET type = :type, data = :data
WHERE media.owner = :owner 

-- :name get-file :? :1
-- Gets a file from the database
SELECT  *
FROM media
WHERE name = :name 
-- END:media

-- :name boost-post! :! :n
-- :doc Boosts a post or moves a post to the top of a users timeline
INSERT INTO boosts (user_id, post_id, poster) 
VALUES (:user, :post, nullif(:poster, :user))
ON CONFLICT (user_id, post_id) DO UPDATE

SET timestamp = now()
WHERE boosts.user_id = :user 
AND boosts.post_id = :post  

-- :name boosters-of-post :? :*
-- :doc Get all boosters of a post
SELECT  user_id AS user
FROM boosts
WHERE post_id = :post 

-- :name get-reboosts :? :*
-- :doc Gets all boosts descended from a given boost
 WITH RECURSIVE reboosts AS (WITH post_boosts AS (
SELECT  user_id
       ,poster
FROM boosts
WHERE post_id = :post) 
SELECT  user_id
       ,poster
FROM post_boosts
WHERE user_id = :user UNION 
SELECT  b.user_id
       ,b.poster
FROM post_boosts b
INNER JOIN reboosts r
ON r.user_id = b.poster)
SELECT  user_id AS user
       ,poster  AS source
FROM reboosts

-- :name get-boost-chain :? :*
-- :doc Get all boosts above the original boost
 WITH RECURSIVE reboosts AS (WITH post_boosts AS (
SELECT  user_id
       ,poster
FROM boosts
WHERE post_id = :post) 
SELECT  user_id
       ,poster
FROM post_boosts WHERRE user_id = :user UNION
SELECT  b.user_id
       ,b.poster
FROM post_boosts b
INNER JOIN reboosts
ON r.poster = b.user_id)
SELECT  user_id AS user
       ,poster  AS source
FROM reboosts

-- :name get-timeline :? :*
-- :doc Gets the latest post or boost for each post
SELECT  *
FROM 
(
	SELECT  distinct
	ON (p.id) *
	FROM posts_and_boosts AS p
	ORDER BY p.id, p.posted_at desc
) AS t
ORDER BY t.posted_at asc

-- :name get-timeline-for-poster :? :*
-- :doc Gets the latest post or boost for each post
SELECT  *
FROM 
(
	SELECT  distinct
	ON (p.id) *
	FROM posts_and_boosts AS p
	ORDER BY p.id, p.posted_at desc
) AS t
ORDER BY t.posted_at asc

-- :name get-timeline-post :? :1
-- :doc Gets the boosted post for updating timelines
SELECT  *
FROM posts_and_boosts
WHERE is_boost = :is_boost 
AND poster = :user 
AND id = :post
ORDER BY posted_at asc 
LIMIT 1