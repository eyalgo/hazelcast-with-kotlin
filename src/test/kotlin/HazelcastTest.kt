import com.github.blindpirate.extensions.CaptureSystemOutput
import com.github.blindpirate.extensions.CaptureSystemOutput.OutputCapture
import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.core.Hazelcast.newHazelcastInstance
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import eyalgo.hazelcast.ElementsMapListener
import eyalgo.model.Element
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
    // TODO check the documentation regarding serialisation (avoiding it)
    private lateinit var map: IMap<Key, Element>

    @BeforeEach
    fun setUp() {
        val mapConfig = MapConfig(mapName)
        val config = Config()
            .addMapConfig(mapConfig)
        hazelcast = newHazelcastInstance(config)

        map = hazelcast.getMap(mapName)
        map.addEntryListener(ElementsMapListener(), false)
    }

    @AfterEach
    fun tearDown() {
        hazelcast.shutdown()
    }

    @BeforeAll
    fun disableHazelcastLogging() {
        // See [logging configuration](https://docs.hazelcast.com/imdg/4.2/clusters/logging-configuration)
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
    @CaptureSystemOutput
    fun `entry is evacuated by TTL`(output: OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 200, MILLISECONDS, 600, MILLISECONDS)
        Thread.sleep(250)
        map[key] shouldBe null

        output.expect(containsString("$key is expired"))
    }

    @Test
    @CaptureSystemOutput
    fun `entry is evacuated by Max Idle`(output: OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 600, MILLISECONDS, 200, MILLISECONDS)
        Thread.sleep(250)
        map[key] shouldBe null

        output.expect(containsString("$key is expired"))
    }

    @Test
    @CaptureSystemOutput
    fun `an action happens when an element is evicted`(output: OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 400, MILLISECONDS, 600, MILLISECONDS)
        Thread.sleep(250)
        map.evict(key)

        output.expect(containsString("$key is evicted"))
    }

    @Test
    @CaptureSystemOutput
    fun `an action happens when an element is deleted`(output: OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 400, MILLISECONDS, 600, MILLISECONDS)
        Thread.sleep(250)
        map.delete(key)

        output.expect(containsString("$key is expired"))
    }

    @Test
    @CaptureSystemOutput
    @Disabled("WIP - need to investigate")
    fun `an action happens when an element is removed`(output: OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 400, MILLISECONDS, 600, MILLISECONDS)
        Thread.sleep(250)
        map.remove(key)

        output.expect(containsString("$key is removed"))
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

    private fun randomKey(): Key = Key(randomString(), Random.nextInt())
    private fun randomValue(): Element = Element(randomString())
    private fun randomString(): String = randomUUID().toString()
}
