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

You can find the compiled plugin in `build/libs/petals-0.1-all.jar`.

### Running the database

You'll need [Docker](https://www.docker.com/) to run your instance of a Redis store.

```sh
# Run Redis and allow Petals to interact with it throught the port 6379
docker run --name redis -p6379:6379 -d redis:alpine
```

You may also manually interact with Redis through Redis CLI, although I would not recommend doing so unless you know exactly what you're doing.

```sh
docker exec -it redis redis-cli
```

## For plugin developpers

### Depending on Petals

To use Petals in your plugins, you'll need to [compile the sources](#compiling-sources) and publish the Petals API to your local maven registry.

```sh
gradlew.bat publishToMavenLocal # Windows
./gradlew shadowJar # MacOS & Linux
```

You can now add the Petals API to your dependencies in `build.gradle`.

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

To allow the Petals plugin to recognize your plugin, your main class should implement the `Petal` interface. Here's an example:

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

### Declaring a Petals state

In Petals, you interact with the state of a Player/Game through an interface that maps keys in the object's state. You write the interface, and Petals implements it dynamically at runtime.

Here's an example of a custom state with Petals:

```java
import io.github.petals.state.*;

public interface MyState extends State {
    @Getter("blockBreakCount") int getBlockBreakCount(); // Equivalent: Integer.parseInt(this.raw().getOrDefault("blockBreakCount", "0"))
    @Setter("blockBreakCount") void setBlockBreakCount(int blockBreakCount); // Equivalent: this.raw().put("blockBreakCount", String.valueOf(blockBreakCount))
}
```

The Petals API makes interacting with states more concise and makes errors occur less often.

Please keep in mind:

- You should never instantiate a State.
- You should never implement a State.
- Every method in a State should be annotated.
- The `Getter`s and `Setter`s annotations only support the following types for now: `String`, `byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `? extends Enum`

Now that we wrote a custom State, let's see how we can interact with it using a simple Listener:

```java
public class MyListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Find role that inherits MyRole
        MyState state = Petals.database().player(e.getPlayer(), MyState.class).get();

        // Increase current count by one
        int currentCount = state.getBlockBreakCount() + 1;
        state.setBlockBreakCount(currentCount);

        // Send message to the player
        e.getPlayer().sendMessage("You broke " + currentCount + " blocks! Good job!");
    }
}
```

