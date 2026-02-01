package com.component.orders.contract

import io.specmatic.graphql.test.SpecmaticGraphQLContractTest
import io.specmatic.stub.ContractStub
import io.specmatic.stub.createStub
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT

@SpringBootTest(webEnvironment = DEFINED_PORT)
class ContractTests : SpecmaticGraphQLContractTest {
    companion object {
        private var httpStub: ContractStub? = null

        @JvmStatic
        @BeforeAll
        fun setUp() {
            // Start Specmatic Http Stub and set the expectations
            httpStub = createStub()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            // Shutdown Specmatic Http Stub
            httpStub?.close()
        }
    }
}
