package io.cloudflight.gradle.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

@Api("Foo")
public interface FooApi {

    @GetMapping("/overview")
    public String getOverview();
}