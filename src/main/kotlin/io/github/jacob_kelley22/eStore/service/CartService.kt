package io.github.jacob_kelley22.eStore.service

import io.github.jacob_kelley22.eStore.dto.cart.AddCartItemRequestDTO
import io.github.jacob_kelley22.eStore.dto.cart.CartResponseDTO
import io.github.jacob_kelley22.eStore.entity.Cart
import io.github.jacob_kelley22.eStore.entity.CartItem
import io.github.jacob_kelley22.eStore.entity.User
import io.github.jacob_kelley22.eStore.exception.BadRequestException
import io.github.jacob_kelley22.eStore.exception.ResourceNotFoundException
import io.github.jacob_kelley22.eStore.mapper.toDTO
import io.github.jacob_kelley22.eStore.repository.CartItemRepository
import io.github.jacob_kelley22.eStore.repository.CartRepository
import io.github.jacob_kelley22.eStore.repository.ProductRepository
import io.github.jacob_kelley22.eStore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import jakarta.transaction.Transactional

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) {

    private val logger = LoggerFactory.getLogger(CartService::class.java)

    fun getCartByUserId(userId: Long): CartResponseDTO {
        val user = userRepository.findById(userId)
            .orElseThrow {
                logger.warn("User $userId not found while fetching cart")
                ResourceNotFoundException("User $userId not found")
            }

        val cart = cartRepository.findByUser(user)
            .orElseGet {
                cartRepository.save(Cart(user = user))
            }

        return cart.toDTO()
    }

    @Transactional
    fun addItemToCart(
        user: User,
        productId: Long,
        requestQuantity: Int
    ): CartResponseDTO {

        logger.info("Adding product: {} with quantity: {} to cart of user: {}", productId, user, requestQuantity)

        val product = productRepository.findById(productId)
        .orElseThrow {
            logger.warn("Product with id {} not found while adding to cart for user {}", productId, user)
            ResourceNotFoundException("Product $productId not found")
        }

        if(requestQuantity > product.stockQuantity) {
            logger.warn("Requested quantity of product: {} exceeds stock of product: {}" +
                    " for user {}", requestQuantity, productId, user)
            throw BadRequestException("Requested quantity exceeds current stock")
        }

        val cart = cartRepository.findByUser(user)
            .orElseGet {
                logger.info("No cart found for user: {}. Creating one now", user)
                cartRepository.save(Cart(user = user))
            }

        val existingItem = cartItemRepository.findByCartAndProduct(cart, product)

        if (existingItem.isPresent) {
            val item = existingItem.get()
            val newQuantity = item.quantity + requestQuantity

            if(newQuantity > product.stockQuantity) {
                logger.warn(
                    "Updated quantity: {} of product: {} for user: {} exceeds current stock: {}",
                    newQuantity, product.id, user.email, product.stockQuantity
                )
                throw BadRequestException("Requested quantity exceeds current stock")
            }

            item.quantity = newQuantity
            cartItemRepository.save(item)
            logger.info("Updated cart item for user {}. Product: {}, Quantity: {}",
                user.email, productId, newQuantity)
        } else {
            val item = CartItem(
                cart = cart,
                product = product,
                quantity = requestQuantity
            )
            cart.items.add(item)
            cartItemRepository.save(item)
            logger.info("Created cart item for user {}. Product: {}, Quantity: {}",
                user.email, productId, item.quantity)
        }

        return cartRepository.findByUserId(user.id)
            .orElseThrow {
                logger.warn("Cart disappeared for user {} after update", user.email)
                ResourceNotFoundException("Cart not found after update")
            }
            .toDTO()
    }

    @Transactional
    fun removeItemFromCart(user: User, productId: Long): CartResponseDTO {

        val product = productRepository.findById(productId)
        .orElseThrow {
            logger.warn("Product with id {} not found while removing item", productId)
            ResourceNotFoundException("Product $productId not found")
        }

        val cart = cartRepository.findByUser(user)
        .orElseThrow {
            logger.warn("Cart not found while removing item for user {}", user.email)
            ResourceNotFoundException("Cart not found for user $user.id")
        }

        val item = cartItemRepository.findByCartAndProduct(cart, product)
            .orElseThrow {
                logger.warn("Product {} not present in cart for user {}", productId, user.email)
                ResourceNotFoundException("Product $productId not found in cart")
            }

        cart.items.remove(item)
        cartItemRepository.delete(item)

        logger.info("Removed product {} from cart for user {}", user.email, productId)
        return cart.toDTO()
    }

    fun getCartByUserEmail(email: String): CartResponseDTO {
        val user = userRepository.findByEmail(email)
            .orElseThrow {
                logger.warn("Cart belonging to user with email {} not found", email)
                throw ResourceNotFoundException("User $email not found")
            }

        val cart = cartRepository.findByUserId(user.id)
            .orElseGet {
                cartRepository.save(Cart(user = user))
            }

        return cart.toDTO()
    }

    @Transactional
    fun addItemToCartByUserEmail(
        email: String,
        request: AddCartItemRequestDTO
    ): CartResponseDTO {
        val user = userRepository.findByEmail(email)
            .orElseThrow{
                logger.warn("User with email {} not found while adding item to cart", email)
                ResourceNotFoundException("User $email not found")
            }

        return addItemToCart(
            user = user,
            productId = request.productId,
            requestQuantity = request.quantity)
    }

    @Transactional
    fun removeItemFromCartByUserEmail(
        email: String,
        productId: Long
    ): CartResponseDTO {
        val user = userRepository.findByEmail(email)
            .orElseThrow{
                logger.warn("Cart belonging to user with email {}" +
                        " not found while removing item from cart", email)
                ResourceNotFoundException("User $email not found")
            }

        return removeItemFromCart(user = user, productId = productId)
    }
}