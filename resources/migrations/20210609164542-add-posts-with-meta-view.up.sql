CREATE OR REPLACE VIEW posts_with_meta AS
SELECT  p.id                 AS id
       ,p.timestamp          AS timestamp
       ,p.message            AS message
       ,p.name               AS name
       ,p.author             AS author
       ,a.profile->>'avatar' AS avatar
       ,COUNT(b.user_id)     AS boosts
FROM posts AS p
LEFT JOIN users AS a
ON a.login = p.author
LEFT JOIN boosts AS b
ON p.id = b.post_id
GROUP BY  p.id
         ,a.login
--;;
CREATE OR REPLACE VIEW posts_and_boosts AS
SELECT  p.id                                    AS id
       ,p.timestamp                             AS timestamp
       ,p.message                               AS message
       ,p.name                                  AS name
       ,p.author                                AS author
       ,p.avatar                                AS avatar
       ,p.boosts                                AS boosts
       ,b.post_id is not null                   AS is_boost
       ,coalesce(b.timestamp,p.timestamp)       AS posted_at
       ,coalesce(b.user_id,p.author)            AS poster
       ,coalesce(u.profile->>'avatar',p.avatar) AS poster_avatar
       ,coalesce(b.poster,p.author)             AS source
       ,coalesce(s.profile->>'avatar',p.avatar) AS source_avatar
FROM posts_with_meta AS p
LEFT JOIN boosts AS b
ON b.post_id = p.id
LEFT JOIN users AS u
ON b.user_id = u.login
LEFT JOIN users AS s
ON b.poster = s.login