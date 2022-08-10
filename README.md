# Petals

A lovely framework for UHC plugin developpers

## What does it do

Petals stores games, users and worlds for UHC games in a dedicated Redis environment and provides a fresh API to interact with all that stuff without a hassle.

## Running the database

You'll need [Docker](https://www.docker.com/) to run your instance of a Redis store.

```sh
# Run Redis and allow Petals to interact with it throught the port 6379
docker run --name redis -p6379:6379 -d redis:alpine
```

You may also manually interact with Redis through Redis CLI, although I would not recommend doing so unless you exacly know what you're doing.

```sh
docker exec -it redis redis-cli
```

