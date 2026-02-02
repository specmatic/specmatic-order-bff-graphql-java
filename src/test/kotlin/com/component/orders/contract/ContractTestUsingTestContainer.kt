package com.component.orders.contract

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnabledIf(value = "isNonCIOrLinux", disabledReason = "Run only on Linux in CI; all platforms allowed locally")
class ContractTestsUsingTestContainer {
    companion object {

        private fun isCI(): Boolean = System.getenv("CI") == "true"
        private fun isNonCI(): Boolean = !isCI()
        private fun isLinux(): Boolean = System.getProperty("os.name").lowercase().contains("linux")

        @JvmStatic
        fun isNonCIOrLinux(): Boolean =
            isNonCI() || isLinux()

        fun isCIAndLinux(): Boolean =
           isCI() && isLinux()


        @Container
        private val mockContainer: GenericContainer<*> =
            GenericContainer("specmatic/enterprise")
                .withCommand("mock")
                .withHostUserIfRunningInCIAndLinux(isCIAndLinux())
                .withFileSystemBind(".", "/usr/src/app", BindMode.READ_WRITE)
                .withNetworkMode("host")
                .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
                .withLogConsumer { print(it.utf8String) }

        private val testContainer: GenericContainer<*> =
            GenericContainer("specmatic/enterprise")
                .withCommand("test")
                .withHostUserIfRunningInCIAndLinux(isCIAndLinux())
                .withCreateContainerCmdModifier { cmd -> cmd.withUser("1001:1001") }
                .withFileSystemBind(".", "/usr/src/app", BindMode.READ_WRITE)
                .withNetworkMode("host")
                .waitingFor(Wait.forLogMessage(".*Tests run:.*", 1))
                .withLogConsumer { print(it.utf8String) }

    }

    @Test
    fun specmaticContractTest() {
        testContainer.start()
        val hasSucceeded = testContainer.logs.contains("Failures: 0")
        assertThat(hasSucceeded).isTrue()
    }
}

private fun GenericContainer<*>.withHostUserIfRunningInCIAndLinux(
    isCIAndLinux: Boolean,
): GenericContainer<*> =
    this.withCreateContainerCmdModifier { cmd ->
        if (isCIAndLinux) {
            try {
                val uid = ProcessBuilder("id", "-u")
                    .redirectErrorStream(true)
                    .start()
                    .apply { waitFor() }
                    .inputStream.bufferedReader().readText().trim()

                val gid = ProcessBuilder("id", "-g")
                    .redirectErrorStream(true)
                    .start()
                    .apply { waitFor() }
                    .inputStream.bufferedReader().readText().trim()

                if (uid.isNotBlank() && gid.isNotBlank()) {
                    cmd.withUser("$uid:$gid")
                }
            } catch (e: Exception) {
                println("Failed to set container user: ${e.message}")
            }
        }
    }