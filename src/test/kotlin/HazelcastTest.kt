import com.github.blindpirate.extensions.CaptureSystemOutput
import com.github.blindpirate.extensions.CaptureSystemOutput.OutputCapture
import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.core.Hazelcast.newHazelcastInstance
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import eyalgo.hazelcast.ElementsMapListener
import eyalgo.model.Key
import io.kotest.matchers.shouldBe
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.UUID
import java.util.UUID.randomUUID
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.random.Random

@TestInstance(PER_CLASS)
class HazelcastTest {
    private val mapName = "elements"
    private lateinit var hazelcast: HazelcastInstance

    // TODO modify the value to list of elements (add tests)
    private lateinit var map: IMap<Key, UUID>

    @BeforeEach
    fun setUp() {
        val mapConfig = MapConfig(mapName)
        val config = Config()
            .addMapConfig(mapConfig)
        hazelcast = newHazelcastInstance(config)

        map = hazelcast.getMap(mapName)
    }

    @AfterEach
    fun tearDown() {
        hazelcast.shutdown()
    }

    @BeforeAll
    fun disableHazelcastLogging() {
        System.setProperty("hazelcast.logging.type", "none")
    }

    @Test
    fun `only one entry in the time frame`() {
        val key = randomKey()
        val value = randomValue()
        map.put(key, value, 1, MINUTES, 2, MINUTES)
        map[key] shouldBe value
    }

    @Test
    fun `entry is evacuated by TTL`() {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 200, MILLISECONDS, 600, MILLISECONDS)
        Thread.sleep(250)
        map[key] shouldBe null
    }

    @Test
    fun `entry is evacuated by Max Idle`() {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 600, MILLISECONDS, 200, MILLISECONDS)
        Thread.sleep(250)
        map[key] shouldBe null
    }

    @Test
    @CaptureSystemOutput
    fun `an action happens when an element is evicted`(output: OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 100, MILLISECONDS, 200, MILLISECONDS)
        map.addEntryListener(ElementsMapListener(), true)
        Thread.sleep(250)
        map.evict(key)

        output.expect(containsString("$key is evicted"))
    }

    @Test
    @Disabled("WIP - tests for the eviction ttl")
    fun `Max Idle is updated `() {
        val key = randomKey()
        val value = randomValue()

//        map.put(key, value, 500, MINUTES, 600, MILLISECONDS)
        map.put(key, value, 1, MINUTES, 500, SECONDS)
//        Thread.sleep(100)
        map[key] shouldBe value

        Thread.sleep(150)
        map[key] shouldBe value
        Thread.sleep(220)
        map[key] shouldBe null
    }

    private fun randomKey(): Key = Key(randomUUID().toString(), Random.nextInt())
    private fun randomValue(): UUID = randomUUID()
}
