package io.cloudflight.skeleton.angular.web

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class LicenseNoticeController {

    private val notice = this.javaClass.classLoader
        .getResources("META-INF/NOTICE.html").toList()
        .first { it.path.contains("skeleton-server") }
        .readText()

    @ResponseBody
    @GetMapping("/notice", produces = [MediaType.TEXT_HTML_VALUE])
    fun renderNoticeMessage() = notice
}
