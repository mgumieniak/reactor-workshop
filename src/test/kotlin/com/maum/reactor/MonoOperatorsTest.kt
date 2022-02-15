package com.maum.reactor

import feign.FeignException
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.Duration
import java.util.logging.Logger


class MonoOperatorsTest {

    private val client = createClient()

    private val log = Logger.getLogger(this.javaClass.name)

    private fun getTodo(id: String) = client.getTodo(id)
            .doOnSubscribe { log.info("getTodo($id) subscribed") }
            .doOnError { log.info("getTodo($id) error") }
            .doOnCancel { log.info("getTodo($id) cancel") }
            .doOnSuccess { log.info("getTodo($id) success") }


    /*
    zipWhen - emit element from first call, then start process another
     */
    @Test
    fun `how zipWhen work`() {
        val todos = getTodo("1")
                .zipWhen { getTodo("12d") }

        todos.test()
                .expectError(FeignException.NotFound::class.java)
                .verify()
    }

    /*
    zipWith - run both query at the same time. An error or empty completion of any source will cause the other source
    to be cancelled and the resulting Mono to immediately error or complete, respectively.
     */
    @Test
    fun `how zipWith work`() {
        val todos = getTodo("1")
                .zipWith(getTodo("12d"))

        todos.test()
                .expectError(FeignException.NotFound::class.java)
                .verify()
    }

    /*
    and - join and return void
     */
    @Test
    fun `how and work`() {
        val strings = Mono.fromCallable { log.info("exec first!") }
                .and(Mono.fromCallable { log.info("exec second!") })

        strings.test()
                .expectComplete()
                .verify()
    }

    /*
    concatWith - subscribe the second one is after first success. Get Flux (no interleave)
     */
    @Test
    fun `how concatWith work`() {
        val todos: Flux<Todo> = getTodo("1").delayElement(Duration.ofSeconds(1))
                .concatWith(getTodo("12"))

        todos.test()
                .expectNext(Todo(id = 1, userId = 1, title = "delectus aut autem", completed = false))
                .expectNext(Todo(id = 12, userId = 1, title = "ipsa repellendus fugit nisi", completed = true))
                .verifyComplete()
    }

    /*
    thenReturn - let this Mono complete successfully, then emit the provided value. On an error, the error signal is propagated instead.
     */
    @Test
    fun `how thenReturn work`() {
        val todos = getTodo("1")
                .thenReturn("OK")

        val todosError = getTodo("12d")
                .thenReturn("OK")


        todos.test()
                .expectNext("OK")
                .verifyComplete()

        todosError.test()
                .expectError(FeignException.NotFound::class.java)
                .verify()
    }

    @Test
    fun `flatMapMany vs flatMap`() {
        val todos: Flux<Todo> = getTodo("1")
                .flatMapMany { todo -> getTodo("${todo.id}2") } // return Flux

        val todo: Mono<Todo> = getTodo("1")
                .flatMap { todo -> getTodo("${todo.id}2") } // return Mono
    }

    /*
    mergeWith - merge publishers (interleave). Get Flux
     */
    @Test
    fun `how mergeWith work`() {
        val todos: Flux<Todo> = getTodo("1")
                .mergeWith(getTodo("2"))

        todos.test()
                .expectNext(Todo(id = 1, userId = 1, title = "delectus aut autem", completed = false))
                .expectNext(Todo(id = 2, userId = 1, title = "quis ut nam facilis et officia qui", completed = false))
                .verifyComplete()
    }

    @Test
    fun `how mapNotNull work`() {
        val todos = getTodo("1")
                .mapNotNull { } // filer(!null) + map(...)
    }

    /*
    delayUntil - accept arguments and return
     */
    @Test
    fun `delayUntil vs flatMap`() {

        Mono.fromCallable { Mono.just(1) }
                .delayUntil { number -> Mono.fromCallable { log.info("Consumer argument: $number") } }
                .flatMap { theSameNumber -> Mono.fromCallable { log.info("Consumer argument: $theSameNumber") }.thenReturn(theSameNumber) }
                .block()
    }
}