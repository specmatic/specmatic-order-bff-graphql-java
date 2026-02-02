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

        @JvmStatic
        fun isNonCIOrLinux(): Boolean =
            System.getenv("CI") != "true" || System.getProperty("os.name").lowercase().contains("linux")

        fun isCIAndLinux(): Boolean =
            System.getenv("CI") == "true" && System.getProperty("os.name").lowercase().contains("linux")

        @Container
        private val mockContainer: GenericContainer<*> =
            GenericContainer("specmatic/enterprise")
                .withCommand("mock")
                .withCreateContainerCmdModifier { cmd ->
                    if (isCIAndLinux()) {
                        try {
                            val uid = ProcessBuilder("id", "-u").start().inputStream.bufferedReader().readText().trim()
                            val gid = ProcessBuilder("id", "-g").start().inputStream.bufferedReader().readText().trim()
                            cmd.withUser("$uid:$gid")
                        } catch (e: Exception) {
                            println("Failed to set container user: ${e.message}")
                        }
                    }
                }
                .withFileSystemBind(".", "/usr/src/app", BindMode.READ_WRITE)
                .withNetworkMode("host")
                .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
                .withLogConsumer { print(it.utf8String) }

        private val testContainer: GenericContainer<*> =
            GenericContainer("specmatic/enterprise")
                .withCommand("test")
                .withCreateContainerCmdModifier { cmd ->
                    if (isCIAndLinux()) {
                        try {
                            val uid = ProcessBuilder("id", "-u").start().inputStream.bufferedReader().readText().trim()
                            val gid = ProcessBuilder("id", "-g").start().inputStream.bufferedReader().readText().trim()
                            cmd.withUser("$uid:$gid")
                        } catch (e: Exception) {
                            println("Failed to set container user: ${e.message}")
                        }
                    }
                }
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