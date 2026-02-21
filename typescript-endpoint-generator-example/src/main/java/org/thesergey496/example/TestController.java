package org.thesergey496.example;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thesergey496.annotations.TypescriptService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@TypescriptService
@RestController
@RequestMapping("/test/zxczxc/")
public class TestController<U> {
    @GetMapping("method1/{notSoSimple}")
    public <T> String method1(@PathVariable("notSoSimple") int a,
                              @RequestParam(name = "notSoSimple2", required = false) boolean a1,
                              @RequestParam() double[] a2,
                              @RequestParam("notSoSimple3") Optional<Integer> b,
                              @RequestParam() Long c,
                              @RequestParam(defaultValue = "'qwe'") String d,
                              @RequestParam(defaultValue = "1.23") BigDecimal e,
                              @RequestParam() List<Integer> f,
                              @RequestParam() List<List<Integer>> f1,
                              @RequestParam() Map<String, Long> g,
                              @RequestParam() Class1 h,
                              @RequestParam() Class2<U, T> h1,
                              @RequestParam() Class2<U, Class2<Class1, T>> h2,
                              @RequestParam() Set<T> i,
                              @RequestParam() TestId j,
                              @RequestParam() TestId2 k,
                              @RequestParam() Void v,
                              @RequestBody() U u) {
        return "jopa";
    }

    @PostMapping(value = {"method2", "METHOD2"})
    public Class2<Class1, Class2<Integer, String>> method2() {
        return new Class2<>();
    }

    @PostMapping("res")
    public ResponseEntity<Resource> method3() {
        return null;
    }
}
