package com.maum.reactor

import feign.Param
import feign.RequestLine
import reactivefeign.java11.Java11ReactiveFeign
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

data class Todo(val id: Long, val userId: Long, val title: String, val completed: Boolean)

data class Company(val name: String, val catchPhrase: String, val bs: String)

data class Geo(val lat: String, val lng: String)

data class Address(val street: String, val suite: String, val city: String, val zipcode: String, val geo: Geo)

data class User(
    val id: Long,
    val name: String,
    val username: String,
    val email: String,
    val address: Address,
    val phone: String,
    val website: String,
    val company: Company
)

interface TodoApi {

    @RequestLine("GET /todos/{todoId}")
    fun getTodo(@Param("todoId") todoId: String): Mono<Todo>

    @RequestLine("GET /todos")
    fun getTodos(): Flux<Todo>

    @RequestLine("GET /users")
    fun getUsers(): Flux<User>
}

fun createClient(): TodoApi = Java11ReactiveFeign
    .builder<TodoApi>()
    .target(TodoApi::class.java, "https://jsonplaceholder.typicode.com")




