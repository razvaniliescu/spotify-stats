import java.net.InetSocketAddress
import com.sun.net.httpserver.{HttpServer, HttpHandler, HttpExchange}
import scala.concurrent.Promise

object SimpleServer {
  private var server: HttpServer = _
  private val codePromise = Promise[String]()

  def startServer(port: Int): Unit = {
    server = HttpServer.create(new InetSocketAddress(port), 0)
    server.createContext("/callback", new CallbackHandler())
    server.setExecutor(null)
    server.start()
  }

  def stopServer(): Unit = {
    if (server != null) {
      server.stop(0)
    }
  }

  def getCodeBlocking: String = {
    import scala.concurrent.Await
    import scala.concurrent.duration._
    Await.result(codePromise.future, 5.minutes)
  }

  private class CallbackHandler extends HttpHandler {
    def handle(exchange: HttpExchange): Unit = {
      val query = exchange.getRequestURI.getQuery
      val codeOpt = query.split("&").find(_.startsWith("code=")).map(_.substring(5))
      codeOpt match {
        case Some(code) =>
          val response = "You can now return to the app. Authorization successful."
          exchange.sendResponseHeaders(200, response.length)
          val os = exchange.getResponseBody
          os.write(response.getBytes)
          os.close()

          codePromise.success(code)
        case None =>
          exchange.sendResponseHeaders(400, 0)
          exchange.getResponseBody.close()
      }
    }
  }
}
