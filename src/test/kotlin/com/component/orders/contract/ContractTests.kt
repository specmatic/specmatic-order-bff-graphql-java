package com.component.orders.contract

import io.specmatic.enterprise.SpecmaticContractTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT

@SpringBootTest(webEnvironment = DEFINED_PORT)
class ContractTests : SpecmaticContractTest()
