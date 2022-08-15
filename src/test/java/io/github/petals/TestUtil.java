package io.github.petals;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.*;

@Testcontainers
public class TestUtil {
    protected PetalsDatabase db;
    // Main Petals plugin
    protected PetalsPlugin petals;
    // Some plugin depending on Petals
    protected Petal plugin;

    // Bukkit mock
    protected MockedStatic<Bukkit> bukkit;

    @Container
    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

    @BeforeEach
    public void setUp() {
        this.db = new PetalsDatabase(redis.getHost(), redis.getFirstMappedPort());

        this.petals = mock(PetalsPlugin.class);
        when(petals.database()).thenReturn(db);

        this.plugin = mock(Petal.class);
        when(plugin.getName()).thenReturn("Plugin");

        PluginManager pm = mock(PluginManager.class);
        when(pm.getPlugin(eq("Petals"))).thenReturn(this.petals);
        when(pm.getPlugin(eq("Plugin"))).thenReturn(this.plugin);

        this.bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getPluginManager).thenReturn(pm);
    }

    @AfterEach
    public void cleanUp() {
        this.bukkit.close();
    }
}

