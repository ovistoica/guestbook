# Changes

## Base - `guestbook-base/`

Created project from:
`lein new luminus guestbook --template-version 3.91 -- +h2 +http-kit`

Some small changes, but more or less brand new luminus.
`.gitignore` updated to include generated files from later branches and other environment specific files, but include config files for purposes of including in the book.

## Guestbook - `guestbook/`

Write migration for guestbook table

Write markup for messages homepage

Hydrate markup from DB

Write some queries to create messages

Write a POST endpoint for saving messages

write some dummy tests
