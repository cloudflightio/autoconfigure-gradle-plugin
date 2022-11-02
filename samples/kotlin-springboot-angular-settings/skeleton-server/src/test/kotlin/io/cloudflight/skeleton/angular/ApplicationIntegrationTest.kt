package io.cloudflight.skeleton.angular

import io.cloudflight.platform.spring.context.ApplicationContextProfiles
import io.cloudflight.platform.spring.test.openfeign.FeignTestClientFactory
import io.cloudflight.skeleton.angular.api.HelloWorldApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(ApplicationContextProfiles.TEST)
class ApplicationIntegrationTest(
    @Autowired @LocalServerPort private val port: Int
) {

    private val helloWorldApi = FeignTestClientFactory.createClientApi(HelloWorldApi::class.java, port)

    @Test
    fun helloWorld() {
        assertThat(helloWorldApi.getHello().casual).isEqualTo("Hello ;)")
    }

}
