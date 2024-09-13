package com.example

// import io.ktor.client.HttpClient
// import io.ktor.client.statement.HTTPre
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.models.*
import com.example.services.UserService
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.cors.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.encodeToString
import io.ktor.http.ContentType
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import io.ktor.server.sessions.*

@Serializable
data class UserCredentials(val email: String, val password: String, val firstName: String, val lastName: String)

@Serializable
data class UserEmailPassword(val email: String, val password: String)

@Serializable
data class CartUpdate(val cart: String)

@Serializable
data class UserSession(val state: String)

@Serializable
data class TokenResponse(val accessToken: String, val idToken: String)

val cartDatabase: MutableMap<String, String> = mutableMapOf()

fun main() {
    embeddedServer(Netty, port = 9000, host = "0.0.0.0") {
        val userService = UserService()

        


        install(CORS){
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowHeaders { true }
            allowNonSimpleContentTypes = true
            allowCredentials = true
            anyHost()
            exposeHeader("key")
        }
        // Add JWT authentication
        install(Authentication) {
            jwt {
                val jwtAudience = "http://0.0.0.0:9000/api"
                realm = "Ktor Server"
                verifier(
                    JWT
                        .require(Algorithm.HMAC256("secret"))
                        .withAudience(jwtAudience)
                        .withIssuer("http://0.0.0.0:9000/")
                        .build()
                )
                validate { credential ->
                    if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
                }
            }
        }

        // Add this installation
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        install(Sessions) {
            cookie<UserSession>("USER_SESSION")
        }

        routing {
            
            get("/api"){
                call.respondText(Json.encodeToString(itemList), ContentType.Text.Plain)
            }
            post("/api/cart"){
                val post = call.receive<String>()
                call.respondText("Received $post from the cart request.", ContentType.Text.Plain)
            }
            post("/api/payment"){
                val post = call.receive<String>()
                call.application.environment.log.info("Incoming payment request: $post")
                call.respondText("Received $post from the payment request.", ContentType.Text.Plain)
            }
            post("api/register") {
                call.application.environment.log.info("Wlazlem do rejestracji")
                val credentials = call.receive<UserCredentials>()
                call.application.environment.log.info("po wyciagnieciu credentiali")
                call.application.environment.log.info(credentials.toString())
                val existingUser = userService.getUserByEMail(credentials.email)
                if (existingUser != null) {
                    call.respondText("User with this email already exists", status = HttpStatusCode.Conflict)
                } else {
                    val newUser = userService.addUser(credentials.email, credentials.password, credentials.firstName, credentials.lastName)
                    call.respond(HttpStatusCode.Created, newUser)
                }
            }

            post("api/login") {
                val credentials = call.receive<UserEmailPassword>()
                if (userService.verifyUser(credentials.email, credentials.password)) {
                    val token = JWT.create()
                        .withAudience("http://0.0.0.0:9000/api")
                        .withIssuer("http://0.0.0.0:9000/")
                        .withClaim("email", credentials.email)
                        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                        .sign(Algorithm.HMAC256("secret"))
                    call.respond(hashMapOf("token" to token))
                } else {
                    call.respondText("Invalid email or password", status = HttpStatusCode.Unauthorized)
                }
            }

            authenticate {
                get("/api/users") {
                    val users = userService.getAllUsers()
                    call.respond(users)
                }

                post("/api/update_cart") {
                    call.application.environment.log.info("Wlazlem do update_cart")
                    val principal = call.principal<JWTPrincipal>()
                    call.application.environment.log.info("principal zaciagniety")
                    val email = principal!!.payload.getClaim("email").asString()
                    call.application.environment.log.info("Email zaciagniety: $email")
                    val cartUpdate = call.receive<CartUpdate>()
                    call.application.environment.log.info("Cart zaciagniety: ${cartUpdate.cart}")
                    
                    cartDatabase[email] = cartUpdate.cart
                    call.application.environment.log.info("Wlozono do slownika")
                    call.respondText("Cart updated for $email")
                }

                get("/api/get_cart") {
                    val principal = call.principal<JWTPrincipal>()
                    val email = principal!!.payload.getClaim("email").asString()
                    
                    val cart = cartDatabase[email]
                    call.application.environment.log.info("Cart retrievowany: ${cart}")
                    if (cart != null) {
                        call.respond(CartUpdate(cart))
                    } else {
                        call.respondText("No cart found for $email", status = HttpStatusCode.NotFound)
                    }
                }
            }
            
//            get("/login/google") {
//                val state = generateNonce()
//                call.sessions.set(UserSession(state = state))
//
//                val authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
//                    "client_id=${System.getenv("GOOGLE_CLIENT_ID")}&" +
//                    "response_type=code&" +
//                    "scope=email%20profile&" +
//                    "redirect_uri=http://localhost:9000/auth/google/callback&" +
//                    "state=$state"
//
//                call.respondRedirect(authorizeUrl)
//            }

//            get("/auth/google/callback") {
//                suspend fun requestAccessToken(code: String): TokenResponse {
//                    val client = HttpClient()
//                    val response: HttpResponse = client.post("https://oauth2.googleapis.com/token") {
//                        parameter("code", code)
//                        parameter("client_id", System.getenv("GOOGLE_CLIENT_ID"))
//                        parameter("client_secret", System.getenv("GOOGLE_CLIENT_SECRET"))
//                        parameter("redirect_uri", "http://localhost:9000/auth/google/callback")
//                        parameter("grant_type", "authorization_code")
//                    }
//                    val responseBody = response.bodyAsText()
//                    val json = Json.parseToJsonElement(responseBody).jsonObject
//                    call.application.environment.log.info(responseBody)
//                    return TokenResponse(
//                        accessToken = json["access_token"]?.jsonPrimitive?.content ?: throw Exception("No access token"),
//                        idToken = json["id_token"]?.jsonPrimitive?.content ?: throw Exception("No ID token")
//                    )
//                }
//
//                suspend fun requestUserInfo(accessToken: String): GoogleUserInfo {
//                    val client = HttpClient()
//                    val response: HttpResponse = client.get("https://www.googleapis.com/oauth2/v2/userinfo") {
//                        header("Authorization", "Bearer $accessToken")
//                    }
//                    val responseBody = response.bodyAsText()
//                    val json = Json.parseToJsonElement(responseBody).jsonObject
//                    call.application.environment.log.info(responseBody)
//                    return GoogleUserInfo(
//                        email = json["email"]?.jsonPrimitive?.content ?: throw Exception("No email"),
//                        name = json["name"]?.jsonPrimitive?.content ?: throw Exception("No name")
//                    )
//                }
//
//
//                val code = call.parameters["code"]
//                val state = call.parameters["state"]
//                val session = call.sessions.get<UserSession>()
//
//                if (code == null || state == null || session == null || state != session.state) {
//                    call.respond(HttpStatusCode.BadRequest, "Invalid request")
//                    return@get
//                }
//
//                val tokenResponse = requestAccessToken(code)
//                val userInfo = requestUserInfo(tokenResponse.accessToken)
//
//                // Find existing user or create a new one
//                val user = userService.getUserByEMail(userInfo.email)
//                if (user == null) {
//                    val newUser = userService.addUser(
//                        email = userInfo.email,
//                        firstName = userInfo.name,
//                        lastName = "LASTNAME",
//                        password = "123"
//                    )
//
//                    application.log.info("Created new user with Google OAuth: ${newUser.email}")
//                } else {
//                    application.log.info("Existing user logged in with Google OAuth: ${user.email}")
//                }
//
//                // Create JWT token for your app
//                val token = JWT.create()
//                    .withAudience("http://0.0.0.0:9000/api")
//                    .withIssuer("http://0.0.0.0:9000/")
//                    .withClaim("email", userInfo.email)
//                    .withExpiresAt(Date(System.currentTimeMillis() + 60000))
//                    .sign(Algorithm.HMAC256("secret"))
//                // call.respond(hashMapOf("token" to token))
//
//                call.respondRedirect("http://localhost:3000/login-success?token=$token")
//            }
        }
        //        configureRouting()
    }.start(wait = true)

}

data class GoogleUserInfo(val email: String, val name: String)

fun generateNonce(): String {
    return UUID.randomUUID().toString()
}

