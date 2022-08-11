# Petals

A lovely framework for UHC plugin developpers

## What does it do

Petals stores games, users and worlds for UHC games in a dedicated Redis environment and provides a fresh API to interact with all that stuff without a hassle.

## For server admins

### Compiling sources

If your Minecraft server uses a plugin that depends on Petals, you'll need to compile the source code and place the Petals plugin in your server's `plugins` directory.

```sh
gradlew.bat shadowJar # Windows
./gradlew shadowJar # MacOS & Linux
```

### Running the database

You'll need [Docker](https://www.docker.com/) to run your instance of a Redis store.

```sh
# Run Redis and allow Petals to interact with it throught the port 6379
docker run --name redis -p6379:6379 -d redis:alpine
```

You may also manually interact with Redis through Redis CLI, although I would not recommend doing so unless you exacly know what you're doing.

```sh
docker exec -it redis redis-cli
```

## For plugin developpers

### Depending on Petals

To use Petals in your plugins, you'll need to [compile the sources](#compiling-sources) and publish the Petals API to your local maven registry.

```sh
gradlew.bar publishToMavenLocal # Windows
./gradlew shadowJar # MacOS & Linux
```

You can now add the Petals API to your `build.gradle` dependencies.

```groovy
repositories {
    mavenLocal()
}

dependencies {
    implementation 'io.github.petals:petals-api:0.1'
}
```

You should also add the Petals plugin as a dependency to your `plugin.yml`:

```yml
depend: [Petals]
```

### Declaring a Petals plugin

To allow the Petals plugin to recognize your plugin, your main class should implement the `Petal` interface. Here's an example main class for a Petals plugin:

```java
import org.bukkit.plugin.java.JavaPlugin;

import io.github.petals.Petal;

public class MyMainClass extends JavaPlugin implements Petal {
    @Override
    public void onCreateGame(Game game) {
        // This will run when your plugin creates a game
    }

    @Override
    public void onStartGame(Game game) {
        // This will run when your plugin starts a game
    }

    // You should implement every method defined in the Petal interface
}
```

