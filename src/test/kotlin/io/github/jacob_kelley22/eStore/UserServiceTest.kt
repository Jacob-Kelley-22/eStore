package io.github.jacob_kelley22.eStore

import io.github.jacob_kelley22.eStore.entity.User
import io.github.jacob_kelley22.eStore.repository.UserRepository
import io.github.jacob_kelley22.eStore.service.UserService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import kotlin.test.assertEquals

@ActiveProfiles("test")
@SpringBootTest
class UserServiceTest {

    private val userRepo : UserRepository = mock()
    private val userService = UserService(
        userRepo,
        passwordEncoder = mock()
    )

    @Test
    fun shouldReturnAllUsers() {
        val user = User(
            id = 1,
            email = "test@test.com",
            password = "test",
        )

        whenever(userRepo.findAll()).thenReturn(listOf(user))

        val result = userService.findAllUsers()

        print(result.size)
        assertEquals(1, result.size)
        assertEquals(user.email, result[0].email)
    }


}