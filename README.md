# Guestbook

A twitter like message board application to help me understand the more advanced parts of Clojure(Script)

[![MIT License](https://img.shields.io/apm/l/atomic-design-ui.svg?)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)
[![Twitter](https://img.shields.io/twitter/url?style=social&url=https%3A%2F%2Fgithub.com%2Fstoica94%2Fguestbook)](https://twitter.com/intent/tweet?text=Wow:&url=https%3A%2F%2Fgithub.com%2Fstoica94%2Fguestbook)

## Tech Stack

**Client:** ClojureScript, Re-Frame, Reagent, BulmaCSS,

**Server:** Clojure, PostgreSQL, Mount

## Features

- Live feed updates w websocket
- Image storage in db as blob
- Fully costumizable profile
- Support for comments & replies

## Run Locally

Clone the project

```bash
  git clone https://github.com/stoica94/guestbook
```

Go to the project directory

```bash
  cd guestbook
```

Install dependencies

```bash
  npm install
```

Start the repl

```bash
  lein repl
```

Call start function from `user` namespace

```bash
  user:> (start)
```

Build shadow-cljs in a separate terminal window

```bash
  npx shadow-cljs watch app
```

That's it!

## API Reference

All API routes are documented with swagger

#### Swagger route

```http
  GET /api/swagger-ui/
```

## Acknowledgements

This project is made based on the book [Web Development in Clojure](https://pragprog.com/titles/dswdcloj3/web-development-with-clojure-third-edition/)

## Feedback

If you have any feedback, please reach out to me at ovidiu.stoica1094@gmail.com
