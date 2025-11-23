package com.allride

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BulkUserImportApplication

fun main(args: Array<String>) {
    runApplication<BulkUserImportApplication>(*args)
}

