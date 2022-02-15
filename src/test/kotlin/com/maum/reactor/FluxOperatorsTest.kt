package com.maum.reactor

import org.junit.jupiter.api.Test
import java.util.logging.Logger


class FluxOperatorsTest {

    private val client = createClient()

    private val log = Logger.getLogger(this.javaClass.name)

    private fun getTodos() = client.getTodos()
        .doOnSubscribe { log.info("getTodos() subscribed") }
        .doOnError { log.info("getTodos() error") }
        .doOnCancel { log.info("getTodos() cancel") }
        .doOnComplete { log.info("getTodos() completed") }

    private fun getUsers() = client.getUsers()
        .doOnSubscribe { log.info("getUsers() subscribed") }
        .doOnError { log.info("getUsers() error") }
        .doOnCancel { log.info("getUsers() cancel") }
        .doOnComplete { log.info("getUsers() completed") }


    /*
    concatMap - does not let values from different inners interleave. Ensure order
     */
    @Test
    fun `prefer concatMap over flatMap if you don’t strictly need parallel execution`() {
        val user = getTodos()
            .concatMap { getUsers() }.blockFirst()
    }

    /*
    switchMap - emit element till emitting another one
     */
    @Test
    fun `switchMap emit till next element`() {
        val user = getTodos()
            .switchMap { getUsers() }.blockFirst()
    }

    /*
    flatMapSequential - emit element till emitting another one
     */
    @Test
    fun `flatMapSequential work like concatMap giving order and flatMap enable parallel execution`() {
        val user = getTodos()
            .flatMapSequential { getUsers() }.blockFirst()
    }


}