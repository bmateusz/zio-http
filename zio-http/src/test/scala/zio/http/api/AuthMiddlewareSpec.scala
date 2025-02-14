package zio.http.api

import zio._
import zio.http.internal.HttpAppTestExtensions
import zio.http.model.{Headers, Status}
import zio.http.{Http, Request, URL}
import zio.test.Assertion.{equalTo, isSome}
import zio.test._

object AuthMiddlewareSpec extends ZIOSpecDefault with HttpAppTestExtensions {

  private val successBasicHeader  = Headers.basicAuthorizationHeader("user", "resu")
  private val failureBasicHeader  = Headers.basicAuthorizationHeader("user", "user")
  private val bearerToken         = "dummyBearerToken"
  private val successBearerHeader = Headers.bearerAuthorizationHeader(bearerToken)
  private val failureBearerHeader = Headers.bearerAuthorizationHeader(bearerToken + "SomethingElse")

  private val basicAuth     = Middleware.basicAuth { с => с.uname.reverse == с.upassword }
  private val basicAuthZIO  = Middleware.basicAuthZIO { c => ZIO.succeed(c.uname.reverse == c.upassword) }
  private val bearerAuth    = Middleware.bearerAuth { _ == bearerToken }
  private val bearerAuthZIO = Middleware.bearerAuthZIO { c => ZIO.succeed(c == bearerToken) }

  override def spec =
    suite("Auth Middleware Spec")(
      suite("basicAuth")(
        test("HttpApp is accepted if the basic authentication succeeds") {
          val app = Http.ok.withMiddleware(basicAuth).status
          assertZIO(app(Request.get(URL.empty).copy(headers = successBasicHeader)))(equalTo(Status.Ok))
        },
        test("Uses forbidden app if the basic authentication fails") {
          val app = Http.ok.withMiddleware(basicAuth).status
          assertZIO(app(Request.get(URL.empty).copy(headers = failureBasicHeader)))(equalTo(Status.Unauthorized))
        },
        test("Responses should have WWW-Authentication header if Basic Auth failed") {
          val app = Http.ok.withMiddleware(basicAuth).header("WWW-AUTHENTICATE")
          assertZIO(app(Request.get(URL.empty).copy(headers = failureBasicHeader)))(isSome)
        },
      ),
      suite("basicAuthZIO")(
        test("HttpApp is accepted if the basic authentication succeeds") {
          val app = Http.ok.withMiddleware(basicAuthZIO).status
          assertZIO(app(Request.get(URL.empty).copy(headers = successBasicHeader)))(equalTo(Status.Ok))
        },
        test("Uses forbidden app if the basic authentication fails") {
          val app = Http.ok.withMiddleware(basicAuthZIO).status
          assertZIO(app(Request.get(URL.empty).copy(headers = failureBasicHeader)))(equalTo(Status.Unauthorized))
        },
        test("Responses should have WWW-Authentication header if Basic Auth failed") {
          val app = Http.ok.withMiddleware(basicAuthZIO).header("WWW-AUTHENTICATE")
          assertZIO(app(Request.get(URL.empty).copy(headers = failureBasicHeader)))(isSome)
        },
      ),
      suite("bearerAuth")(
        test("HttpApp is accepted if the bearer authentication succeeds") {
          val app = Http.ok.withMiddleware(bearerAuth).status
          assertZIO(app(Request.get(URL.empty).copy(headers = successBearerHeader)))(equalTo(Status.Ok))
        },
        test("Uses forbidden app if the bearer authentication fails") {
          val app = Http.ok.withMiddleware(bearerAuth).status
          assertZIO(app(Request.get(URL.empty).copy(headers = failureBearerHeader)))(equalTo(Status.Unauthorized))
        },
        test("Responses should have WWW-Authentication header if bearer Auth failed") {
          val app = Http.ok.withMiddleware(bearerAuth).header("WWW-AUTHENTICATE")
          assertZIO(app(Request.get(URL.empty).copy(headers = failureBearerHeader)))(isSome)
        },
      ),
      suite("bearerAuthZIO")(
        test("HttpApp is accepted if the bearer authentication succeeds") {
          val app = Http.ok.withMiddleware(bearerAuthZIO).status
          assertZIO(app(Request.get(URL.empty).copy(headers = successBearerHeader)))(equalTo(Status.Ok))
        },
        test("Uses forbidden app if the bearer authentication fails") {
          val app = Http.ok.withMiddleware(bearerAuthZIO).status
          assertZIO(app(Request.get(URL.empty).copy(headers = failureBearerHeader)))(equalTo(Status.Unauthorized))
        },
        test("Responses should have WWW-Authentication header if bearer Auth failed") {
          val app = Http.ok.withMiddleware(bearerAuthZIO).header("WWW-AUTHENTICATE")
          assertZIO(app(Request.get(URL.empty).copy(headers = failureBearerHeader)))(isSome)
        },
      ),
    )
}
