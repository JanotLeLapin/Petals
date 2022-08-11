# Petals

Un framework agréable pour les développeurs de plugins UHC

## À quoi ça sert

Petals stocke les parties, utilisateurs et mondes des parties UHC dans un environnement Redis dédié et offre un API frais pour interagir avec tout ça dans difficulté.

## Pour les admins

### Compiler le code source

Si votre serveur Minecraft utilise un plugin qui dépend de Petals, vous aurez besoin de compiler le code source et de placer le plugin Petals dans le dossier `plugins` de votre server.

```sh
gradlew.bat shadowJar # Windows
./gradlew shadowJar # MacOS & Linux
```

Le plugin compilé se trouve dans `build/libs/petals-0.1-all.jar`.

### Lancer la base de données

Vous aurez besoin de [Docker](https://www.docker.com/) pour lancer une base de données Redis.

```sh
# Lancer Redis et permettre à Petals d'interagir avec à travers le port 6379
docker run --name redis -p6379:6379 -d redis:alpine
```

Vous pouvez aussi manuellement interagir avec Redis à travers Redis CLI, cependant je ne recommenderais pas de faire ça à moins que vous sachiez exactement ce que vous faites.

```sh
docker exec -it redis redis-cli
```

## Pour les devs

### Dépendre de Petals

Pour utiliser Petals dans vos plugins, vous devez [compiler le code source](#compiler-le-code-source) puis publier l'API Petals sur votre répertoire local Maven.

```sh
gradlew.bar publishToMavenLocal # Windows
./gradlew shadowJar # MacOS & Linux
```

Vous pouvez maintenant ajouter l'API Petals à vos dépendances dans `build.gradle`.

```groovy
repositories {
    mavenLocal()
}

dependencies {
    implementation 'io.github.petals:petals-api:0.1'
}
```

Vous devez aussi ajouter le plugin Petals en dépendance à `plugin.yml`:

```yml
depend: [Petals]
```

### Déclarer un plugin Petals

Pour permettre au plugin Petals de reconnaître votre plugin, votre classe principale doit implémenter l'interface `Petal`. Voilà un exemple de classe principale pour un plugin Petals:

```java
import org.bukkit.plugin.java.JavaPlugin;

import io.github.petals.Petal;

public class MyMainClass extends JavaPlugin implements Petal {
    @Override
    public void onCreateGame(Game game) {
        // Cette fonction sera exécutée quand votre plugin crée une partie
    }

    @Override
    public void onStartGame(Game game) {
        // Cette fonction sera exécutée quand votre plugin démarre une partie
    }

    // Vous devez implémenter toutes les autres méthodes définies dans l'interface Petal
}
```
