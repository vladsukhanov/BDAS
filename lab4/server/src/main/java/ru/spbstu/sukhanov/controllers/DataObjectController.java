package ru.spbstu.sukhanov.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.spbstu.sukhanov.dto.DataObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

@RestController
public class DataObjectController {

    private static final String TEST_HEADER = "Test";

    @GetMapping("/data/{id}")
    public DataObject findById(@PathVariable final long id,
                               final HttpServletRequest req,
                               final HttpServletResponse res) {
        if (req.getHeader(TEST_HEADER) != null) {
            res.addHeader(TEST_HEADER, req.getHeader(TEST_HEADER));
        }
        return new DataObject(id, randomAlphabetic(4));
    }
}
